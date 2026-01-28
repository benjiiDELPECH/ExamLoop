package com.examloop.features.learning.domain

import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.min

/**
 * SpacedRepetitionService — Service de répétition espacée (Domain pur)
 *
 * Implémente la courbe d'oubli d'Ebbinghaus avec calcul de priorité.
 * Détermine quelles questions doivent être révisées et avec quelle urgence.
 *
 * ALGORITHME:
 * - Calcule la probabilité d'oubli basée sur le temps écoulé
 * - Détermine le niveau de maîtrise (NOVICE → MASTERED)
 * - Calcule un score de priorité pour chaque question
 * - Sélectionne les questions à réviser selon la stratégie de distribution
 *
 * Ce service est STATELESS et PUR - aucune dépendance Spring, aucun I/O.
 */
class SpacedRepetitionService {

    private val log = LoggerFactory.getLogger(SpacedRepetitionService::class.java)

    companion object {
        // ✅ INTERVALLES SCIENTIFIQUES - Basés sur Ebbinghaus
        val SPACED_INTERVALS_DAYS = listOf(1, 3, 7, 14, 30, 90)

        // ✅ SEUILS
        const val FORGETTING_THRESHOLD = 0.60  // Seuil de probabilité d'oubli
        const val MIN_REVIEWS_FOR_MASTERY = 3
        const val MASTERY_SUCCESS_RATE = 0.85

        // ✅ POIDS DE PRIORITÉ
        const val WEIGHT_FORGETTING = 50.0
        const val WEIGHT_PERFORMANCE = 30.0
        const val WEIGHT_DIFFICULTY = 15.0
        const val WEIGHT_RECENCY = 5.0
    }

    /**
     * ✅ ANALYSE D'UNE QUESTION
     * Calcule les métriques de révision pour une question donnée.
     */
    fun analyzeQuestion(
        question: Question,
        attempts: List<QuestionAttempt>,
        now: Instant = Instant.now()
    ): ReviewAnalysis {
        require(attempts.isNotEmpty()) { "Attempts cannot be empty" }

        val sortedAttempts = attempts.sortedBy { it.timestamp }
        val lastAttempt = sortedAttempts.last()
        val successCount = attempts.count { it.isCorrect }
        val reviewCount = attempts.size
        val successRate = successCount.toDouble() / reviewCount

        // Déterminer le niveau de maîtrise
        val masteryLevel = MasteryLevel.determine(reviewCount, successRate)

        // Calculer l'intervalle optimal
        val optimalIntervalDays = getOptimalInterval(masteryLevel)

        // Calculer la probabilité d'oubli
        val forgettingProbability = calculateForgettingProbability(
            lastReviewedAt = lastAttempt.timestamp,
            optimalIntervalDays = optimalIntervalDays,
            now = now
        )

        return ReviewAnalysis(
            question = question,
            reviewCount = reviewCount,
            successRate = successRate,
            lastReviewedAt = lastAttempt.timestamp,
            optimalIntervalDays = optimalIntervalDays,
            forgettingProbability = forgettingProbability,
            lastAnswerCorrect = lastAttempt.isCorrect,
            masteryLevel = masteryLevel
        )
    }

    /**
     * ✅ CALCUL DE PRIORITÉ
     * Score de priorité pour une question (plus élevé = plus urgent à réviser).
     */
    fun calculatePriority(analysis: ReviewAnalysis, now: Instant = Instant.now()): QuestionPriority {
        val daysSinceReview = ChronoUnit.DAYS.between(analysis.lastReviewedAt, now)

        // Composition fonctionnelle des scores
        val forgettingScore = WEIGHT_FORGETTING * analysis.forgettingProbability
        val performanceScore = WEIGHT_PERFORMANCE * (1.0 - analysis.successRate)
        val difficultyScore = WEIGHT_DIFFICULTY * (analysis.question.difficulty.level / 4.0)
        val recencyScore = WEIGHT_RECENCY * min(1.0, daysSinceReview / 25.0)

        val totalScore = forgettingScore + performanceScore + difficultyScore + recencyScore

        return QuestionPriority(
            question = analysis.question,
            priorityScore = totalScore,
            analysis = analysis
        )
    }

