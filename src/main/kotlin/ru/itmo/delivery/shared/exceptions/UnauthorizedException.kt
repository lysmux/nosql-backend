package ru.itmo.delivery.shared.exceptions

abstract class UnauthorizedException(message: String, code: String) : AppException(message, code)
