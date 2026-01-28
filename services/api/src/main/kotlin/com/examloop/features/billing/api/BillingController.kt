package com.examloop.features.billing.api

import com.examloop.features.learning.api.ProblemDetail
import com.examloop.features.learning.application.ProfilePort
import com.examloop.features.learning.application.Profile
import com.examloop.features.learning.domain.UserId
import com.stripe.Stripe
import com.stripe.model.Event
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

/**
 * BillingController — Stripe Checkout + Webhook
 */
@RestController
@RequestMapping("/api/v1/billing")
class BillingController(
    private val profilePort: ProfilePort,
    @Value("\${stripe.api-key:}") private val stripeApiKey: String,
    @Value("\${stripe.webhook-secret:}") private val webhookSecret: String,
    @Value("\${stripe.price-id:}") private val priceId: String,
    @Value("\${examloop.base-url:http://localhost:8080}") private val baseUrl: String
) {
    private val log = LoggerFactory.getLogger(BillingController::class.java)

    init {
        if (stripeApiKey.isNotBlank()) {
            Stripe.apiKey = stripeApiKey
        }
    }

    /**
     * Crée une session Stripe Checkout pour l'abonnement Premium
     */
    @PostMapping("/checkout")
    fun createCheckoutSession(
        @RequestHeader("X-Device-Id") deviceId: String,
        @RequestBody(required = false) request: CheckoutRequest?
    ): ResponseEntity<*> {
        log.info("💳 POST /billing/checkout - deviceId: {}", deviceId)

        if (stripeApiKey.isBlank()) {
            log.warn("⚠️ Stripe not configured")
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                ProblemDetail(
                    title = "Stripe not configured",
                    status = 503,
                    detail = "Le paiement n'est pas encore configuré",
                    errorCode = "STRIPE_NOT_CONFIGURED"
                )
            )
        }

        // Vérifier si déjà premium
        val profile = profilePort.findById(UserId(deviceId))
        if (profile?.premium == true) {
            return ResponseEntity.badRequest().body(
                ProblemDetail(
                    title = "Already premium",
                    status = 400,
                    detail = "Vous êtes déjà abonné Premium",
                    errorCode = "ALREADY_PREMIUM"
                )
            )
        }

        try {
            val params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl("$baseUrl/billing/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("$baseUrl/billing/cancel")
                .putMetadata("device_id", deviceId)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1L)
                        .build()
                )
                .build()

            val session = Session.create(params)

            log.info("✅ Checkout session created: {}", session.id)

            return ResponseEntity.ok(
                CheckoutResponse(
                    sessionId = session.id,
                    url = session.url
                )
            )
        } catch (e: Exception) {
            log.error("❌ Failed to create checkout session: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ProblemDetail(
                    title = "Checkout failed",
                    status = 500,
                    detail = "Erreur lors de la création de la session de paiement",
                    errorCode = "CHECKOUT_FAILED"
                )
            )
        }
    }

    /**
     * Webhook Stripe pour les événements de paiement
     */
    @PostMapping("/webhook")
    fun handleWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") signature: String
    ): ResponseEntity<*> {
        log.info("🔔 POST /billing/webhook")

        if (webhookSecret.isBlank()) {
            log.warn("⚠️ Webhook secret not configured")
            return ResponseEntity.badRequest().body("Webhook not configured")
        }

        val event: Event = try {
            Webhook.constructEvent(payload, signature, webhookSecret)
        } catch (e: Exception) {
            log.error("❌ Webhook signature verification failed: {}", e.message)
            return ResponseEntity.badRequest().body("Invalid signature")
        }

        log.info("📩 Webhook event: {}", event.type)

        when (event.type) {
            "checkout.session.completed" -> {
                val session = event.dataObjectDeserializer.`object`.orElse(null) as? Session
                if (session != null) {
                    handleCheckoutCompleted(session)
                }
            }
            "customer.subscription.deleted" -> {
                // Gérer l'annulation d'abonnement
                log.info("📉 Subscription cancelled")
                // TODO: Mettre premium = false
            }
            else -> {
                log.info("ℹ️ Unhandled event type: {}", event.type)
            }
        }

        return ResponseEntity.ok().body(mapOf("received" to true))
    }

    private fun handleCheckoutCompleted(session: Session) {
        val deviceId = session.metadata?.get("device_id")
        if (deviceId == null) {
            log.error("❌ No device_id in session metadata")
            return
        }

        log.info("✅ Checkout completed for device: {}", deviceId)

        // Mettre à jour le profil en premium
        val userId = UserId(deviceId)
        val existingProfile = profilePort.findById(userId)
        
        val updatedProfile = if (existingProfile != null) {
            Profile(
                userId = userId,
                premium = true,
                createdAt = existingProfile.createdAt
            )
        } else {
            Profile(
                userId = userId,
                premium = true,
                createdAt = Instant.now()
            )
        }

        profilePort.save(updatedProfile)
        log.info("🎉 User {} upgraded to Premium!", deviceId)
    }

    /**
     * Vérifie le statut premium de l'utilisateur
     */
    @GetMapping("/status")
    fun getStatus(
        @RequestHeader("X-Device-Id") deviceId: String
    ): ResponseEntity<BillingStatusResponse> {
        log.info("📊 GET /billing/status - deviceId: {}", deviceId)

        val profile = profilePort.findById(UserId(deviceId))

        return ResponseEntity.ok(
            BillingStatusResponse(
                premium = profile?.premium ?: false,
                features = if (profile?.premium == true) {
                    listOf(
                        "unlimited_reviews",
                        "ai_question_generation",
                        "priority_support",
                        "export_data"
                    )
                } else {
                    listOf("daily_reviews_20")
                }
            )
        )
    }
}

// ============================================================================
// DTOs
// ============================================================================

data class CheckoutRequest(
    val plan: String? = "premium_monthly"
)

data class CheckoutResponse(
    val sessionId: String,
    val url: String
)

data class BillingStatusResponse(
    val premium: Boolean,
    val features: List<String>
)
