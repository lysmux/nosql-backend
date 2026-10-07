package ru.itmo.delivery.restaurants.internal.adapters.inbound.rest

import ru.itmo.delivery.entrypoint.api.model.RestaurantDto
import ru.itmo.delivery.entrypoint.api.model.RestaurantPageDto
import ru.itmo.delivery.restaurants.api.models.Restaurant
import ru.itmo.delivery.shared.models.Paged

fun Restaurant.toDto() = RestaurantDto(
    restaurantId = restaurantId,
    name = name,
    createdAt = createdAt,
)

fun Paged<Restaurant>.toPageDto() = RestaurantPageDto(
    items = items.map { it.toDto() },
    page = page,
    pageSize = size,
    totalItems = totalItems,
    totalPages = totalPages,
)
