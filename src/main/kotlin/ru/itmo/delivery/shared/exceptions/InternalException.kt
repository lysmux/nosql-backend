package ru.itmo.delivery.shared.exceptions

open class InternalException(message: String, code: String = "internal_error") : AppException(message, code)
