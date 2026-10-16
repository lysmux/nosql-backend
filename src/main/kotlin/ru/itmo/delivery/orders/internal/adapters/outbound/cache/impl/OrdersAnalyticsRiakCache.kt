package ru.itmo.delivery.orders.internal.adapters.outbound.cache.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import ru.itmo.delivery.orders.api.models.RestaurantOrderStats
import ru.itmo.delivery.orders.internal.adapters.outbound.cache.OrdersAnalyticsCache
import ru.itmo.delivery.orders.internal.config.OrdersAnalyticsCacheConfig
import ru.itmo.delivery.shared.riak.RiakTemplate
import java.time.Instant
import java.util.UUID

data class CachedOrderStats(
    val payload: List<RestaurantOrderStats>,
    val cachedAt: Instant,
)

@Repository
class OrdersAnalyticsRiakCache(
    private val riak: RiakTemplate,
    private val config: OrdersAnalyticsCacheConfig,
) : OrdersAnalyticsCache {
    override fun get(ownerId: UUID): List<RestaurantOrderStats>? =
        try {
            val cached = riak.get(BUCKET, key(ownerId), CachedOrderStats::class.java)
            when {
                cached == null -> null
                cached.cachedAt.plus(config.ttl).isBefore(Instant.now()) -> null.also { invalidate(ownerId) }
                else -> cached.payload
            }
        } catch (exception: Exception) {
            log.warn("Analytics cache read failed, falling back to the database", exception)
            null
        }

    override fun put(ownerId: UUID, stats: List<RestaurantOrderStats>) {
        try {
            riak.put(BUCKET, key(ownerId), CachedOrderStats(stats, Instant.now()))
        } catch (exception: Exception) {
            log.warn("Analytics cache write failed", exception)
        }
    }

    override fun invalidate(ownerId: UUID) {
        try {
            riak.delete(BUCKET, key(ownerId))
        } catch (exception: Exception) {
            log.warn("Analytics cache invalidation failed", exception)
        }
    }

    private fun key(ownerId: UUID) = "orders-by-restaurant:$ownerId"

    private companion object {
        const val BUCKET = "analytics_cache"
        private val log = LoggerFactory.getLogger(OrdersAnalyticsRiakCache::class.java)
    }
}
