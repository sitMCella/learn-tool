package de.mcella.spring.learntool.common

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [(IllegalArgumentException::class)])
    protected fun handleUnprocessableEntity(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        val body = "One or more parameters are incorrect."
        return handleExceptionInternal(ex, body, HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request)
    }
}
