package ru.itmo.delivery.orders.api.models

import java.math.BigDecimal
import java.util.UUID

data class RestaurantOrderStats(
    val restaurantId: UUID,
    val name: String,
    val total: Long,
    val delivered: Long,
    val cancelled: Long,
    val revenue: BigDecimal,
)
