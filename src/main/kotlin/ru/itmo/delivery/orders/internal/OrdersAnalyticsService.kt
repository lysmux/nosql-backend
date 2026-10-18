package ru.itmo.delivery.orders.internal

import org.springframework.stereotype.Service
import ru.itmo.delivery.orders.api.OrdersAnalytics
import ru.itmo.delivery.orders.api.models.RestaurantOrderStats
import ru.itmo.delivery.orders.internal.adapters.outbound.cache.OrdersAnalyticsCache
import ru.itmo.delivery.orders.internal.adapters.outbound.dao.OrdersAnalyticsDAO
import ru.itmo.delivery.restaurants.api.Restaurants
import java.util.UUID

@Service
class OrdersAnalyticsService(
    private val dao: OrdersAnalyticsDAO,
    private val cache: OrdersAnalyticsCache,
    private val restaurants: Restaurants,
) : OrdersAnalytics {
    override fun ordersByRestaurant(ownerId: UUID): List<RestaurantOrderStats> =
        cache.get(ownerId) ?: dao.statsByRestaurant(restaurants.ownedIds(ownerId)).also { cache.put(ownerId, it) }
}
