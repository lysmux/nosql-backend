package ru.itmo.delivery.shared.exceptions

open class ForbiddenException(message: String, code: String = "forbidden") : AppException(message, code)
