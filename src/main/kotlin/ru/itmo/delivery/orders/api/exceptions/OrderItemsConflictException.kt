package ru.itmo.delivery.orders.api.exceptions

import ru.itmo.delivery.shared.exceptions.ConflictException

class OrderItemsConflictException(message: String) : ConflictException(message)
