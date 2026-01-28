package com.examloop.shared.api

import com.examloop.features.learning.api.ProblemDetail
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.util.UUID

/**
 * GlobalExceptionHandler — Gestion centralisée des erreurs
 * 
 * Toutes les erreurs sont transformées en Problem Details (RFC 7807).
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        val correlationId = MDC.get("correlationId") ?: UUID.randomUUID().toString()
        
        log.warn("⚠️ IllegalArgumentException: {} [correlationId={}]", ex.message, correlationId)

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ProblemDetail(
                title = "Bad Request",
                status = 400,
                detail = ex.message ?: "Invalid request",
                errorCode = "VALIDATION_FAILED",
                correlationId = correlationId
            )
        )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(
        ex: IllegalStateException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        val correlationId = MDC.get("correlationId") ?: UUID.randomUUID().toString()
        
        log.error("❌ IllegalStateException: {} [correlationId={}]", ex.message, correlationId)

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ProblemDetail(
                title = "Conflict",
                status = 409,
                detail = ex.message ?: "Invalid state",
                errorCode = "INVALID_STATE",
                correlationId = correlationId
            )
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(
        ex: NoSuchElementException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        val correlationId = MDC.get("correlationId") ?: UUID.randomUUID().toString()
        
        log.warn("⚠️ NotFound: {} [correlationId={}]", ex.message, correlationId)

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ProblemDetail(
                title = "Not Found",
                status = 404,
                detail = ex.message ?: "Resource not found",
                errorCode = "NOT_FOUND",
                correlationId = correlationId
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        val correlationId = MDC.get("correlationId") ?: UUID.randomUUID().toString()
        
        log.error("❌ Unhandled exception: {} [correlationId={}]", ex.message, correlationId, ex)

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ProblemDetail(
                title = "Internal Server Error",
                status = 500,
                detail = "Une erreur inattendue s'est produite. Réessayez plus tard.",
                errorCode = "INTERNAL_ERROR",
                correlationId = correlationId
            )
        )
    }
}
