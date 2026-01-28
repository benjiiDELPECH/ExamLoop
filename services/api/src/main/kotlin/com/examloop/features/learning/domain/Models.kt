package com.examloop.features.learning.domain

import java.time.Instant
import java.util.UUID

// ============================================================================
// VALUE OBJECTS
// ============================================================================

@JvmInline
value class QuestionId(val value: Long) {
    companion object {
        fun from(value: String) = QuestionId(value.toLong())
        fun from(value: Long) = QuestionId(value)
    }
}

@JvmInline
value class UserId(val value: String)

@JvmInline
value class GoalId(val value: Long) {
    companion object {
        fun from(value: String) = GoalId(value.toLong())
        fun from(value: Long) = GoalId(value)
    }
}

// ============================================================================
// ENUMS
// ============================================================================

/**
 * Niveau de difficulté des questions.
 * Utilisé par l'algorithme Adaptive Difficulty.
 */
enum class QuestionDifficulty(val level: Int, val label: String) {
    EASY(1, "Facile"),
    MEDIUM(2, "Moyen"),
    HARD(3, "Difficile"),
    VERY_HARD(4, "Très difficile");

    fun canPromoteTo(): QuestionDifficulty? = when (this) {
        EASY -> MEDIUM
        MEDIUM -> HARD
        HARD -> VERY_HARD
        VERY_HARD -> null
    }

    fun canDemoteTo(): QuestionDifficulty? = when (this) {
        EASY -> null
        MEDIUM -> EASY
        HARD -> MEDIUM
        VERY_HARD -> HARD
    }

    companion object {
        fun fromLevel(level: Int): QuestionDifficulty =
            entries.find { it.level == level } ?: MEDIUM
    }
}

/**
 * Niveau de maîtrise d'une question.
 * Utilisé par l'algorithme Spaced Repetition.
 */
enum class MasteryLevel(val intervalIndex: Int) {
    NOVICE(0),      // < 2 tentatives
    LEARNING(0),    // < 3 tentatives OU < 70% succès
    COMPETENT(2),   // >= 3 tentatives ET < 85% succès
    MASTERED(4);    // >= 3 tentatives ET >= 85% succès

    companion object {
        private const val MIN_REVIEWS_FOR_MASTERY = 3
        private const val MASTERY_SUCCESS_RATE = 0.85
        private const val COMPETENT_SUCCESS_RATE = 0.70

        fun determine(reviewCount: Int, successRate: Double): MasteryLevel = when {
            reviewCount < 2 -> NOVICE
            reviewCount < MIN_REVIEWS_FOR_MASTERY -> LEARNING
            successRate < COMPETENT_SUCCESS_RATE -> LEARNING
            successRate < MASTERY_SUCCESS_RATE -> COMPETENT
            else -> MASTERED
        }
    }
}

/**
 * Type de question.
 */
enum class QuestionType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    OPEN
}

// ============================================================================
// DOMAIN MODELS
// ============================================================================

/**
 * Une question d'examen.
 */
data class Question(
    val id: QuestionId,
    val goalId: GoalId,
    val type: QuestionType,
    val prompt: String,
    val answer: String,
    val choices: List<Choice>? = null,
    val explanation: String? = null,
    val difficulty: QuestionDifficulty,
    val chapter: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Instant = Instant.now()
)

/**
 * Un choix pour les questions MCQ.
 */
data class Choice(
    val id: UUID = UUID.randomUUID(),
    val label: String,
    val isCorrect: Boolean
)

/**
 * Une tentative de réponse à une question.
 */
data class QuestionAttempt(
    val id: UUID = UUID.randomUUID(),
    val questionId: QuestionId,
    val userId: UserId,
    val isCorrect: Boolean,
    val selectedChoiceIds: List<UUID>? = null,
    val timestamp: Instant = Instant.now()
)

/**
 * État de révision d'une question pour un utilisateur (Spaced Repetition).
 */
data class ReviewState(
    val questionId: QuestionId,
    val userId: UserId,
    val masteryLevel: MasteryLevel,
    val successCount: Int,
    val failCount: Int,
    val lastReviewedAt: Instant?,
    val nextReviewAt: Instant,
    val optimalIntervalDays: Int
) {
    val reviewCount: Int get() = successCount + failCount
    
    val successRate: Double get() = 
        if (reviewCount == 0) 0.0 else successCount.toDouble() / reviewCount
}

/**
 * Configuration d'une session d'entraînement.
 */
data class SessionConfig(
    val goalId: GoalId?,
    val maxQuestions: Int = 10,
    val difficultyFilter: QuestionDifficulty? = null,
    val chapterFilter: String? = null,
    val distributionStrategy: DistributionStrategy? = null // null = auto
)

/**
 * Stratégie de distribution entre révision et découverte.
 */
enum class DistributionStrategy(
    val description: String,
    val reviewRatio: Double,
    val discoveryRatio: Double
) {
    EXPLORATION_FOCUSED("Focus découverte - 80% nouvelles questions", 0.20, 0.80),
    DISCOVERY_PREFERRED("Préférence découverte - 70% nouvelles questions", 0.30, 0.70),
    BALANCED("Équilibré - 50/50", 0.50, 0.50),
    REVIEW_PREFERRED("Préférence révision - 70% révision", 0.70, 0.30),
    MASTERY_FOCUSED("Focus maîtrise - 80% révision", 0.80, 0.20);

    companion object {
        /**
         * Détermine automatiquement la stratégie optimale basée sur l'historique utilisateur.
         */
        fun determineOptimal(totalAnsweredQuestions: Int, averageSuccessRate: Double): DistributionStrategy =
            when {
                totalAnsweredQuestions < 10 -> EXPLORATION_FOCUSED
                totalAnsweredQuestions < 50 && averageSuccessRate > 0.7 -> DISCOVERY_PREFERRED
                averageSuccessRate < 0.5 -> REVIEW_PREFERRED
                else -> BALANCED
            }
    }
}
