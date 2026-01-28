package com.examloop.features.learning.domain

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests comportementaux pour SpacedRepetitionService.
 * 
 * Ces tests vérifient les INVARIANTS de l'algorithme :
 * - INV-01: La probabilité d'oubli augmente avec le temps
 * - INV-02: Le mastery level dépend du nombre de tentatives et du taux de réussite
 * - INV-03: Les questions avec haute probabilité d'oubli sont prioritaires
 * - INV-04: L'intervalle optimal augmente avec le mastery level
 */
@DisplayName("SpacedRepetitionService")
class SpacedRepetitionServiceTest {

    private lateinit var service: SpacedRepetitionService
    private val userId = UserId("test-user")
    private val now = Instant.now()
    private var questionIdCounter = 100L
    private var goalIdCounter = 1L

    @BeforeEach
    fun setup() {
        service = SpacedRepetitionService()
    }

    @Nested
    @DisplayName("Mastery Level determination")
    inner class MasteryLevelTests {

        @Test
        fun `should be NOVICE with less than 2 attempts`() {
            // Given: 1 tentative
            val level = MasteryLevel.determine(reviewCount = 1, successRate = 1.0)

            // Then
            assertEquals(MasteryLevel.NOVICE, level)
        }

        @Test
        fun `should be LEARNING with 2 attempts and low success rate`() {
            // Given: 2 tentatives, 50% succès
            val level = MasteryLevel.determine(reviewCount = 2, successRate = 0.5)

            // Then
            assertEquals(MasteryLevel.LEARNING, level)
        }

        @Test
        fun `should be LEARNING with 3+ attempts but low success rate`() {
            // Given: 5 tentatives, 60% succès (< 70%)
            val level = MasteryLevel.determine(reviewCount = 5, successRate = 0.6)

            // Then
            assertEquals(MasteryLevel.LEARNING, level)
        }

        @Test
        fun `should be COMPETENT with 3+ attempts and medium success rate`() {
            // Given: 5 tentatives, 75% succès (entre 70% et 85%)
            val level = MasteryLevel.determine(reviewCount = 5, successRate = 0.75)

            // Then
            assertEquals(MasteryLevel.COMPETENT, level)
        }

        @Test
        fun `should be MASTERED with 3+ attempts and high success rate`() {
            // Given: 5 tentatives, 90% succès (>= 85%)
            val level = MasteryLevel.determine(reviewCount = 5, successRate = 0.9)

            // Then
            assertEquals(MasteryLevel.MASTERED, level)
        }
    }

    @Nested
    @DisplayName("Forgetting probability")
    inner class ForgettingProbabilityTests {

        @Test
        fun `should have low forgetting probability right after review`() {
            // Given: question révisée aujourd'hui
            val question = createQuestion()
            val attempts = listOf(createAttempt(question.id, isCorrect = true, daysAgo = 0))

            // When
            val analysis = service.analyzeQuestion(question, attempts, now)

            // Then: probabilité d'oubli faible
            assertTrue(analysis.forgettingProbability < 0.1)
        }

        @Test
        fun `should have high forgetting probability after long delay`() {
            // Given: question révisée il y a 30 jours, intervalle optimal = 1 jour (NOVICE)
            val question = createQuestion()
            val attempts = listOf(createAttempt(question.id, isCorrect = true, daysAgo = 30))

            // When
            val analysis = service.analyzeQuestion(question, attempts, now)

            // Then: probabilité d'oubli élevée (proche de 1)
            assertTrue(analysis.forgettingProbability >= 0.9)
        }

        @Test
        fun `should indicate review needed when forgetting probability is high`() {
            // Given: question révisée il y a longtemps
            val question = createQuestion()
            val attempts = listOf(createAttempt(question.id, isCorrect = true, daysAgo = 15))

            // When
            val analysis = service.analyzeQuestion(question, attempts, now)

            // Then
            assertTrue(analysis.needsReview())
        }
    }

