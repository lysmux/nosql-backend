package ru.itmo.delivery.orders.api.models

import java.util.UUID

data class CreateOrderCommand(
    val restaurantId: UUID,
    val items: List<OrderItemRequest>,
    val deliveryAddress: String,
)

data class OrderItemRequest(
    val menuItemId: UUID,
    val quantity: Int,
)
