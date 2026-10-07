package ru.itmo.delivery.menu.api

import ru.itmo.delivery.menu.api.models.MenuItem
import ru.itmo.delivery.menu.api.models.MenuItemPatch
import ru.itmo.delivery.menu.api.models.NewMenuItem
import ru.itmo.delivery.menu.api.models.OrderMenuItem
import java.util.UUID

interface Menu {
    fun list(restaurantId: UUID, includeUnavailable: Boolean, requesterId: UUID?): List<MenuItem>

    fun create(ownerId: UUID, restaurantId: UUID, request: NewMenuItem): MenuItem

    fun update(ownerId: UUID, restaurantId: UUID, menuItemId: UUID, patch: MenuItemPatch): MenuItem

    fun archive(ownerId: UUID, restaurantId: UUID, menuItemId: UUID)

    fun lookupForOrder(menuItemIds: Collection<UUID>): Map<UUID, OrderMenuItem>
}
