package ru.itmo.delivery.orders.api

import ru.itmo.delivery.orders.api.models.RestaurantOrderStats
import java.util.UUID

interface OrdersAnalytics {
    fun ordersByRestaurant(ownerId: UUID): List<RestaurantOrderStats>
}
