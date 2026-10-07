package ru.itmo.delivery.restaurants.internal.adapters.outbound.cache

import ru.itmo.delivery.restaurants.api.models.Restaurant

interface RestaurantCatalogCache {
    fun getAll(): List<Restaurant>?

    fun put(restaurants: List<Restaurant>)

    fun invalidate()
}
