package ru.itmo.delivery.notifications.internal

import org.springframework.stereotype.Service
import ru.itmo.delivery.notifications.api.Notifications
import ru.itmo.delivery.notifications.api.models.Notification
import ru.itmo.delivery.notifications.api.models.NotificationEventType
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.NotificationRecord
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.NotificationsDAO
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.toNotification
import ru.itmo.delivery.orders.api.events.OrderEventType
import ru.itmo.delivery.orders.api.models.Order
import ru.itmo.delivery.orders.api.models.OrderStatus
import ru.itmo.delivery.shared.exceptions.ValidationException
import ru.itmo.delivery.shared.models.Paged
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class NotificationsService(
    private val dao: NotificationsDAO,
) : Notifications {
    override fun feed(userId: UUID, page: Int, size: Int): Paged<Notification> {
        if (page < 0 || size !in MIN_PAGE_SIZE..MAX_PAGE_SIZE) {
            throw ValidationException("page должен быть >= 0, size от $MIN_PAGE_SIZE до $MAX_PAGE_SIZE")
        }

        val records = dao.findPage(userId, page, size)
        return records.withItems(records.items.map { it.toNotification() })
    }

    fun createFor(order: Order, eventType: OrderEventType) {
        dao.save(
            NotificationRecord(
                notificationId = UUID.randomUUID(),
                userId = order.userId,
                orderId = order.orderId,
                eventType = eventType.toNotificationEventType(),
                status = order.status,
                text = textFor(order),
                createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS),
            ),
        )
    }

    private fun OrderEventType.toNotificationEventType() = NotificationEventType.valueOf(name)

    private fun textFor(order: Order): String {
        val restaurant = "«${order.restaurantName}»"
        return when (order.status) {
            OrderStatus.CREATED -> "Ваш заказ из $restaurant принят"
            OrderStatus.COOKING -> "Ваш заказ из $restaurant готовится"
            OrderStatus.READY -> "Ваш заказ из $restaurant готов и ожидает курьера"
            OrderStatus.CANCELLED -> "Ваш заказ из $restaurant отменён"
            OrderStatus.IN_DELIVERY -> "Ваш заказ из $restaurant передан курьеру"
            OrderStatus.DELIVERED -> "Ваш заказ из $restaurant доставлен"
        }
    }

    private companion object {
        const val MIN_PAGE_SIZE = 1
        const val MAX_PAGE_SIZE = 100
    }
}
