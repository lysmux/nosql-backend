package ru.itmo.delivery.shared.rest.interceptor

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.itmo.delivery.entrypoint.api.model.ErrorBodyDto
import ru.itmo.delivery.entrypoint.api.model.ErrorCodeDto
import ru.itmo.delivery.shared.exceptions.AppException
import ru.itmo.delivery.shared.exceptions.ConflictException
import ru.itmo.delivery.shared.exceptions.ForbiddenException
import ru.itmo.delivery.shared.exceptions.InternalException
import ru.itmo.delivery.shared.exceptions.NotFoundException
import ru.itmo.delivery.shared.exceptions.ServiceUnavailableException
import ru.itmo.delivery.shared.exceptions.UnauthorizedException
import ru.itmo.delivery.shared.exceptions.ValidationException

@RestControllerAdvice
class DomainExceptionHandler {
    @ExceptionHandler(AppException::class)
    fun handle(exception: AppException): ResponseEntity<ErrorBodyDto> =
        errorResponse(exception.httpStatus(), ErrorCodeDto.forValue(exception.code), exception.message)

    private fun AppException.httpStatus() = when (this) {
        is ValidationException -> HttpStatus.BAD_REQUEST
        is UnauthorizedException -> HttpStatus.UNAUTHORIZED
        is ForbiddenException -> HttpStatus.FORBIDDEN
        is NotFoundException -> HttpStatus.NOT_FOUND
        is ConflictException -> HttpStatus.CONFLICT
        is InternalException -> HttpStatus.INTERNAL_SERVER_ERROR
        is ServiceUnavailableException -> HttpStatus.SERVICE_UNAVAILABLE
    }
}
