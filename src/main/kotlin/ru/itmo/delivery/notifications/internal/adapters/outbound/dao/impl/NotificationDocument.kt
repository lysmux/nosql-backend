package ru.itmo.delivery.notifications.internal.adapters.outbound.dao.impl

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.itmo.delivery.notifications.api.models.NotificationEventType
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.NotificationRecord
import ru.itmo.delivery.orders.api.models.OrderStatus
import java.time.Instant
import java.util.UUID

const val NOTIFICATION_SCHEMA_VERSION = 1

@Document(collection = NotificationDocument.COLLECTION)
data class NotificationDocument(
    @Id val id: UUID,
    val userId: UUID,
    val orderId: UUID,
    val eventType: NotificationEventType,
    val status: OrderStatus,
    val text: String,
    val createdAt: Instant,
    val schemaVersion: Int = NOTIFICATION_SCHEMA_VERSION,
) {
    fun toRecord() = NotificationRecord(
        notificationId = id,
        userId = userId,
        orderId = orderId,
        eventType = eventType,
        status = status,
        text = text,
        createdAt = createdAt,
    )

    companion object {
        const val COLLECTION = "notifications"
    }
}
