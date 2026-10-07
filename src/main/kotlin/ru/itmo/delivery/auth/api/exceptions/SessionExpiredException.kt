package ru.itmo.delivery.auth.api.exceptions

import ru.itmo.delivery.shared.exceptions.UnauthorizedException

class SessionExpiredException : UnauthorizedException("Сессия отсутствует или истекла", "session_expired")
