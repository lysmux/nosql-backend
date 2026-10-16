package ru.itmo.delivery.orders.internal.adapters.outbound.dao

import ru.itmo.delivery.orders.api.models.RestaurantOrderStats
import java.util.UUID

interface OrdersAnalyticsDAO {
    fun statsByRestaurant(restaurantIds: Collection<UUID>): List<RestaurantOrderStats>
}
