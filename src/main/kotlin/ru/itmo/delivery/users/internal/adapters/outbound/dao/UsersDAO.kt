package ru.itmo.delivery.users.internal.adapters.outbound.dao

import ru.itmo.delivery.users.api.models.User
import java.util.UUID

interface UsersDAO {
    fun save(user: User, passwordHash: String)

    fun get(id: UUID): User?

    fun findCredentials(email: String): UserCredentials?

    fun getAll(ids: Collection<UUID>): Map<UUID, User>

    fun exists(id: UUID): Boolean

    fun existsByEmail(email: String): Boolean

    fun updateCourierMode(id: UUID, enabled: Boolean): User?

    fun findCourierIds(): List<UUID>
}

data class UserCredentials(val userId: UUID, val passwordHash: String)
