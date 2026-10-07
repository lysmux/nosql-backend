package ru.itmo.delivery.menu.internal.adapters.outbound.dao.impl

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.itmo.delivery.menu.api.models.MenuItem
import ru.itmo.delivery.menu.api.models.OrderMenuItem
import ru.itmo.delivery.menu.internal.adapters.outbound.dao.MenuDAO
import java.time.Instant
import java.util.UUID

@Repository
class MenuDefaultDao(
    private val items: MenuItemJpaRepository,
) : MenuDAO {
    override fun save(item: MenuItem) {
        items.save(
            MenuItemEntity(
                menuItemId = item.menuItemId,
                restaurantId = item.restaurantId,
                name = item.name,
                description = item.description,
                price = item.price,
                available = item.available,
                archived = false,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt,
            ),
        )
    }

    @Transactional
    override fun updateActive(item: MenuItem): Boolean =
        items.updateActive(
            item.restaurantId,
            item.menuItemId,
            item.name,
            item.description,
            item.price,
            item.available,
            item.updatedAt,
        ) > 0

    override fun findActive(restaurantId: UUID, menuItemId: UUID): MenuItem? =
        items.findActive(restaurantId, menuItemId)?.toMenuItem()

    override fun findAll(restaurantId: UUID, onlyAvailable: Boolean): List<MenuItem> =
        items.findListed(restaurantId, onlyAvailable).map { it.toMenuItem() }

    override fun findForOrder(menuItemIds: Collection<UUID>): List<OrderMenuItem> =
        items.findAllByIds(menuItemIds).map {
            OrderMenuItem(
                menuItemId = it.menuItemId,
                restaurantId = it.restaurantId,
                name = it.name,
                price = it.price,
                available = it.available,
                archived = it.archived,
            )
        }

    @Transactional
    override fun archive(restaurantId: UUID, menuItemId: UUID, now: Instant): Boolean =
        items.markArchived(restaurantId, menuItemId, now) > 0

    private fun MenuItemEntity.toMenuItem() = MenuItem(
        menuItemId = menuItemId,
        restaurantId = restaurantId,
        name = name,
        description = description,
        price = price,
        available = available,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
