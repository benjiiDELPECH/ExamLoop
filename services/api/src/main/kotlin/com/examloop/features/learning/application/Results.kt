package com.examloop.features.learning.application

import com.examloop.features.learning.domain.*

/**
 * Results — Sealed classes pour les résultats des Use Cases
 * 
 * Pattern : pas d'exceptions pour le flow nominal.
 * Chaque erreur est typée et structurée.
 */

// ============================================================================
// SUBMIT REVIEW RESULT
// ============================================================================

sealed class SubmitReviewResult {
    data class Success(
        val reviewState: ReviewState,
        val usage: UsageInfo
    ) : SubmitReviewResult()
    
    data class QuotaExceeded(
        val quotaLimit: Int,
        val quotaUsed: Int,
        val message: String = "Quota journalier dépassé. Passez à Premium pour continuer."
    ) : SubmitReviewResult()
    
    data class QuestionNotFound(
        val questionId: QuestionId
    ) : SubmitReviewResult()
    
    data class Forbidden(
        val message: String
    ) : SubmitReviewResult()
}

data class UsageInfo(
    val date: String,
    val reviewsUsed: Int,
    val reviewsLimit: Int,
    val premium: Boolean
) {
    val remaining: Int get() = if (premium) Int.MAX_VALUE else (reviewsLimit - reviewsUsed).coerceAtLeast(0)
    val percentUsed: Int get() = if (premium) 0 else ((reviewsUsed.toDouble() / reviewsLimit) * 100).toInt()
}

// ============================================================================
// GENERATE SESSION RESULT
// ============================================================================

sealed class GenerateSessionResult {
    data class Success(
        val sessionId: String,
        val questions: List<Question>,
        val strategy: DistributionStrategy,
        val reviewCount: Int,
        val discoveryCount: Int
    ) : GenerateSessionResult()
    
    data class GoalNotFound(
        val goalId: GoalId
    ) : GenerateSessionResult()
    
    data class NoQuestionsAvailable(
        val goalId: GoalId?,
        val message: String = "Aucune question disponible pour cette session."
    ) : GenerateSessionResult()
    
    data class Forbidden(
        val message: String
    ) : GenerateSessionResult()
}

// ============================================================================
// BOOTSTRAP RESULT
// ============================================================================

sealed class BootstrapResult {
    data class Success(
        val profile: Profile,
        val usage: UsageInfo,
        val dashboard: DashboardInfo
    ) : BootstrapResult()
}

data class DashboardInfo(
    val dueCount: Int,
    val totalQuestions: Int,
    val goalsCount: Int,
    val masteredCount: Int
)

// ============================================================================
// CREATE GOAL RESULT
// ============================================================================

sealed class CreateGoalResult {
    data class Success(val goal: Goal) : CreateGoalResult()
    
    data class ValidationError(
        val errors: List<String>
    ) : CreateGoalResult()
}

// ============================================================================
// CREATE QUESTION RESULT
// ============================================================================

sealed class CreateQuestionResult {
    data class Success(val question: Question) : CreateQuestionResult()
    
    data class GoalNotFound(val goalId: GoalId) : CreateQuestionResult()
    
    data class ValidationError(
        val errors: List<String>
    ) : CreateQuestionResult()
    
    data class Forbidden(val message: String) : CreateQuestionResult()
}
