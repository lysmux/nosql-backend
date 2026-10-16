package ru.itmo.delivery.notifications.internal.adapters.inbound.events

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import ru.itmo.delivery.notifications.internal.NotificationsService
import ru.itmo.delivery.orders.api.events.OrderStatusChanged

@Component
class OrderEventsListener(
    private val notifications: NotificationsService,
) {
    @EventListener
    fun on(event: OrderStatusChanged) {
        notifications.createFor(event.order, event.eventType, event.entry)
    }
}
