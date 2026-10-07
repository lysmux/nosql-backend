package ru.itmo.delivery.notifications.internal.adapters.inbound.rest

import ru.itmo.delivery.entrypoint.api.model.NotificationDto
import ru.itmo.delivery.entrypoint.api.model.NotificationEventTypeDto
import ru.itmo.delivery.entrypoint.api.model.NotificationPageDto
import ru.itmo.delivery.entrypoint.api.model.OrderStatusDto
import ru.itmo.delivery.notifications.api.models.Notification
import ru.itmo.delivery.notifications.api.models.NotificationEventType
import ru.itmo.delivery.shared.models.Paged

fun NotificationEventType.toDto() = NotificationEventTypeDto.valueOf(name)

fun Notification.toDto() = NotificationDto(
    notificationId = notificationId,
    userId = userId,
    orderId = orderId,
    eventType = eventType.toDto(),
    status = OrderStatusDto.valueOf(status.name),
    text = text,
    createdAt = createdAt,
)

fun Paged<Notification>.toPageDto() = NotificationPageDto(
    items = items.map { it.toDto() },
    page = page,
    pageSize = size,
    totalItems = totalItems,
    totalPages = totalPages,
)
