package ru.itmo.delivery.auth.api.models

import java.time.Instant
import java.util.UUID

data class Session(
    val token: String,
    val userId: UUID,
    val expiresAt: Instant,
)
