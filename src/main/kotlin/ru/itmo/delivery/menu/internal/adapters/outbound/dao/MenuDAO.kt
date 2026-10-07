package ru.itmo.delivery.menu.internal.adapters.outbound.dao

import ru.itmo.delivery.menu.api.models.MenuItem
import ru.itmo.delivery.menu.api.models.OrderMenuItem
import java.time.Instant
import java.util.UUID

interface MenuDAO {
    fun save(item: MenuItem)

    fun updateActive(item: MenuItem): Boolean

    fun findActive(restaurantId: UUID, menuItemId: UUID): MenuItem?

    fun findAll(restaurantId: UUID, onlyAvailable: Boolean): List<MenuItem>

    fun findForOrder(menuItemIds: Collection<UUID>): List<OrderMenuItem>

    fun archive(restaurantId: UUID, menuItemId: UUID, now: Instant): Boolean
}
