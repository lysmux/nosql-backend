package ru.itmo.delivery.restaurants.internal.adapters.inbound.rest

import ru.itmo.delivery.entrypoint.api.model.CreateMenuItemRequestDto
import ru.itmo.delivery.entrypoint.api.model.MenuItemDto
import ru.itmo.delivery.entrypoint.api.model.UpdateMenuItemRequestDto
import ru.itmo.delivery.menu.api.models.MenuItem
import ru.itmo.delivery.menu.api.models.MenuItemPatch
import ru.itmo.delivery.menu.api.models.NewMenuItem

fun MenuItem.toDto() = MenuItemDto(
    menuItemId = menuItemId,
    restaurantId = restaurantId,
    name = name,
    price = price,
    available = available,
    createdAt = createdAt,
    updatedAt = updatedAt,
    description = description,
)

fun CreateMenuItemRequestDto.toNewMenuItem() = NewMenuItem(
    name = name,
    description = description,
    price = price,
    available = available ?: true,
)

fun UpdateMenuItemRequestDto.toPatch(descriptionPresent: Boolean) = MenuItemPatch(
    name = name,
    description = description,
    price = price,
    available = available,
    descriptionPresent = descriptionPresent,
)
