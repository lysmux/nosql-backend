package ru.itmo.delivery.restaurants.api

import ru.itmo.delivery.restaurants.api.models.Restaurant
import ru.itmo.delivery.shared.models.Paged
import java.util.UUID

interface Restaurants {
    fun create(ownerId: UUID, name: String): Restaurant

    fun get(id: UUID): Restaurant

    fun requireOwned(ownerId: UUID, restaurantId: UUID): Restaurant

    fun list(ownerId: UUID?, page: Int, size: Int): Paged<Restaurant>

    fun update(ownerId: UUID, restaurantId: UUID, name: String): Restaurant

    fun registerVisit(restaurantId: UUID): Long

    fun visits(restaurantId: UUID): Long
}
