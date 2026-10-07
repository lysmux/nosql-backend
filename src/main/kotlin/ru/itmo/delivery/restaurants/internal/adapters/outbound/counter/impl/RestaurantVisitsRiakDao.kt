package ru.itmo.delivery.restaurants.internal.adapters.outbound.counter.impl

import org.springframework.stereotype.Repository
import ru.itmo.delivery.restaurants.internal.adapters.outbound.counter.RestaurantVisitsDAO
import ru.itmo.delivery.shared.riak.RiakTemplate
import java.util.UUID

@Repository
class RestaurantVisitsRiakDao(
    private val riak: RiakTemplate,
) : RestaurantVisitsDAO {
    override fun increment(restaurantId: UUID): Long = riak.incrementCounter(BUCKET, restaurantId.toString())

    override fun get(restaurantId: UUID): Long = riak.fetchCounter(BUCKET, restaurantId.toString())

    private companion object {
        const val BUCKET = "restaurant_visits"
    }
}
