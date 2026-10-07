package ru.itmo.delivery.menu.api.models

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class MenuItem(
    val menuItemId: UUID,
    val restaurantId: UUID,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val available: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class NewMenuItem(
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val available: Boolean,
)

data class MenuItemPatch(
    val name: String?,
    val description: String?,
    val price: BigDecimal?,
    val available: Boolean?,
    val descriptionPresent: Boolean = false,
) {
    fun isEmpty() = name == null && !descriptionPresent && price == null && available == null
}
