package ru.itmo.delivery.auth.internal.adapters.inbound.rest

import ru.itmo.delivery.auth.api.models.Session
import ru.itmo.delivery.entrypoint.api.model.SessionDto

fun Session.toDto() = SessionDto(userId = userId, expiresAt = expiresAt)
