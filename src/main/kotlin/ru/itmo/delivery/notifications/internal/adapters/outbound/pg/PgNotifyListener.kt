package ru.itmo.delivery.notifications.internal.adapters.outbound.pg

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.postgresql.PGConnection
import org.slf4j.LoggerFactory
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.stereotype.Component
import ru.itmo.delivery.notifications.internal.adapters.inbound.sse.NotificationStreamRegistry
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.NotificationsDAO
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.toNotification
import tools.jackson.databind.json.JsonMapper
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID

@Component
class PgNotifyListener(
    private val datasource: DataSourceProperties,
    private val dao: NotificationsDAO,
    private val registry: NotificationStreamRegistry,
    private val json: JsonMapper,
) {
    @Volatile
    private var running = false

    @Volatile
    private var connection: Connection? = null

    private var thread: Thread? = null

    @PostConstruct
    fun start() {
        running = true
        thread = Thread(::listenForever, "pg-notify-listener").apply {
            isDaemon = true
            start()
        }
    }

    @PreDestroy
    fun stop() {
        running = false
        thread?.interrupt()
        closeConnection()
        thread?.join(JOIN_TIMEOUT_MILLIS)
    }

    private fun listenForever() {
        var failureLogged = false
        while (running) {
            try {
                listen { failureLogged = false }
            } catch (exception: InterruptedException) {
                return
            } catch (exception: Exception) {
                if (!running) return
                if (!failureLogged) {
                    log.warn(
                        "Notification listener lost its connection, retrying every {} ms",
                        RECONNECT_BACKOFF_MILLIS,
                        exception,
                    )
                    failureLogged = true
                }
            } finally {
                closeConnection()
            }
            if (running) Thread.sleep(RECONNECT_BACKOFF_MILLIS)
        }
    }

    private fun listen(onListening: () -> Unit) {
        val own = DriverManager.getConnection(
            datasource.determineUrl(),
            datasource.determineUsername(),
            datasource.determinePassword(),
        )
        connection = own
        val pg = own.unwrap(PGConnection::class.java)
        own.createStatement().use { it.execute("LISTEN $CHANNEL") }
        log.info("Listening on PostgreSQL channel {}", CHANNEL)
        onListening()
        while (running) {
            pg.getNotifications(POLL_TIMEOUT_MILLIS)?.forEach { dispatch(it.parameter) }
        }
    }

    private fun dispatch(payload: String) {
        try {
            val node = json.readTree(payload)
            val notificationId = UUID.fromString(node.get("notificationId").asString())
            val userId = UUID.fromString(node.get("userId").asString())
            dao.findById(notificationId)?.let { registry.publish(userId, it.toNotification()) }
        } catch (exception: Exception) {
            log.warn("Failed to dispatch notification payload {}", payload, exception)
        }
    }

    private fun closeConnection() {
        val own = connection ?: return
        connection = null
        runCatching { own.createStatement().use { it.execute("UNLISTEN *") } }
        runCatching { own.close() }
    }

    private companion object {
        private val log = LoggerFactory.getLogger(PgNotifyListener::class.java)
        const val CHANNEL = "order_notifications"
        const val POLL_TIMEOUT_MILLIS = 1000
        const val RECONNECT_BACKOFF_MILLIS = 2000L
        const val JOIN_TIMEOUT_MILLIS = 3000L
    }
}
