package ru.itmo.delivery.orders.api.exceptions

import ru.itmo.delivery.shared.exceptions.ConflictException

class OrderStatusConflictException(message: String) : ConflictException(message)
