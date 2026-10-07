package ru.itmo.delivery.users.internal.adapters.inbound.rest

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import ru.itmo.delivery.auth.api.currentUserId
import ru.itmo.delivery.entrypoint.api.UsersApi
import ru.itmo.delivery.entrypoint.api.model.CourierModeRequestDto
import ru.itmo.delivery.entrypoint.api.model.UserDto
import ru.itmo.delivery.users.api.Users
import ru.itmo.delivery.users.api.exceptions.UserNotFoundException

@RestController
class UsersController(
    private val users: Users,
) : UsersApi {
    override fun usersMe(): ResponseEntity<UserDto> {
        val userId = currentUserId()
        val user = users.get(userId) ?: throw UserNotFoundException(userId)
        return ResponseEntity.ok(user.toDto())
    }

    override fun usersSetCourierMode(courierModeRequestDto: CourierModeRequestDto): ResponseEntity<UserDto> =
        ResponseEntity.ok(users.setCourierMode(currentUserId(), courierModeRequestDto.enabled).toDto())
}
