package ru.itmo.delivery.auth.api

import ru.itmo.delivery.auth.api.models.Session
import java.util.UUID

interface Sessions {
    fun create(userId: UUID): Session

    fun resolve(token: String): UUID

    fun isActive(token: String): Boolean

    fun logout(token: String)
}
