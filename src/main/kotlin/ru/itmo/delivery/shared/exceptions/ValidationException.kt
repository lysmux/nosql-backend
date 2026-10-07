package ru.itmo.delivery.shared.exceptions

open class ValidationException(message: String, code: String = "validation_failed") : AppException(message, code)
