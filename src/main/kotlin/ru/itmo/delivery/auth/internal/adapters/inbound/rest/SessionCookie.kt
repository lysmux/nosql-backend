package ru.itmo.delivery.auth.internal.adapters.inbound.rest

import org.springframework.http.ResponseCookie
import ru.itmo.delivery.auth.api.SESSION_COOKIE
import java.time.Duration

fun sessionCookie(token: String, secure: Boolean): ResponseCookie = sessionCookieBuilder(token, secure).build()

fun expiredSessionCookie(secure: Boolean): ResponseCookie =
    sessionCookieBuilder("", secure).maxAge(Duration.ZERO).build()

private fun sessionCookieBuilder(value: String, secure: Boolean) =
    ResponseCookie.from(SESSION_COOKIE, value)
        .path("/")
        .httpOnly(true)
        .sameSite("Lax")
        .secure(secure)
