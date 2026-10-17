package ru.itmo.delivery.notifications.internal.adapters.inbound.mongo

import com.mongodb.client.MongoChangeStreamCursor
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.changestream.ChangeStreamDocument
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.bson.BsonDocument
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import ru.itmo.delivery.notifications.internal.adapters.inbound.sse.NotificationStreamRegistry
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.impl.NotificationDocument
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.toNotification
import java.util.concurrent.TimeUnit

@Component
class NotificationChangeStreamListener(
    private val template: MongoTemplate,
    private val registry: NotificationStreamRegistry,
) {
    @Volatile
    private var running = false

    @Volatile
    private var reopenRequested = false

    @Volatile
    private var cursor: MongoChangeStreamCursor<ChangeStreamDocument<Document>>? = null

    @Volatile
    private var resumeToken: BsonDocument? = null

    private var thread: Thread? = null

    @PostConstruct
    fun start() {
        running = true
        thread = Thread(::listenForever, "mongo-change-stream-listener").apply {
            isDaemon = true
            start()
        }
    }

    @PreDestroy
    fun stop() {
        running = false
        thread?.interrupt()
        closeCursor()
        thread?.join(JOIN_TIMEOUT_MILLIS)
    }

    internal fun isListening(): Boolean = cursor != null

    internal fun reopen() {
        reopenRequested = true
        closeCursor()
    }

    internal fun dispatch(document: Document) {
        try {
            val notification = template.converter.read(NotificationDocument::class.java, document)
                .toRecord()
                .toNotification()
            registry.publish(notification.userId, notification)
        } catch (exception: Exception) {
            log.warn("Failed to dispatch notification document {}", document, exception)
        }
    }

    private fun listenForever() {
        var failureLogged = false
        while (running) {
            try {
                reopenRequested = false
                listen { failureLogged = false }
            } catch (exception: InterruptedException) {
                return
            } catch (exception: Exception) {
                if (!running) return
                if (!reopenRequested && !failureLogged) {
                    log.warn(
                        "Notification change stream failed, retrying every {} ms",
                        RECONNECT_BACKOFF_MILLIS,
                        exception,
                    )
                    failureLogged = true
                }
            } finally {
                closeCursor()
            }
            if (running && !reopenRequested && !pauseBeforeRetry()) return
        }
    }

    private fun listen(onListening: () -> Unit) {
        val pipeline = listOf(Aggregates.match(Filters.eq("operationType", "insert")))
        val token = resumeToken
        val opened = try {
            template.getCollection(NotificationDocument.COLLECTION)
                .watch(pipeline)
                .maxAwaitTime(POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .let { if (token != null) it.resumeAfter(token) else it }
                .cursor()
        } catch (exception: Exception) {
            resumeToken = null
            throw exception
        }

        opened.use { own ->
            cursor = own
            log.info("Listening to change stream of collection {}", NotificationDocument.COLLECTION)
            onListening()

            while (running && !reopenRequested) {
                val event = own.tryNext() ?: continue
                resumeToken = event.resumeToken
                event.fullDocument?.let(::dispatch)
            }
        }
    }

    private fun pauseBeforeRetry(): Boolean =
        try {
            Thread.sleep(RECONNECT_BACKOFF_MILLIS)
            true
        } catch (exception: InterruptedException) {
            false
        }

    private fun closeCursor() {
        val own = cursor ?: return
        cursor = null
        runCatching { own.close() }
    }

    private companion object {
        private val log = LoggerFactory.getLogger(NotificationChangeStreamListener::class.java)
        const val POLL_TIMEOUT_SECONDS = 1L
        const val RECONNECT_BACKOFF_MILLIS = 2000L
        const val JOIN_TIMEOUT_MILLIS = 3000L
    }
}
