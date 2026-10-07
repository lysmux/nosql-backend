package ru.itmo.delivery.restaurants.internal.adapters.outbound.cache.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import ru.itmo.delivery.restaurants.api.models.Restaurant
import ru.itmo.delivery.restaurants.internal.adapters.outbound.cache.RestaurantCatalogCache
import ru.itmo.delivery.restaurants.internal.config.CatalogCacheConfig
import ru.itmo.delivery.shared.riak.RiakTemplate
import java.time.Instant

data class CachedCatalog(
    val payload: List<Restaurant>,
    val cachedAt: Instant,
)

@Repository
class RestaurantCatalogRiakCache(
    private val riak: RiakTemplate,
    private val config: CatalogCacheConfig,
) : RestaurantCatalogCache {
    override fun getAll(): List<Restaurant>? =
        try {
            val cached = riak.get(BUCKET, KEY, CachedCatalog::class.java)
            when {
                cached == null -> null
                cached.cachedAt.plus(config.ttl).isBefore(Instant.now()) -> null.also { invalidate() }
                else -> cached.payload
            }
        } catch (exception: Exception) {
            log.warn("Catalog cache read failed, falling back to the database", exception)
            null
        }

    override fun put(restaurants: List<Restaurant>) {
        try {
            riak.put(BUCKET, KEY, CachedCatalog(restaurants, Instant.now()))
        } catch (exception: Exception) {
            log.warn("Catalog cache write failed", exception)
        }
    }

    override fun invalidate() {
        try {
            riak.delete(BUCKET, KEY)
        } catch (exception: Exception) {
            log.warn("Catalog cache invalidation failed", exception)
        }
    }

    private companion object {
        const val BUCKET = "catalog_cache"
        const val KEY = "all"
        private val log = LoggerFactory.getLogger(RestaurantCatalogRiakCache::class.java)
    }
}
