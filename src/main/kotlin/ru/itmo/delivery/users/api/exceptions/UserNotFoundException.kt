package ru.itmo.delivery.users.api.exceptions

import ru.itmo.delivery.shared.exceptions.NotFoundException
import java.util.UUID

class UserNotFoundException(id: UUID) : NotFoundException("Клиент $id не найден")
