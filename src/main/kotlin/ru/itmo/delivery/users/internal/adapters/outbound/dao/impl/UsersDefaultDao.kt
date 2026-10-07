package ru.itmo.delivery.users.internal.adapters.outbound.dao.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.itmo.delivery.users.api.models.User
import ru.itmo.delivery.users.internal.adapters.outbound.dao.UserCredentials
import ru.itmo.delivery.users.internal.adapters.outbound.dao.UsersDAO
import java.util.UUID

@Repository
class UsersDefaultDao(
    private val users: UserJpaRepository,
) : UsersDAO {

    override fun save(user: User, passwordHash: String) {
        users.saveAndFlush(
            UserEntity(user.id, user.name, user.email, passwordHash, user.courierModeEnabled, user.createdAt),
        )
    }

    override fun get(id: UUID): User? = users.findByIdOrNull(id)?.toDomain()

    override fun findCredentials(email: String): UserCredentials? =
        users.findByEmail(email)?.let { UserCredentials(it.userId, it.passwordHash) }

    override fun getAll(ids: Collection<UUID>): Map<UUID, User> =
        users.findAllById(ids).associate { it.userId to it.toDomain() }

    override fun exists(id: UUID): Boolean = users.existsById(id)

    override fun existsByEmail(email: String): Boolean = users.existsByEmail(email)

    @Transactional
    override fun updateCourierMode(id: UUID, enabled: Boolean): User? {
        val entity = users.findByIdOrNull(id) ?: return null
        entity.courierModeEnabled = enabled
        return users.save(entity).toDomain()
    }

    private fun UserEntity.toDomain() = User(userId, name, email, courierModeEnabled, createdAt)
}
