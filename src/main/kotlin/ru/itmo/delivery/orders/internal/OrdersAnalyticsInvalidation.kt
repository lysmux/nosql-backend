package ru.itmo.delivery.orders.internal

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.itmo.delivery.orders.api.events.OrderStatusChanged
import ru.itmo.delivery.orders.internal.adapters.outbound.cache.OrdersAnalyticsCache
import ru.itmo.delivery.restaurants.api.Restaurants

@Component
class OrdersAnalyticsInvalidation(
    private val cache: OrdersAnalyticsCache,
    private val restaurants: Restaurants,
) {
    @TransactionalEventListener(fallbackExecution = true)
    fun onOrderChanged(event: OrderStatusChanged) {
        cache.invalidate(restaurants.get(event.order.restaurantId).ownerId)
    }
}
