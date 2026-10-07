package ru.itmo.delivery.users.api.exceptions

import ru.itmo.delivery.shared.exceptions.ConflictException

class CourierModeConflictException : ConflictException("Нельзя отключить режим курьера при активной доставке")
