package ru.itmo.delivery.menu.internal.adapters.outbound.cache.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import ru.itmo.delivery.menu.api.models.MenuItem
import ru.itmo.delivery.menu.internal.adapters.outbound.cache.MenuCache
import ru.itmo.delivery.menu.internal.config.MenuCacheConfig
import ru.itmo.delivery.shared.riak.RiakTemplate
import java.time.Instant
import java.util.UUID

data class CachedMenu(
    val payload: List<MenuItem>,
    val cachedAt: Instant,
)

@Repository
class MenuRiakCache(
    private val riak: RiakTemplate,
    private val config: MenuCacheConfig,
) : MenuCache {
    override fun get(restaurantId: UUID): List<MenuItem>? =
        try {
            val cached = riak.get(BUCKET, restaurantId.toString(), CachedMenu::class.java)
            when {
                cached == null -> null
                cached.cachedAt.plus(config.ttl).isBefore(Instant.now()) -> null.also { invalidate(restaurantId) }
                else -> cached.payload
            }
        } catch (exception: Exception) {
            log.warn("Menu cache read failed, falling back to the database", exception)
            null
        }

    override fun put(restaurantId: UUID, items: List<MenuItem>) {
        try {
            riak.put(BUCKET, restaurantId.toString(), CachedMenu(items, Instant.now()))
        } catch (exception: Exception) {
            log.warn("Menu cache write failed", exception)
        }
    }

    override fun invalidate(restaurantId: UUID) {
        try {
            riak.delete(BUCKET, restaurantId.toString())
        } catch (exception: Exception) {
            log.warn("Menu cache invalidation failed", exception)
        }
    }

    private companion object {
        const val BUCKET = "menu_cache"
        private val log = LoggerFactory.getLogger(MenuRiakCache::class.java)
    }
}
