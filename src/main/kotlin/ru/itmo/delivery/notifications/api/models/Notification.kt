package ru.itmo.delivery.notifications.api.models

import ru.itmo.delivery.orders.api.models.ActorType
import ru.itmo.delivery.orders.api.models.OrderStatus
import java.time.Instant
import java.util.UUID

enum class NotificationEventType {
    ORDER_CREATED,
    ORDER_STATUS_CHANGED,
}

data class NotificationAuthor(
    val type: ActorType,
    val id: UUID?,
)

data class Notification(
    val notificationId: UUID,
    val userId: UUID,
    val orderId: UUID,
    val eventType: NotificationEventType,
    val status: OrderStatus,
    val text: String,
    val author: NotificationAuthor,
    val reason: String,
    val createdAt: Instant,
)
