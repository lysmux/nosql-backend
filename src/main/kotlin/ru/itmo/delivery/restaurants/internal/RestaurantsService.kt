package ru.itmo.delivery.restaurants.internal

import org.springframework.stereotype.Service
import ru.itmo.delivery.restaurants.api.Restaurants
import ru.itmo.delivery.restaurants.api.exceptions.RestaurantNotFoundException
import ru.itmo.delivery.restaurants.api.models.Restaurant
import ru.itmo.delivery.restaurants.internal.adapters.outbound.cache.RestaurantCatalogCache
import ru.itmo.delivery.restaurants.internal.adapters.outbound.counter.RestaurantVisitsDAO
import ru.itmo.delivery.restaurants.internal.adapters.outbound.dao.RestaurantsDAO
import ru.itmo.delivery.shared.exceptions.ForbiddenException
import ru.itmo.delivery.shared.exceptions.ServiceUnavailableException
import ru.itmo.delivery.shared.exceptions.ValidationException
import ru.itmo.delivery.shared.models.Paged
import java.time.Instant
import java.util.UUID

@Service
class RestaurantsService(
    private val dao: RestaurantsDAO,
    private val visits: RestaurantVisitsDAO,
    private val catalogCache: RestaurantCatalogCache,
) : Restaurants {
    override fun create(ownerId: UUID, name: String): Restaurant {
        val restaurant = Restaurant(
            restaurantId = UUID.randomUUID(),
            ownerId = ownerId,
            name = checkName(name),
            createdAt = Instant.now(),
        )

        dao.save(restaurant)
        catalogCache.invalidate()
        return restaurant
    }

    override fun get(id: UUID): Restaurant = dao.get(id) ?: throw RestaurantNotFoundException(id)

    override fun requireOwned(ownerId: UUID, restaurantId: UUID): Restaurant {
        val restaurant = get(restaurantId)
        if (restaurant.ownerId != ownerId) throw ForbiddenException("Ресторан принадлежит другому пользователю")
        return restaurant
    }

    override fun list(ownerId: UUID?, page: Int, size: Int): Paged<Restaurant> {
        if (page < 0 || size !in 1..MAX_PAGE_SIZE) {
            throw ValidationException("page должен быть >= 0, size от 1 до $MAX_PAGE_SIZE")
        }

        if (ownerId != null) return dao.findPage(ownerId, page, size)
        return paginate(publicCatalog(), page, size)
    }

    override fun update(ownerId: UUID, restaurantId: UUID, name: String): Restaurant {
        requireOwned(ownerId, restaurantId)

        val updated = dao.updateName(restaurantId, checkName(name)) ?: throw RestaurantNotFoundException(restaurantId)

        catalogCache.invalidate()
        return updated
    }

    override fun registerVisit(restaurantId: UUID): Long = withVisitCounter(restaurantId) { visits.increment(it) }

    override fun visits(restaurantId: UUID): Long = withVisitCounter(restaurantId) { visits.get(it) }

    private fun withVisitCounter(restaurantId: UUID, operation: (UUID) -> Long): Long {
        get(restaurantId)

        return try {
            operation(restaurantId)
        } catch (exception: Exception) {
            throw ServiceUnavailableException("Хранилище счётчиков недоступно").apply { initCause(exception) }
        }
    }

    private fun publicCatalog(): List<Restaurant> =
        catalogCache.getAll() ?: dao.findAllOrdered().also(catalogCache::put)

    private fun paginate(all: List<Restaurant>, page: Int, size: Int): Paged<Restaurant> {
        val offset = minOf(page.toLong() * size, all.size.toLong()).toInt()
        return Paged(all.drop(offset).take(size), page, size, all.size.toLong())
    }

    private fun checkName(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > MAX_NAME_LENGTH) {
            throw ValidationException("Название должно быть от 1 до $MAX_NAME_LENGTH символов")
        }

        return trimmed
    }

    private companion object {
        const val MAX_NAME_LENGTH = 200
        const val MAX_PAGE_SIZE = 100
    }
}
