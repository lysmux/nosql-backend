package ru.itmo.delivery.menu.internal

import org.springframework.stereotype.Service
import ru.itmo.delivery.auth.api.exceptions.SessionExpiredException
import ru.itmo.delivery.menu.api.Menu
import ru.itmo.delivery.menu.api.exceptions.MenuItemNotFoundException
import ru.itmo.delivery.menu.api.models.MenuItem
import ru.itmo.delivery.menu.api.models.MenuItemPatch
import ru.itmo.delivery.menu.api.models.NewMenuItem
import ru.itmo.delivery.menu.api.models.OrderMenuItem
import ru.itmo.delivery.menu.internal.adapters.outbound.cache.MenuCache
import ru.itmo.delivery.menu.internal.adapters.outbound.dao.MenuDAO
import ru.itmo.delivery.restaurants.api.Restaurants
import ru.itmo.delivery.shared.exceptions.ValidationException
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class MenuService(
    private val dao: MenuDAO,
    private val cache: MenuCache,
    private val restaurants: Restaurants,
) : Menu {
    override fun list(restaurantId: UUID, includeUnavailable: Boolean, requesterId: UUID?): List<MenuItem> {
        if (includeUnavailable) {
            restaurants.requireOwned(requesterId ?: throw SessionExpiredException(), restaurantId)
        } else {
            restaurants.get(restaurantId)
        }

        if (includeUnavailable) return dao.findAll(restaurantId, onlyAvailable = false)
        return cache.get(restaurantId)
            ?: dao.findAll(restaurantId, onlyAvailable = true).also { cache.put(restaurantId, it) }
    }

    override fun create(ownerId: UUID, restaurantId: UUID, request: NewMenuItem): MenuItem {
        restaurants.requireOwned(ownerId, restaurantId)

        val timestamp = now()
        val item = MenuItem(
            menuItemId = UUID.randomUUID(),
            restaurantId = restaurantId,
            name = checkName(request.name),
            description = checkDescription(request.description),
            price = checkPrice(request.price),
            available = request.available,
            createdAt = timestamp,
            updatedAt = timestamp,
        )

        dao.save(item)
        cache.invalidate(restaurantId)
        return item
    }

    override fun update(ownerId: UUID, restaurantId: UUID, menuItemId: UUID, patch: MenuItemPatch): MenuItem {
        restaurants.requireOwned(ownerId, restaurantId)
        if (patch.isEmpty()) throw ValidationException("Изменение не содержит полей")

        val current = dao.findActive(restaurantId, menuItemId) ?: throw MenuItemNotFoundException(menuItemId)
        val updated = current.copy(
            name = patch.name?.let(::checkName) ?: current.name,
            description = patchedDescription(current.description, patch),
            price = patch.price?.let(::checkPrice) ?: current.price,
            available = patch.available ?: current.available,
            updatedAt = now(),
        )

        if (!dao.updateActive(updated)) throw MenuItemNotFoundException(menuItemId)
        cache.invalidate(restaurantId)
        return updated
    }

    override fun archive(ownerId: UUID, restaurantId: UUID, menuItemId: UUID) {
        restaurants.requireOwned(ownerId, restaurantId)
        if (!dao.archive(restaurantId, menuItemId, now())) throw MenuItemNotFoundException(menuItemId)
        cache.invalidate(restaurantId)
    }

    override fun lookupForOrder(menuItemIds: Collection<UUID>): Map<UUID, OrderMenuItem> =
        if (menuItemIds.isEmpty()) emptyMap() else dao.findForOrder(menuItemIds).associateBy { it.menuItemId }

    private fun patchedDescription(current: String?, patch: MenuItemPatch): String? =
        if (patch.descriptionPresent) checkDescription(patch.description) else current

    private fun now(): Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

    private fun checkName(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > MAX_NAME_LENGTH) {
            throw ValidationException("Название должно быть от 1 до $MAX_NAME_LENGTH символов")
        }

        return trimmed
    }

    private fun checkDescription(description: String?): String? {
        if (description != null && description.length > MAX_DESCRIPTION_LENGTH) {
            throw ValidationException("Описание не должно быть длиннее $MAX_DESCRIPTION_LENGTH символов")
        }
        return description
    }

    private fun checkPrice(price: BigDecimal): BigDecimal {
        if (price.signum() < 0 || price > MAX_PRICE || price.stripTrailingZeros().scale() > PRICE_SCALE) {
            throw ValidationException("Цена должна быть от 0 до $MAX_PRICE с точностью до $PRICE_SCALE знаков")
        }
        return price.setScale(PRICE_SCALE)
    }

    private companion object {
        const val MAX_NAME_LENGTH = 200
        const val MAX_DESCRIPTION_LENGTH = 2000
        const val PRICE_SCALE = 2
        val MAX_PRICE = BigDecimal("999999999.99")
    }
}
