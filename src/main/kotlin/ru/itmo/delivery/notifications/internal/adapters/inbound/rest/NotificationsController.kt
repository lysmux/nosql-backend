package ru.itmo.delivery.notifications.internal.adapters.inbound.rest

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import ru.itmo.delivery.auth.api.currentUserId
import ru.itmo.delivery.auth.api.sessionToken
import ru.itmo.delivery.entrypoint.api.NotificationsApi
import ru.itmo.delivery.entrypoint.api.model.NotificationPageDto
import ru.itmo.delivery.notifications.api.Notifications
import ru.itmo.delivery.notifications.internal.adapters.inbound.sse.NotificationStreamRegistry

@RestController
class NotificationsController(
    private val notifications: Notifications,
    private val registry: NotificationStreamRegistry,
    private val request: HttpServletRequest,
) : NotificationsApi {
    override fun notificationsFeed(page: Int, size: Int): ResponseEntity<NotificationPageDto> =
        ResponseEntity.ok(notifications.feed(currentUserId(), page, size).toPageDto())

    override fun notificationsStream(): ResponseEntity<SseEmitter> =
        ResponseEntity.ok()
            .cacheControl(CacheControl.noCache())
            .body(registry.subscribe(currentUserId(), request.sessionToken()))
}
