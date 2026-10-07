package ru.itmo.delivery.auth.internal.adapters.inbound.rest

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import ru.itmo.delivery.auth.api.Sessions
import ru.itmo.delivery.auth.api.sessionToken

@Component
class SessionFilter(
    private val sessions: Sessions,
    private val sessionContext: SessionContext,
    @Qualifier("handlerExceptionResolver") private val exceptionResolver: HandlerExceptionResolver,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (request.method == HttpMethod.OPTIONS.name()) {
            filterChain.doFilter(request, response)
            return
        }
        try {
            sessionContext.userId = resolveUser(request)
        } catch (exception: Exception) {
            exceptionResolver.resolveException(request, response, null, exception)
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveUser(request: HttpServletRequest) =
        try {
            sessions.resolve(request.sessionToken())
        } catch (exception: Exception) {
            if (!isPublic(request)) throw exception
            null
        }

    private fun isPublic(request: HttpServletRequest): Boolean {
        val path = request.servletPath.orEmpty() + request.pathInfo.orEmpty()
        val method = if (request.method == HttpMethod.HEAD.name()) HttpMethod.GET.name() else request.method

        return PUBLIC_ENDPOINTS.any { (allowedMethod, pattern) ->
            allowedMethod.name() == method && PATH_MATCHER.match(pattern, path)
        }
    }

    private companion object {
        val PATH_MATCHER = AntPathMatcher()
        val PUBLIC_ENDPOINTS = listOf(
            HttpMethod.POST to "/auth/register",
            HttpMethod.POST to "/auth/login",
            HttpMethod.GET to "/error",
            HttpMethod.GET to "/restaurants",
            HttpMethod.GET to "/restaurants/*",
            HttpMethod.GET to "/restaurants/*/menu-items",
            HttpMethod.POST to "/restaurants/*/visits",
            HttpMethod.GET to "/restaurants/*/visits",
        )
    }
}
