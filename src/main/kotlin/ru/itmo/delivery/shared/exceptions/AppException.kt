package ru.itmo.delivery.shared.exceptions

sealed class AppException(message: String, val code: String) : RuntimeException(message)