    /**
     * ✅ SÉLECTION DES QUESTIONS À RÉVISER
     * Retourne les questions qui doivent être révisées (forgetting > seuil).
     */
    fun selectQuestionsForReview(
        analyses: List<ReviewAnalysis>,
        maxQuestions: Int,
        now: Instant = Instant.now()
    ): List<Question> {
        val thresholdScore = FORGETTING_THRESHOLD * 100

        return analyses
            .map { calculatePriority(it, now) }
            .filter { it.priorityScore >= thresholdScore }
            .sortedByDescending { it.priorityScore }
            .take(maxQuestions)
            .map { it.question }
    }

    /**
     * ✅ MISE À JOUR DE L'ÉTAT DE RÉVISION
     * Calcule le nouvel état après une réponse.
     */
    fun updateReviewState(
        currentState: ReviewState?,
        questionId: QuestionId,
        userId: UserId,
        isCorrect: Boolean,
        now: Instant = Instant.now()
    ): ReviewState {
        val newSuccessCount = (currentState?.successCount ?: 0) + if (isCorrect) 1 else 0
        val newFailCount = (currentState?.failCount ?: 0) + if (!isCorrect) 1 else 0
        val newReviewCount = newSuccessCount + newFailCount
        val newSuccessRate = newSuccessCount.toDouble() / newReviewCount

        val newMasteryLevel = MasteryLevel.determine(newReviewCount, newSuccessRate)
        val newIntervalDays = getOptimalInterval(newMasteryLevel)
        val nextReviewAt = now.plus(newIntervalDays.toLong(), ChronoUnit.DAYS)

        return ReviewState(
            questionId = questionId,
            userId = userId,
            masteryLevel = newMasteryLevel,
            successCount = newSuccessCount,
            failCount = newFailCount,
            lastReviewedAt = now,
            nextReviewAt = nextReviewAt,
            optimalIntervalDays = newIntervalDays
        )
    }

    /**
     * ✅ INTERVALLE OPTIMAL
     * Retourne l'intervalle en jours selon le niveau de maîtrise.
     */
    private fun getOptimalInterval(masteryLevel: MasteryLevel): Int {
        val index = min(masteryLevel.intervalIndex, SPACED_INTERVALS_DAYS.size - 1)
        return SPACED_INTERVALS_DAYS[index]
    }

    /**
     * ✅ PROBABILITÉ D'OUBLI (Ebbinghaus)
     * Calcule la probabilité qu'une question soit oubliée.
     */
    private fun calculateForgettingProbability(
        lastReviewedAt: Instant,
        optimalIntervalDays: Int,
        now: Instant
    ): Double {
        val daysSince = ChronoUnit.DAYS.between(lastReviewedAt, now)
        return min(1.0, daysSince.toDouble() / (optimalIntervalDays * 2.0))
    }
}

/**
 * Analyse de révision d'une question.
 */
data class ReviewAnalysis(
    val question: Question,
    val reviewCount: Int,
    val successRate: Double,
    val lastReviewedAt: Instant,
    val optimalIntervalDays: Int,
    val forgettingProbability: Double,
    val lastAnswerCorrect: Boolean,
    val masteryLevel: MasteryLevel
) {
    /**
     * La question doit-elle être révisée ?
     */
    fun needsReview(): Boolean = 
        forgettingProbability >= SpacedRepetitionService.FORGETTING_THRESHOLD
}

/**
 * Priorité de révision d'une question.
 */
data class QuestionPriority(
    val question: Question,
    val priorityScore: Double,
    val analysis: ReviewAnalysis
) {
    fun isHighPriority(): Boolean = 
        priorityScore >= SpacedRepetitionService.FORGETTING_THRESHOLD * 100

    fun getPriorityLevel(): String = when {
        priorityScore >= 80 -> "URGENT"
        priorityScore >= 60 -> "HIGH"
        priorityScore >= 40 -> "MEDIUM"
        else -> "LOW"
    }
}
