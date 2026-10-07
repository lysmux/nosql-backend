package ru.itmo.delivery.shared.rest.interceptor

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import ru.itmo.delivery.entrypoint.api.model.ErrorBodyDto
import ru.itmo.delivery.entrypoint.api.model.ErrorCodeDto

fun errorResponse(status: HttpStatus, code: ErrorCodeDto, message: String?): ResponseEntity<ErrorBodyDto> =
    ResponseEntity.status(status).body(ErrorBodyDto(code, message.orEmpty()))
