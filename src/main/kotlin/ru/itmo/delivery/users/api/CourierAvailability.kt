package ru.itmo.delivery.users.api

import java.util.UUID

interface CourierAvailability {
    fun existsActiveAssignment(courierId: UUID): Boolean
}
