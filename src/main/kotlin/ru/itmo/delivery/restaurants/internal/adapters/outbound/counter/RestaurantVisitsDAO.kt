package ru.itmo.delivery.restaurants.internal.adapters.outbound.counter

import java.util.UUID

interface RestaurantVisitsDAO {
    fun increment(restaurantId: UUID): Long

    fun get(restaurantId: UUID): Long
}
