package ru.itmo.delivery.notifications.api

import ru.itmo.delivery.notifications.api.models.Notification
import ru.itmo.delivery.shared.models.Paged
import java.util.UUID

interface Notifications {
    fun feed(userId: UUID, page: Int, size: Int): Paged<Notification>
}
