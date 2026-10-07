package ru.itmo.delivery.notifications.internal.adapters.outbound.dao.impl

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.domain.Persistable
import ru.itmo.delivery.notifications.api.models.NotificationEventType
import ru.itmo.delivery.orders.api.models.OrderStatus
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "notifications")
class NotificationEntity(
    @Id
    var notificationId: UUID,
    var userId: UUID,
    var orderId: UUID,
    @Enumerated(EnumType.STRING)
    var eventType: NotificationEventType,
    @Enumerated(EnumType.STRING)
    var status: OrderStatus,
    var text: String,
    var createdAt: Instant,
) : Persistable<UUID> {
    override fun getId(): UUID = notificationId

    override fun isNew(): Boolean = true
}
