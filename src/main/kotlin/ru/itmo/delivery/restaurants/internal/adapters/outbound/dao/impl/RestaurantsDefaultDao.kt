package ru.itmo.delivery.restaurants.internal.adapters.outbound.dao.impl

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.itmo.delivery.restaurants.api.models.Restaurant
import ru.itmo.delivery.restaurants.internal.adapters.outbound.dao.RestaurantsDAO
import ru.itmo.delivery.shared.models.Paged
import java.util.UUID

@Repository
class RestaurantsDefaultDao(
    private val restaurants: RestaurantJpaRepository,
) : RestaurantsDAO {
    override fun save(restaurant: Restaurant) {
        restaurants.save(
            RestaurantEntity(
                restaurantId = restaurant.restaurantId,
                ownerId = restaurant.ownerId,
                name = restaurant.name,
                createdAt = restaurant.createdAt,
            ),
        )
    }

    override fun get(id: UUID): Restaurant? = restaurants.findById(id).map { it.toRestaurant() }.orElse(null)

    override fun findPage(ownerId: UUID?, page: Int, size: Int): Paged<Restaurant> {
        val offset = page.toLong() * size
        val (entities, total) =
            if (ownerId == null) {
                restaurants.findAllPage(size, offset) to restaurants.countAll()
            } else {
                restaurants.findPageByOwner(ownerId, size, offset) to restaurants.countByOwner(ownerId)
            }
        return Paged(entities.map { it.toRestaurant() }, page, size, total)
    }

    override fun findAllOrdered(): List<Restaurant> = restaurants.findAllOrdered().map { it.toRestaurant() }

    @Transactional
    override fun updateName(id: UUID, name: String): Restaurant? =
        restaurants.findById(id).map { entity ->
            entity.name = name
            restaurants.save(entity).toRestaurant()
        }.orElse(null)

    private fun RestaurantEntity.toRestaurant() = Restaurant(
        restaurantId = restaurantId,
        ownerId = ownerId,
        name = name,
        createdAt = createdAt,
    )
}
