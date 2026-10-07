package ru.itmo.delivery.restaurants.internal.adapters.inbound.rest

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.WebUtils
import tools.jackson.databind.json.JsonMapper

internal fun JsonMapper.hasDescriptionField(): Boolean = readTree(cachedRequestBody()).has(DESCRIPTION_FIELD)

private fun cachedRequestBody(): ByteArray {
    val request: HttpServletRequest =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
            ?: error("No current servlet request")
    val wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper::class.java)
        ?: error("Menu PATCH request body is not cached; MenuPatchBodyCachingFilter is not applied")
    return wrapper.contentAsByteArray
}

private const val DESCRIPTION_FIELD = "description"
