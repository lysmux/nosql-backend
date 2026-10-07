package ru.itmo.delivery.orders.api.exceptions

import ru.itmo.delivery.shared.exceptions.NotFoundException
import java.util.UUID

class OrderNotFoundException(id: UUID) : NotFoundException("Заказ $id не найден")
