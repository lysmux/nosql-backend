package ru.itmo.delivery.notifications.internal.adapters.outbound.dao.impl

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.NotificationRecord
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.NotificationsDAO
import ru.itmo.delivery.shared.models.Paged
import java.util.UUID

@Repository
class NotificationsDefaultDao(
    private val notifications: NotificationJpaRepository,
) : NotificationsDAO {
    override fun save(notification: NotificationRecord) {
        notifications.save(
            NotificationEntity(
                notificationId = notification.notificationId,
                userId = notification.userId,
                orderId = notification.orderId,
                eventType = notification.eventType,
                status = notification.status,
                text = notification.text,
                createdAt = notification.createdAt,
            ),
        )
    }

    @Transactional(readOnly = true)
    override fun findById(notificationId: UUID): NotificationRecord? =
        notifications.findById(notificationId).orElse(null)?.toRecord()

    @Transactional(readOnly = true)
    override fun findPage(userId: UUID, page: Int, size: Int): Paged<NotificationRecord> {
        val items = notifications.pageByUser(userId, size, page.toLong() * size).map { it.toRecord() }
        return Paged(items, page, size, notifications.countByUser(userId))
    }

    private fun NotificationEntity.toRecord() = NotificationRecord(
        notificationId = notificationId,
        userId = userId,
        orderId = orderId,
        eventType = eventType,
        status = status,
        text = text,
        createdAt = createdAt,
    )
}