    @Nested
    @DisplayName("Priority calculation")
    inner class PriorityTests {

        @Test
        fun `should have higher priority for questions with high forgetting probability`() {
            // Given: deux questions, une récente, une ancienne
            val questionRecent = createQuestion()
            val questionOld = createQuestion()

            val recentAttempts = listOf(createAttempt(questionRecent.id, isCorrect = true, daysAgo = 1))
            val oldAttempts = listOf(createAttempt(questionOld.id, isCorrect = true, daysAgo = 30))

            val recentAnalysis = service.analyzeQuestion(questionRecent, recentAttempts, now)
            val oldAnalysis = service.analyzeQuestion(questionOld, oldAttempts, now)

            // When
            val recentPriority = service.calculatePriority(recentAnalysis, now)
            val oldPriority = service.calculatePriority(oldAnalysis, now)

            // Then: l'ancienne a plus de priorité
            assertTrue(oldPriority.priorityScore > recentPriority.priorityScore)
        }

        @Test
        fun `should have higher priority for questions with low success rate`() {
            // Given: deux questions, une avec bon taux, une avec mauvais taux
            val questionGood = createQuestion()
            val questionBad = createQuestion()

            val goodAttempts = (1..5).map { createAttempt(questionGood.id, isCorrect = true, daysAgo = 5) }
            val badAttempts = (1..5).map { createAttempt(questionBad.id, isCorrect = it <= 1, daysAgo = 5) }

            val goodAnalysis = service.analyzeQuestion(questionGood, goodAttempts, now)
            val badAnalysis = service.analyzeQuestion(questionBad, badAttempts, now)

            // When
            val goodPriority = service.calculatePriority(goodAnalysis, now)
            val badPriority = service.calculatePriority(badAnalysis, now)

            // Then: celle avec mauvais taux a plus de priorité
            assertTrue(badPriority.priorityScore > goodPriority.priorityScore)
        }
    }

    @Nested
    @DisplayName("Review state update")
    inner class ReviewStateUpdateTests {

        @Test
        fun `should increment success count on correct answer`() {
            // Given: état initial avec 3 succès
            val questionId = QuestionId(questionIdCounter++)
            val currentState = createReviewState(questionId, successCount = 3, failCount = 1)

            // When
            val newState = service.updateReviewState(currentState, questionId, userId, isCorrect = true, now)

            // Then
            assertEquals(4, newState.successCount)
            assertEquals(1, newState.failCount)
        }

        @Test
        fun `should increment fail count on incorrect answer`() {
            // Given: état initial avec 1 échec
            val questionId = QuestionId(questionIdCounter++)
            val currentState = createReviewState(questionId, successCount = 3, failCount = 1)

            // When
            val newState = service.updateReviewState(currentState, questionId, userId, isCorrect = false, now)

            // Then
            assertEquals(3, newState.successCount)
            assertEquals(2, newState.failCount)
        }

        @Test
        fun `should update mastery level after review`() {
            // Given: état initial LEARNING
            val questionId = QuestionId(questionIdCounter++)
            val currentState = createReviewState(questionId, successCount = 2, failCount = 0)
            assertEquals(MasteryLevel.LEARNING, currentState.masteryLevel)

            // When: réponse correcte (total: 3 succès, 0 échecs = 100%)
            val newState = service.updateReviewState(currentState, questionId, userId, isCorrect = true, now)

            // Then: devrait passer à MASTERED (3+ reviews, 100% success)
            assertEquals(MasteryLevel.MASTERED, newState.masteryLevel)
        }

        @Test
        fun `should create new state when current is null`() {
            // Given: pas d'état existant
            val questionId = QuestionId(questionIdCounter++)

            // When
            val newState = service.updateReviewState(null, questionId, userId, isCorrect = true, now)

            // Then
            assertEquals(1, newState.successCount)
            assertEquals(0, newState.failCount)
            assertEquals(questionId, newState.questionId)
            assertEquals(userId, newState.userId)
        }
    }

    // ============================================================================
    // HELPERS
    // ============================================================================

    private fun createQuestion(
        difficulty: QuestionDifficulty = QuestionDifficulty.MEDIUM
    ): Question = Question(
        id = QuestionId(questionIdCounter++),
        goalId = GoalId(goalIdCounter),
        type = QuestionType.SINGLE_CHOICE,
        prompt = "Test question",
        answer = "Test answer",
        difficulty = difficulty
    )

    private fun createAttempt(
        questionId: QuestionId,
        isCorrect: Boolean,
        daysAgo: Long
    ): QuestionAttempt = QuestionAttempt(
        questionId = questionId,
        userId = userId,
        isCorrect = isCorrect,
        timestamp = now.minus(daysAgo, ChronoUnit.DAYS)
    )

    private fun createReviewState(
        questionId: QuestionId,
        successCount: Int,
        failCount: Int
    ): ReviewState {
        val reviewCount = successCount + failCount
        val successRate = if (reviewCount > 0) successCount.toDouble() / reviewCount else 0.0
        val masteryLevel = MasteryLevel.determine(reviewCount, successRate)

        return ReviewState(
            questionId = questionId,
            userId = userId,
            masteryLevel = masteryLevel,
            successCount = successCount,
            failCount = failCount,
            lastReviewedAt = now.minus(1, ChronoUnit.DAYS),
            nextReviewAt = now.plus(1, ChronoUnit.DAYS),
            optimalIntervalDays = 1
        )
    }
}
