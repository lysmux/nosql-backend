package ru.itmo.delivery.restaurants.internal.adapters.inbound.rest

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper

@Component
class MenuPatchBodyCachingFilter : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath.orEmpty() + request.pathInfo.orEmpty()
        return request.method != HttpMethod.PATCH.name() || !matcher.match(MENU_ITEM_PATH, path)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        filterChain.doFilter(ContentCachingRequestWrapper(request, MAX_BODY_BYTES), response)
    }

    private companion object {
        const val MAX_BODY_BYTES = 64 * 1024
        const val MENU_ITEM_PATH = "/restaurants/*/menu-items/*"
        val matcher = AntPathMatcher()
    }
}
