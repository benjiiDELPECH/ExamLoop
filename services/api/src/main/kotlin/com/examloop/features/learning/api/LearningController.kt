package com.examloop.features.learning.api

import com.examloop.features.learning.application.*
import com.examloop.features.learning.domain.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * LearningController — API REST pour l'apprentissage
 * 
 * Endpoints :
 * - POST /api/v1/bootstrap — Initialisation app
 * - POST /api/v1/sessions — Générer une session
 * - POST /api/v1/reviews — Soumettre une réponse
 * - GET /api/v1/goals — Liste des goals
 */
@RestController
@RequestMapping("/api/v1")
class LearningController(
    private val bootstrapUseCase: BootstrapUseCase,
    private val generateSessionUseCase: GenerateSessionUseCase,
    private val submitReviewUseCase: SubmitReviewUseCase
) {
    private val log = LoggerFactory.getLogger(LearningController::class.java)

    // ============================================================================
    // BOOTSTRAP
    // ============================================================================

    @PostMapping("/bootstrap")
    fun bootstrap(
        @RequestHeader("X-Device-Id") deviceId: String,
        @RequestBody(required = false) request: BootstrapRequest?
    ): ResponseEntity<BootstrapResponse> {
        log.info("📱 POST /bootstrap - deviceId: {}", deviceId)

        val command = BootstrapUseCase.Command(
            userId = UserId(deviceId)
        )

        return when (val result = bootstrapUseCase.execute(command)) {
            is BootstrapResult.Success -> {
                ResponseEntity.ok(ResponseMapper.toBootstrapResponse(result))
            }
        }
    }

    // ============================================================================
    // SESSIONS
    // ============================================================================

    @PostMapping("/sessions")
    fun generateSession(
        @RequestHeader("X-Device-Id") deviceId: String,
        @RequestBody request: GenerateSessionRequest
    ): ResponseEntity<*> {
        log.info("🎯 POST /sessions - deviceId: {}, goalId: {}, limit: {}",
            deviceId, request.goalId, request.limit)

        val command = GenerateSessionUseCase.Command(
            userId = UserId(deviceId),
            goalId = request.goalId?.let { GoalId(it) },
            maxQuestions = request.limit.coerceIn(1, 20),
            difficultyFilter = request.difficulty?.let { QuestionDifficulty.valueOf(it) },
            distributionStrategy = request.strategy?.let { DistributionStrategy.valueOf(it) }
        )

        return when (val result = generateSessionUseCase.execute(command)) {
            is GenerateSessionResult.Success -> {
                ResponseEntity.ok(ResponseMapper.toSessionResponse(result))
            }
            is GenerateSessionResult.GoalNotFound -> {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ProblemDetail(
                        title = "Goal not found",
                        status = 404,
                        detail = "Goal with ID ${result.goalId.value} not found",
                        errorCode = "GOAL_NOT_FOUND"
                    )
                )
            }
            is GenerateSessionResult.NoQuestionsAvailable -> {
                ResponseEntity.ok(
                    SessionResponse(
                        sessionId = "empty",
                        questions = emptyList(),
                        strategy = "NONE",
                        reviewCount = 0,
                        discoveryCount = 0,
                        limits = LimitsResponse(0)
                    )
                )
            }
            is GenerateSessionResult.Forbidden -> {
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ProblemDetail(
                        title = "Forbidden",
                        status = 403,
                        detail = result.message,
                        errorCode = "FORBIDDEN"
                    )
                )
            }
        }
    }

    // ============================================================================
    // REVIEWS
    // ============================================================================

    @PostMapping("/reviews")
    fun submitReview(
        @RequestHeader("X-Device-Id") deviceId: String,
        @RequestBody request: SubmitReviewRequest
    ): ResponseEntity<*> {
        log.info("📝 POST /reviews - deviceId: {}, questionId: {}, correct: {}",
            deviceId, request.questionId, request.correct)

        val command = SubmitReviewUseCase.Command(
            userId = UserId(deviceId),
            questionId = QuestionId(request.questionId),
            isCorrect = request.correct,
            selectedChoiceIds = request.selectedChoiceIds?.map { UUID.fromString(it) }
        )

        return when (val result = submitReviewUseCase.execute(command)) {
            is SubmitReviewResult.Success -> {
                ResponseEntity.ok(ResponseMapper.toReviewResponse(result, request.correct))
            }
            is SubmitReviewResult.QuotaExceeded -> {
                ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    ProblemDetail(
                        title = "Quota exceeded",
                        status = 429,
                        detail = result.message,
                        errorCode = "QUOTA_EXCEEDED",
                        meta = mapOf(
                            "quotaLimit" to result.quotaLimit,
                            "quotaUsed" to result.quotaUsed
                        )
                    )
                )
            }
            is SubmitReviewResult.QuestionNotFound -> {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ProblemDetail(
                        title = "Question not found",
                        status = 404,
                        detail = "Question with ID ${result.questionId.value} not found",
                        errorCode = "QUESTION_NOT_FOUND"
                    )
                )
            }
            is SubmitReviewResult.Forbidden -> {
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ProblemDetail(
                        title = "Forbidden",
                        status = 403,
                        detail = result.message,
                        errorCode = "FORBIDDEN"
                    )
                )
            }
        }
    }
}
