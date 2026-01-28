package com.examloop.shared.api

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * CorrelationIdFilter — Ajoute un ID de corrélation à chaque requête
 * 
 * - Lit X-Request-Id si présent, sinon génère un UUID
 * - Stocke dans MDC pour les logs
 * - Retourne dans X-Correlation-Id
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(CorrelationIdFilter::class.java)

    companion object {
        const val REQUEST_ID_HEADER = "X-Request-Id"
        const val CORRELATION_ID_HEADER = "X-Correlation-Id"
        const val MDC_KEY = "correlationId"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val correlationId = request.getHeader(REQUEST_ID_HEADER)
            ?: UUID.randomUUID().toString().take(8)

        try {
            MDC.put(MDC_KEY, correlationId)
            response.setHeader(CORRELATION_ID_HEADER, correlationId)

            log.debug("→ {} {} [correlationId={}]", 
                request.method, request.requestURI, correlationId)

            filterChain.doFilter(request, response)

            log.debug("← {} {} → {} [correlationId={}]",
                request.method, request.requestURI, response.status, correlationId)

        } finally {
            MDC.remove(MDC_KEY)
        }
    }
}
