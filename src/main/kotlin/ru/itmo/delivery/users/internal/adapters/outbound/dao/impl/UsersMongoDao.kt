package ru.itmo.delivery.users.internal.adapters.outbound.dao.impl

import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import ru.itmo.delivery.users.api.models.User
import ru.itmo.delivery.users.internal.adapters.outbound.dao.UserCredentials
import ru.itmo.delivery.users.internal.adapters.outbound.dao.UsersDAO
import java.util.UUID

@Repository
class UsersMongoDao(
    private val template: MongoTemplate,
) : UsersDAO {
    override fun save(user: User, passwordHash: String) {
        template.insert(
            UserDocument(
                id = user.id,
                name = user.name,
                email = user.email,
                passwordHash = passwordHash,
                courierModeEnabled = user.courierModeEnabled,
                createdAt = user.createdAt,
            ),
        )
    }

    override fun get(id: UUID): User? = template.findById(id, UserDocument::class.java)?.toDomain()

    override fun findCredentials(email: String): UserCredentials? =
        template.findOne(byEmail(email), UserDocument::class.java)
            ?.let { UserCredentials(it.id, it.passwordHash) }

    override fun getAll(ids: Collection<UUID>): Map<UUID, User> =
        template.find(Query(Criteria.where("_id").`in`(ids)), UserDocument::class.java)
            .associate { it.id to it.toDomain() }

    override fun exists(id: UUID): Boolean =
        template.exists(Query(Criteria.where("_id").`is`(id)), UserDocument::class.java)

    override fun existsByEmail(email: String): Boolean =
        template.exists(byEmail(email), UserDocument::class.java)

    override fun updateCourierMode(id: UUID, enabled: Boolean): User? =
        template.findAndModify(
            Query(Criteria.where("_id").`is`(id)),
            Update().set("courierModeEnabled", enabled),
            FindAndModifyOptions.options().returnNew(true),
            UserDocument::class.java,
        )?.toDomain()

    override fun findCourierIds(): List<UUID> =
        template.find(
            Query(Criteria.where("courierModeEnabled").`is`(true)),
            UserDocument::class.java,
        ).map { it.id }

    override fun setActiveOrder(id: UUID, orderId: UUID?) {
        template.updateFirst(
            Query(Criteria.where("_id").`is`(id)),
            Update().set("activeOrderId", orderId),
            UserDocument::class.java,
        )
    }

    private fun byEmail(email: String) = Query(Criteria.where("email").`is`(email))

    private fun UserDocument.toDomain() = User(
        id = id,
        name = name,
        email = email,
        courierModeEnabled = courierModeEnabled,
        createdAt = createdAt,
    )
}
