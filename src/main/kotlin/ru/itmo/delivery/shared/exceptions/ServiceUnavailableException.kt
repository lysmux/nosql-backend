package ru.itmo.delivery.shared.exceptions

open class ServiceUnavailableException(
    message: String,
    code: String = "service_unavailable",
) : AppException(message, code)
