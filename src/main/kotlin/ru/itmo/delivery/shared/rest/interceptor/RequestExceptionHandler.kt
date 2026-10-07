package ru.itmo.delivery.shared.rest.interceptor

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import ru.itmo.delivery.entrypoint.api.model.ErrorBodyDto
import ru.itmo.delivery.entrypoint.api.model.ErrorCodeDto

@RestControllerAdvice
class RequestExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(): ResponseEntity<ErrorBodyDto> =
        badRequest("Тело запроса не соответствует контракту")

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleParameterMismatch(
        exception: MethodArgumentTypeMismatchException,
    ): ResponseEntity<ErrorBodyDto> =
        badRequest("Параметр ${exception.name} имеет недопустимое значение")

    private fun badRequest(message: String) =
        errorResponse(HttpStatus.BAD_REQUEST, ErrorCodeDto.VALIDATION_FAILED, message)
}
