package ru.itmo.delivery.restaurants.api.exceptions

import ru.itmo.delivery.shared.exceptions.NotFoundException
import java.util.UUID

class RestaurantNotFoundException(id: UUID) : NotFoundException("Ресторан $id не найден")
