package ru.itmo.delivery.notifications.internal.adapters.inbound.sse

import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import ru.itmo.delivery.auth.api.Sessions
import ru.itmo.delivery.notifications.api.models.Notification
import ru.itmo.delivery.notifications.internal.adapters.inbound.rest.toDto
import tools.jackson.databind.json.JsonMapper
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class NotificationStreamRegistry(
    private val json: JsonMapper,
    private val sessions: Sessions,
) {
    private data class Subscription(val emitter: SseEmitter, val token: String)

    private val subscriptions = ConcurrentHashMap<UUID, Set<Subscription>>()

    fun subscribe(userId: UUID, token: String): SseEmitter {
        val emitter = register(userId, token, SseEmitter(NO_TIMEOUT))
        sendOrDrop(userId, emitter) { SseEmitter.event().comment("connected") }
        return emitter
    }

    internal fun register(userId: UUID, token: String, emitter: SseEmitter): SseEmitter {
        val subscription = Subscription(emitter, token)
        subscriptions.compute(userId) { _, current -> (current ?: emptySet()) + subscription }

        emitter.onCompletion { remove(userId, subscription) }
        emitter.onTimeout { remove(userId, subscription) }
        emitter.onError { remove(userId, subscription) }

        return emitter
    }

    internal fun emitterCount(userId: UUID): Int = subscriptions[userId]?.size ?: 0

    fun publish(userId: UUID, notification: Notification) {
        val userSubscriptions = subscriptions[userId] ?: return
        val payload = json.writeValueAsString(notification.toDto())

        userSubscriptions.forEach { subscription ->
            sendOrDrop(userId, subscription.emitter) {
                SseEmitter.event()
                    .name(EVENT_NAME)
                    .id(notification.notificationId.toString())
                    .data(payload, MediaType.APPLICATION_JSON)
            }
        }
    }

    @Scheduled(fixedDelay = HEARTBEAT_SECONDS, timeUnit = TimeUnit.SECONDS)
    fun heartbeat() {
        subscriptions.forEach { (userId, userSubscriptions) ->
            userSubscriptions.forEach { subscription ->
                if (isSessionEnded(subscription.token)) {
                    close(userId, subscription)
                } else {
                    sendOrDrop(userId, subscription.emitter) { SseEmitter.event().comment("") }
                }
            }
        }
    }

    @PreDestroy
    fun closeAll() {
        subscriptions.forEach { (userId, userSubscriptions) -> userSubscriptions.forEach { close(userId, it) } }
    }

    private fun isSessionEnded(token: String): Boolean =
        try {
            !sessions.isActive(token)
        } catch (exception: Exception) {
            log.warn("Could not verify the session of a notification stream: {}", exception.message)
            false
        }

    private fun sendOrDrop(userId: UUID, emitter: SseEmitter, event: () -> SseEmitter.SseEventBuilder) {
        try {
            emitter.send(event())
        } catch (exception: Exception) {
            log.debug("Dropping notification stream of user {}: {}", userId, exception.message)
            subscriptions[userId]?.firstOrNull { it.emitter === emitter }
                ?.let { close(userId, it) }
                ?: runCatching { emitter.complete() }
        }
    }

    private fun close(userId: UUID, subscription: Subscription) {
        remove(userId, subscription)
        runCatching { subscription.emitter.complete() }
    }

    private fun remove(userId: UUID, subscription: Subscription) {
        subscriptions.compute(userId) { _, current ->
            (current.orEmpty() - subscription).takeIf { it.isNotEmpty() }
        }
    }

    private companion object {
        private val log = LoggerFactory.getLogger(NotificationStreamRegistry::class.java)
        const val EVENT_NAME = "notification"
        const val NO_TIMEOUT = 0L
        const val HEARTBEAT_SECONDS = 15L
    }
}
