package ru.itmo.delivery.notifications.internal.adapters.outbound.dao

import ru.itmo.delivery.notifications.api.models.Notification
import ru.itmo.delivery.notifications.api.models.NotificationEventType
import ru.itmo.delivery.orders.api.models.OrderStatus
import java.time.Instant
import java.util.UUID

data class NotificationRecord(
    val notificationId: UUID,
    val userId: UUID,
    val orderId: UUID,
    val eventType: NotificationEventType,
    val status: OrderStatus,
    val text: String,
    val createdAt: Instant,
)

fun NotificationRecord.toNotification() = Notification(
    notificationId = notificationId,
    userId = userId,
    orderId = orderId,
    eventType = eventType,
    status = status,
    text = text,
    createdAt = createdAt,
)
