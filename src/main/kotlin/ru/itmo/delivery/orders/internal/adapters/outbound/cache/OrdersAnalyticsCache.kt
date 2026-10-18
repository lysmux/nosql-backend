package ru.itmo.delivery.orders.internal.adapters.outbound.cache

import ru.itmo.delivery.orders.api.models.RestaurantOrderStats
import java.util.UUID

interface OrdersAnalyticsCache {
    fun get(ownerId: UUID): List<RestaurantOrderStats>?

    fun put(ownerId: UUID, stats: List<RestaurantOrderStats>)

    fun invalidate(ownerId: UUID)
}
