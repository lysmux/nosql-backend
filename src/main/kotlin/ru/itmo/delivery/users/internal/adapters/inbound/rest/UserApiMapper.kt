package ru.itmo.delivery.users.internal.adapters.inbound.rest

import ru.itmo.delivery.entrypoint.api.model.UserDto
import ru.itmo.delivery.users.api.models.User

fun User.toDto() = UserDto(
    userId = id,
    name = name,
    email = email,
    courierModeEnabled = courierModeEnabled,
    createdAt = createdAt,
)
