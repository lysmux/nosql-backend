package ru.itmo.delivery.shared.exceptions

open class ConflictException(message: String, code: String = "conflict") : AppException(message, code)
