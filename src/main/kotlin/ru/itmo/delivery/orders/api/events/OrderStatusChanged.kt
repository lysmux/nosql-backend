package ru.itmo.delivery.orders.api.events

import ru.itmo.delivery.orders.api.models.Order

enum class OrderEventType {
    ORDER_CREATED,
    ORDER_STATUS_CHANGED,
}

data class OrderStatusChanged(
    val order: Order,
    val eventType: OrderEventType,
)
