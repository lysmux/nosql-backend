package ru.itmo.delivery.menu.internal.adapters.outbound.cache

import ru.itmo.delivery.menu.api.models.MenuItem
import java.util.UUID

interface MenuCache {
    fun get(restaurantId: UUID): List<MenuItem>?

    fun put(restaurantId: UUID, items: List<MenuItem>)

    fun invalidate(restaurantId: UUID)
}
