package com.examloop.features.learning.api

import com.examloop.features.learning.application.*
import com.examloop.features.learning.domain.*

/**
 * DTOs — Data Transfer Objects pour l'API REST
 * 
 * Séparation stricte entre les modèles domain et les objets API.
 */

// ============================================================================
// REQUESTS
// ============================================================================

data class BootstrapRequest(
    // Actuellement vide, mais extensible
    val metadata: Map<String, String>? = null
)

data class SubmitReviewRequest(
    val questionId: Long,
    val correct: Boolean,
    val selectedChoiceIds: List<String>? = null
)

data class GenerateSessionRequest(
    val goalId: Long? = null,
    val limit: Int = 10,
    val difficulty: String? = null,
    val strategy: String? = null
)

data class CreateGoalRequest(
    val title: String,
    val description: String? = null,
    val isPublic: Boolean = false
)

data class CreateQuestionRequest(
    val goalId: Long,
    val type: String = "SINGLE_CHOICE",
    val prompt: String,
    val answer: String,
    val choices: List<ChoiceRequest>? = null,
    val explanation: String? = null,
    val difficulty: String = "MEDIUM",
    val chapter: String? = null
)

data class ChoiceRequest(
    val label: String,
    val isCorrect: Boolean
)

// ============================================================================
// RESPONSES
// ============================================================================

data class BootstrapResponse(
    val profile: ProfileResponse,
    val usage: UsageResponse,
    val dashboard: DashboardResponse
)

data class ProfileResponse(
    val userId: String,
    val premium: Boolean
)

data class UsageResponse(
    val date: String,
    val reviewsUsed: Int,
    val reviewsLimit: Int,
    val remaining: Int,
    val percentUsed: Int,
    val premium: Boolean
)

data class DashboardResponse(
    val dueCount: Int,
    val totalQuestions: Int,
    val goalsCount: Int,
    val masteredCount: Int
)

data class ReviewResponse(
    val questionId: Long,
    val result: String,
    val reviewState: ReviewStateResponse,
    val usage: UsageResponse
)

data class ReviewStateResponse(
    val masteryLevel: String,
    val successCount: Int,
    val failCount: Int,
    val nextReviewAt: String
)

data class SessionResponse(
    val sessionId: String,
    val questions: List<QuestionResponse>,
    val strategy: String,
    val reviewCount: Int,
    val discoveryCount: Int,
    val limits: LimitsResponse
)

data class QuestionResponse(
    val id: Long,
    val goalId: Long,
    val type: String,
    val prompt: String,
    val choices: List<ChoiceResponse>?,
    val difficulty: String,
    val chapter: String?
)

data class ChoiceResponse(
    val id: String,
    val label: String
    // Note: isCorrect n'est PAS exposé dans la réponse
)

data class LimitsResponse(
    val maxItems: Int
)

data class GoalResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val isPublic: Boolean,
    val stats: GoalStatsResponse
)

data class GoalStatsResponse(
    val items: Int,
    val due: Int
)

data class GoalsListResponse(
    val goals: List<GoalResponse>
)

// ============================================================================
// ERROR RESPONSES (Problem Details)
// ============================================================================

data class ProblemDetail(
    val type: String = "about:blank",
    val title: String,
    val status: Int,
    val detail: String,
    val instance: String? = null,
    val errorCode: String,
    val correlationId: String? = null,
    val meta: Map<String, Any>? = null
)

// ============================================================================
// MAPPERS
// ============================================================================

object ResponseMapper {

    fun toBootstrapResponse(result: BootstrapResult.Success) = BootstrapResponse(
        profile = ProfileResponse(
            userId = result.profile.userId.value,
            premium = result.profile.premium
        ),
        usage = toUsageResponse(result.usage),
        dashboard = DashboardResponse(
            dueCount = result.dashboard.dueCount,
            totalQuestions = result.dashboard.totalQuestions,
            goalsCount = result.dashboard.goalsCount,
            masteredCount = result.dashboard.masteredCount
        )
    )

    fun toUsageResponse(usage: UsageInfo) = UsageResponse(
        date = usage.date,
        reviewsUsed = usage.reviewsUsed,
        reviewsLimit = usage.reviewsLimit,
        remaining = usage.remaining,
        percentUsed = usage.percentUsed,
        premium = usage.premium
    )

    fun toReviewResponse(result: SubmitReviewResult.Success, isCorrect: Boolean) = ReviewResponse(
        questionId = result.reviewState.questionId.value,
        result = if (isCorrect) "CORRECT" else "INCORRECT",
        reviewState = ReviewStateResponse(
            masteryLevel = result.reviewState.masteryLevel.name,
            successCount = result.reviewState.successCount,
            failCount = result.reviewState.failCount,
            nextReviewAt = result.reviewState.nextReviewAt.toString()
        ),
        usage = toUsageResponse(result.usage)
    )

    fun toSessionResponse(result: GenerateSessionResult.Success) = SessionResponse(
        sessionId = result.sessionId,
        questions = result.questions.map { toQuestionResponse(it) },
        strategy = result.strategy.name,
        reviewCount = result.reviewCount,
        discoveryCount = result.discoveryCount,
        limits = LimitsResponse(maxItems = result.questions.size)
    )

    fun toQuestionResponse(question: Question) = QuestionResponse(
        id = question.id.value,
        goalId = question.goalId.value,
        type = question.type.name,
        prompt = question.prompt,
        choices = question.choices?.map { 
            ChoiceResponse(id = it.id.toString(), label = it.label)
        },
        difficulty = question.difficulty.name,
        chapter = question.chapter
    )

    fun toGoalResponse(goal: Goal, itemCount: Int, dueCount: Int) = GoalResponse(
        id = goal.id.value,
        title = goal.title,
        description = goal.description,
        isPublic = goal.isPublic,
        stats = GoalStatsResponse(items = itemCount, due = dueCount)
    )
}
