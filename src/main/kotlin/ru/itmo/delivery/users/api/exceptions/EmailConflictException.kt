package ru.itmo.delivery.users.api.exceptions

import ru.itmo.delivery.shared.exceptions.ConflictException

class EmailConflictException : ConflictException("Пользователь с таким email уже зарегистрирован")
