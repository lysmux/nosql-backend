package ru.itmo.delivery.restaurants.internal.adapters.outbound.dao.impl

import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import ru.itmo.delivery.restaurants.api.models.Restaurant
import ru.itmo.delivery.restaurants.internal.adapters.outbound.dao.RestaurantsDAO
import ru.itmo.delivery.shared.models.Paged
import java.util.UUID

@Repository
class RestaurantsMongoDao(
    private val template: MongoTemplate,
) : RestaurantsDAO {
    private val catalogOrder = Sort.by(Sort.Direction.ASC, "name", "_id")

    override fun save(restaurant: Restaurant) {
        template.save(
            RestaurantDocument(
                id = restaurant.restaurantId,
                ownerId = restaurant.ownerId,
                name = restaurant.name,
                createdAt = restaurant.createdAt,
            ),
        )
    }

    override fun get(id: UUID): Restaurant? =
        template.findById(id, RestaurantDocument::class.java)?.toDomain()

    override fun findPage(ownerId: UUID?, page: Int, size: Int): Paged<Restaurant> {
        val filter = ownerId?.let { Criteria.where("ownerId").`is`(it) } ?: Criteria()
        val total = template.count(Query(filter), RestaurantDocument::class.java)

        val items = template.find(
            Query(filter).with(catalogOrder).skip(page.toLong() * size).limit(size),
            RestaurantDocument::class.java,
        ).map { it.toDomain() }
        return Paged(items, page, size, total)
    }

    override fun findAllOrdered(): List<Restaurant> =
        template.find(Query().with(catalogOrder), RestaurantDocument::class.java).map { it.toDomain() }

    override fun updateName(id: UUID, name: String): Restaurant? =
        template.findAndModify(
            Query(Criteria.where("_id").`is`(id)),
            Update().set("name", name),
            FindAndModifyOptions.options().returnNew(true),
            RestaurantDocument::class.java,
        )?.toDomain()

    override fun findIdsByOwner(ownerId: UUID): List<UUID> =
        template.find(
            Query(Criteria.where("ownerId").`is`(ownerId)).with(catalogOrder),
            RestaurantDocument::class.java,
        ).map { it.id }

    private fun RestaurantDocument.toDomain() = Restaurant(
        restaurantId = id,
        ownerId = ownerId,
        name = name,
        createdAt = createdAt,
    )
}
