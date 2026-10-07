package ru.itmo.delivery.menu.api.models

import java.math.BigDecimal
import java.util.UUID

data class OrderMenuItem(
    val menuItemId: UUID,
    val restaurantId: UUID,
    val name: String,
    val price: BigDecimal,
    val available: Boolean,
    val archived: Boolean,
)
