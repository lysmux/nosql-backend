package ru.itmo.delivery.auth.api

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import ru.itmo.delivery.auth.api.exceptions.SessionExpiredException
import java.util.UUID

const val SESSION_COOKIE = "session_token"

private const val SESSION_CONTEXT_ATTRIBUTE = "scopedTarget.sessionContext"

fun optionalUserId(): UUID? =
    RequestContextHolder.getRequestAttributes()
        ?.getAttribute(SESSION_CONTEXT_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST)
        .let { it as? CurrentUserHolder }
        ?.userId

fun currentUserId(): UUID = optionalUserId() ?: throw SessionExpiredException()

fun HttpServletRequest.sessionTokenOrNull(): String? =
    cookies?.firstOrNull { it.name == SESSION_COOKIE }?.value?.takeIf { it.isNotEmpty() }

fun HttpServletRequest.sessionToken(): String = sessionTokenOrNull() ?: throw SessionExpiredException()
