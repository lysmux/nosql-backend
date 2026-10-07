package ru.itmo.delivery.auth.api.exceptions

import ru.itmo.delivery.shared.exceptions.UnauthorizedException

class InvalidCredentialsException : UnauthorizedException("Неверный email или пароль", "invalid_credentials")
