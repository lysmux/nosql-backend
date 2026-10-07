package ru.itmo.delivery.auth.api

import java.util.UUID

interface CurrentUserHolder {
    var userId: UUID?
}
