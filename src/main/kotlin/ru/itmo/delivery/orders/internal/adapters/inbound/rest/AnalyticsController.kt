package ru.itmo.delivery.orders.internal.adapters.inbound.rest

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import ru.itmo.delivery.auth.api.currentUserId
import ru.itmo.delivery.entrypoint.api.AnalyticsApi
import ru.itmo.delivery.entrypoint.api.model.OrdersByRestaurantDto
import ru.itmo.delivery.entrypoint.api.model.RestaurantOrderStatsDto
import ru.itmo.delivery.orders.api.OrdersAnalytics

@RestController
class AnalyticsController(
    private val analytics: OrdersAnalytics,
) : AnalyticsApi {
    override fun analyticsOrdersByRestaurant(): ResponseEntity<OrdersByRestaurantDto> =
        ResponseEntity.ok(
            OrdersByRestaurantDto(
                items = analytics.ordersByRestaurant(currentUserId()).map {
                    RestaurantOrderStatsDto(
                        restaurantId = it.restaurantId,
                        name = it.name,
                        total = it.total,
                        delivered = it.delivered,
                        cancelled = it.cancelled,
                        revenue = it.revenue,
                    )
                },
            ),
        )
}
