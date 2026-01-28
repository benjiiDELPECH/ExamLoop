package com.examloop.features.learning.domain

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

/**
 * Tests comportementaux pour AdaptiveDifficultyService.
 * 
 * Ces tests vérifient les INVARIANTS de l'algorithme :
 * - INV-01: Promotion si successRate >= 75%
 * - INV-02: Demotion si successRate <= 40%
 * - INV-03: Pas d'adaptation avec < 3 tentatives
 * - INV-04: La difficulté ne peut pas sauter plus d'un niveau
 */
@DisplayName("AdaptiveDifficultyService")
class AdaptiveDifficultyServiceTest {

    private lateinit var service: AdaptiveDifficultyService
    private val userId = UserId("test-user")
    private var questionIdCounter = 1L

    @BeforeEach
    fun setup() {
        service = AdaptiveDifficultyService()
    }

    @Nested
    @DisplayName("Règle de promotion (>= 75% succès)")
    inner class PromotionTests {

        @Test
        fun `should promote from EASY to MEDIUM when success rate is 80%`() {
            // Given: 8 succès sur 10 tentatives = 80%
            val attempts = createAttempts(successCount = 8, failCount = 2)

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.EASY)

            // Then
            assertEquals(QuestionDifficulty.MEDIUM, result.recommendedDifficulty)
            assertEquals(AdaptationReason.PROMOTED, result.reason)
        }

        @Test
        fun `should promote from MEDIUM to HARD when success rate is 75%`() {
            // Given: exactement 75%
            val attempts = createAttempts(successCount = 9, failCount = 3) // 75%

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.MEDIUM)

            // Then
            assertEquals(QuestionDifficulty.HARD, result.recommendedDifficulty)
            assertEquals(AdaptationReason.PROMOTED, result.reason)
        }

        @Test
        fun `should promote from HARD to VERY_HARD when success rate is 100%`() {
            // Given: 100% succès
            val attempts = createAttempts(successCount = 10, failCount = 0)

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.HARD)

            // Then
            assertEquals(QuestionDifficulty.VERY_HARD, result.recommendedDifficulty)
            assertEquals(AdaptationReason.PROMOTED, result.reason)
        }

        @Test
        fun `should NOT promote beyond VERY_HARD`() {
            // Given: 100% succès mais déjà au max
            val attempts = createAttempts(successCount = 10, failCount = 0)

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.VERY_HARD)

            // Then: reste à VERY_HARD
            assertEquals(QuestionDifficulty.VERY_HARD, result.recommendedDifficulty)
            assertEquals(AdaptationReason.MAINTAINED, result.reason)
        }
    }

    @Nested
    @DisplayName("Règle de demotion (<= 40% succès)")
    inner class DemotionTests {

        @Test
        fun `should demote from HARD to MEDIUM when success rate is 30%`() {
            // Given: 3 succès sur 10 tentatives = 30%
            val attempts = createAttempts(successCount = 3, failCount = 7)

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.HARD)

            // Then
            assertEquals(QuestionDifficulty.MEDIUM, result.recommendedDifficulty)
            assertEquals(AdaptationReason.DEMOTED, result.reason)
        }

        @Test
        fun `should demote from MEDIUM to EASY when success rate is 40%`() {
            // Given: exactement 40%
            val attempts = createAttempts(successCount = 4, failCount = 6)

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.MEDIUM)

            // Then
            assertEquals(QuestionDifficulty.EASY, result.recommendedDifficulty)
            assertEquals(AdaptationReason.DEMOTED, result.reason)
        }

        @Test
        fun `should NOT demote below EASY`() {
            // Given: 0% succès mais déjà au min
            val attempts = createAttempts(successCount = 0, failCount = 10)

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.EASY)

            // Then: reste à EASY
            assertEquals(QuestionDifficulty.EASY, result.recommendedDifficulty)
            assertEquals(AdaptationReason.MAINTAINED, result.reason)
        }
    }

    @Nested
    @DisplayName("Règle de maintien (entre 40% et 75%)")
    inner class MaintainTests {

        @Test
        fun `should maintain difficulty when success rate is 50%`() {
            // Given: 50% succès
            val attempts = createAttempts(successCount = 5, failCount = 5)

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.MEDIUM)

            // Then
            assertEquals(QuestionDifficulty.MEDIUM, result.recommendedDifficulty)
            assertEquals(AdaptationReason.MAINTAINED, result.reason)
        }

        @Test
        fun `should maintain difficulty when success rate is 60%`() {
            // Given: 60% succès (entre 40% et 75%)
            val attempts = createAttempts(successCount = 6, failCount = 4)

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.HARD)

            // Then
            assertEquals(QuestionDifficulty.HARD, result.recommendedDifficulty)
            assertEquals(AdaptationReason.MAINTAINED, result.reason)
        }
    }

    @Nested
    @DisplayName("Règle du minimum de tentatives")
    inner class MinAttemptsTests {

        @Test
        fun `should NOT adapt with only 1 attempt`() {
            // Given: seulement 1 tentative (< 3 minimum)
            val attempts = createAttempts(successCount = 1, failCount = 0)

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.MEDIUM)

            // Then
            assertEquals(QuestionDifficulty.MEDIUM, result.recommendedDifficulty)
            assertEquals(AdaptationReason.INSUFFICIENT_DATA, result.reason)
        }

        @Test
        fun `should NOT adapt with only 2 attempts`() {
            // Given: seulement 2 tentatives (< 3 minimum)
            val attempts = createAttempts(successCount = 2, failCount = 0)

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.EASY)

            // Then: pas de promotion malgré 100% succès
            assertEquals(QuestionDifficulty.EASY, result.recommendedDifficulty)
            assertEquals(AdaptationReason.INSUFFICIENT_DATA, result.reason)
        }

        @Test
        fun `should adapt with exactly 3 attempts`() {
            // Given: exactement 3 tentatives (= minimum)
            val attempts = createAttempts(successCount = 3, failCount = 0) // 100% succès

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.EASY)

            // Then: promotion car >= 75%
            assertEquals(QuestionDifficulty.MEDIUM, result.recommendedDifficulty)
            assertEquals(AdaptationReason.PROMOTED, result.reason)
        }
    }

    @Nested
    @DisplayName("Métriques de performance")
    inner class MetricsTests {

        @Test
        fun `should calculate correct success rate`() {
            // Given: 7 succès sur 10
            val attempts = createAttempts(successCount = 7, failCount = 3)

            // When
            val result = service.calculateAdaptiveDifficulty(attempts, QuestionDifficulty.MEDIUM)

            // Then
            assertEquals(10, result.metrics.totalAttempts)
            assertEquals(0.7, result.metrics.successRate, 0.001)
        }
    }

    // ============================================================================
    // HELPERS
    // ============================================================================

    private fun createAttempts(successCount: Int, failCount: Int): List<QuestionAttempt> {
        val questionId = QuestionId(questionIdCounter++)
        val attempts = mutableListOf<QuestionAttempt>()

        repeat(successCount) {
            attempts.add(
                QuestionAttempt(
                    questionId = questionId,
                    userId = userId,
                    isCorrect = true,
                    timestamp = Instant.now()
                )
            )
        }

        repeat(failCount) {
            attempts.add(
                QuestionAttempt(
                    questionId = questionId,
                    userId = userId,
                    isCorrect = false,
                    timestamp = Instant.now()
                )
            )
        }

        return attempts.shuffled()
    }
}
