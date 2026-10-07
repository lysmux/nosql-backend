package ru.itmo.delivery.users.api

import ru.itmo.delivery.users.api.models.User
import java.util.UUID

interface Users {
    fun register(name: String, email: String, password: String): User

    fun authenticate(email: String, password: String): UUID

    fun get(id: UUID): User?

    fun getAll(ids: Collection<UUID>): Map<UUID, User>

    fun exists(id: UUID): Boolean

    fun setCourierMode(userId: UUID, enabled: Boolean): User
}
