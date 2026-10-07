package ru.itmo.delivery.restaurants.internal.adapters.outbound.dao

import ru.itmo.delivery.restaurants.api.models.Restaurant
import ru.itmo.delivery.shared.models.Paged
import java.util.UUID

interface RestaurantsDAO {
    fun save(restaurant: Restaurant)

    fun get(id: UUID): Restaurant?

    fun findPage(ownerId: UUID?, page: Int, size: Int): Paged<Restaurant>

    fun findAllOrdered(): List<Restaurant>

    fun updateName(id: UUID, name: String): Restaurant?
}
