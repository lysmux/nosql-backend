package ru.itmo.delivery.menu.api.exceptions

import ru.itmo.delivery.shared.exceptions.NotFoundException
import java.util.UUID

class MenuItemNotFoundException(id: UUID) : NotFoundException("Позиция меню $id не найдена")
