package ru.itmo.delivery.notifications.internal.adapters.outbound.dao.impl

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.itmo.delivery.notifications.api.models.NotificationAuthor
import ru.itmo.delivery.notifications.api.models.NotificationEventType
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.NotificationRecord
import ru.itmo.delivery.orders.api.models.ActorType
import ru.itmo.delivery.orders.api.models.OrderStatus
import java.time.Instant
import java.util.UUID

const val NOTIFICATION_SCHEMA_VERSION = 2

data class AuthorDocument(
    val type: ActorType,
    val id: UUID?,
)

@Document(collection = NotificationDocument.COLLECTION)
data class NotificationDocument(
    @Id val id: UUID,
    val userId: UUID,
    val orderId: UUID,
    val eventType: NotificationEventType,
    val status: OrderStatus,
    val text: String,
    val createdAt: Instant,
    val author: AuthorDocument? = null,
    val reason: String? = null,
    val schemaVersion: Int = NOTIFICATION_SCHEMA_VERSION,
) {
    fun toRecord() = NotificationRecord(
        notificationId = id,
        userId = userId,
        orderId = orderId,
        eventType = eventType,
        status = status,
        text = text,
        author = author?.let { NotificationAuthor(it.type, it.id) }
            ?: NotificationAuthor(ActorType.SYSTEM, null),
        reason = reason ?: LEGACY_REASON,
        createdAt = createdAt,
    )

    companion object {
        const val COLLECTION = "notifications"
        const val LEGACY_REASON = "legacy"
    }
}
