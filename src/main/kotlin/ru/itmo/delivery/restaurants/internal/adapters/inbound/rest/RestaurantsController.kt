package ru.itmo.delivery.restaurants.internal.adapters.inbound.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import ru.itmo.delivery.auth.api.currentUserId
import ru.itmo.delivery.auth.api.optionalUserId
import ru.itmo.delivery.entrypoint.api.RestaurantsApi
import ru.itmo.delivery.entrypoint.api.model.CreateMenuItemRequestDto
import ru.itmo.delivery.entrypoint.api.model.CreateRestaurantRequestDto
import ru.itmo.delivery.entrypoint.api.model.MenuItemDto
import ru.itmo.delivery.entrypoint.api.model.RestaurantDto
import ru.itmo.delivery.entrypoint.api.model.RestaurantPageDto
import ru.itmo.delivery.entrypoint.api.model.RestaurantVisitCounterDto
import ru.itmo.delivery.entrypoint.api.model.UpdateMenuItemRequestDto
import ru.itmo.delivery.entrypoint.api.model.UpdateRestaurantRequestDto
import ru.itmo.delivery.menu.api.Menu
import ru.itmo.delivery.restaurants.api.Restaurants
import tools.jackson.databind.json.JsonMapper
import java.util.UUID

@RestController
class RestaurantsController(
    private val restaurants: Restaurants,
    private val menu: Menu,
    private val jsonMapper: JsonMapper,
) : RestaurantsApi {
    override fun restaurantsList(mine: Boolean?, page: Int, size: Int): ResponseEntity<RestaurantPageDto> {
        val ownerId = if (mine == true) currentUserId() else null
        return ResponseEntity.ok(restaurants.list(ownerId, page, size).toPageDto())
    }

    override fun restaurantsCreate(
        createRestaurantRequestDto: CreateRestaurantRequestDto,
    ): ResponseEntity<RestaurantDto> {
        val restaurant = restaurants.create(currentUserId(), createRestaurantRequestDto.name)
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurant.toDto())
    }

    override fun restaurantsRead(restaurantId: UUID): ResponseEntity<RestaurantDto> =
        ResponseEntity.ok(restaurants.get(restaurantId).toDto())

    override fun restaurantsUpdate(
        restaurantId: UUID,
        updateRestaurantRequestDto: UpdateRestaurantRequestDto,
    ): ResponseEntity<RestaurantDto> =
        ResponseEntity.ok(
            restaurants.update(currentUserId(), restaurantId, updateRestaurantRequestDto.name).toDto(),
        )

    override fun restaurantsRegisterVisit(restaurantId: UUID): ResponseEntity<RestaurantVisitCounterDto> =
        ResponseEntity.ok(RestaurantVisitCounterDto(restaurantId, restaurants.registerVisit(restaurantId)))

    override fun restaurantsReadVisits(restaurantId: UUID): ResponseEntity<RestaurantVisitCounterDto> =
        ResponseEntity.ok(RestaurantVisitCounterDto(restaurantId, restaurants.visits(restaurantId)))

    override fun menuList(restaurantId: UUID, includeUnavailable: Boolean?): ResponseEntity<List<MenuItemDto>> {
        val items = menu.list(restaurantId, includeUnavailable == true, optionalUserId())
        return ResponseEntity.ok(items.map { it.toDto() })
    }

    override fun menuCreate(
        restaurantId: UUID,
        createMenuItemRequestDto: CreateMenuItemRequestDto,
    ): ResponseEntity<MenuItemDto> {
        val item = menu.create(currentUserId(), restaurantId, createMenuItemRequestDto.toNewMenuItem())
        return ResponseEntity.status(HttpStatus.CREATED).body(item.toDto())
    }

    override fun menuUpdate(
        restaurantId: UUID,
        menuItemId: UUID,
        updateMenuItemRequestDto: UpdateMenuItemRequestDto,
    ): ResponseEntity<MenuItemDto> {
        val descriptionPresent = jsonMapper.hasDescriptionField()
        val patch = updateMenuItemRequestDto.toPatch(descriptionPresent)
        return ResponseEntity.ok(menu.update(currentUserId(), restaurantId, menuItemId, patch).toDto())
    }

    override fun menuRemove(restaurantId: UUID, menuItemId: UUID): ResponseEntity<Unit> {
        menu.archive(currentUserId(), restaurantId, menuItemId)
        return ResponseEntity.noContent().build()
    }
}
