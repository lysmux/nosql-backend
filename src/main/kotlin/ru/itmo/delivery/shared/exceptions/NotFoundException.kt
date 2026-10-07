package ru.itmo.delivery.shared.exceptions

open class NotFoundException(message: String, code: String = "not_found") : AppException(message, code)
