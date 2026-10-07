package ru.itmo.delivery.users.api.models

import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID,
    val name: String,
    val email: String,
    val courierModeEnabled: Boolean,
    val createdAt: Instant,
)
