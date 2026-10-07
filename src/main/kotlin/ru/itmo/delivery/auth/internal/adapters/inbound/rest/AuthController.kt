package ru.itmo.delivery.auth.internal.adapters.inbound.rest

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import ru.itmo.delivery.auth.api.Sessions
import ru.itmo.delivery.auth.api.sessionToken
import ru.itmo.delivery.entrypoint.api.AuthApi
import ru.itmo.delivery.entrypoint.api.model.LoginRequestDto
import ru.itmo.delivery.entrypoint.api.model.RegisterRequestDto
import ru.itmo.delivery.entrypoint.api.model.SessionDto
import ru.itmo.delivery.users.api.Users

@RestController
class AuthController(
    private val users: Users,
    private val sessions: Sessions,
    private val request: HttpServletRequest,
) : AuthApi {
    override fun authRegister(registerRequestDto: RegisterRequestDto): ResponseEntity<Unit> {
        users.register(registerRequestDto.name, registerRequestDto.email, registerRequestDto.password)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    override fun authLogin(loginRequestDto: LoginRequestDto): ResponseEntity<SessionDto> {
        val userId = users.authenticate(loginRequestDto.email, loginRequestDto.password)
        val session = sessions.create(userId)
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, sessionCookie(session.token, request.isSecure).toString())
            .body(session.toDto())
    }

    override fun authLogout(): ResponseEntity<Unit> {
        sessions.logout(request.sessionToken())
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, expiredSessionCookie(request.isSecure).toString())
            .build()
    }
}
