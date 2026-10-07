package ru.itmo.delivery.notifications.internal.adapters.outbound.dao

import ru.itmo.delivery.shared.models.Paged
import java.util.UUID

interface NotificationsDAO {
    fun save(notification: NotificationRecord)

    fun findById(notificationId: UUID): NotificationRecord?

    fun findPage(userId: UUID, page: Int, size: Int): Paged<NotificationRecord>
}
