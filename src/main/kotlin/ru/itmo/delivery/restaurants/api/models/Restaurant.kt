package ru.itmo.delivery.restaurants.api.models

import java.time.Instant
import java.util.UUID

data class Restaurant(
    val restaurantId: UUID,
    val ownerId: UUID,
    val name: String,
    val createdAt: Instant,
)
