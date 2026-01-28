package com.examloop.features.learning.application

import com.examloop.features.learning.domain.*
import org.slf4j.LoggerFactory
import java.time.LocalDate

/**
 * SubmitReviewUseCase — Soumettre une réponse à une question
 * 
 * Responsabilités :
 * 1. Vérifier le quota (free tier)
 * 2. Valider que la question existe et appartient à l'utilisateur
 * 3. Enregistrer la tentative
 * 4. Mettre à jour l'état de révision (Spaced Repetition)
 * 5. Incrémenter le compteur de quota
 */
class SubmitReviewUseCase(
    private val questionPort: QuestionPort,
    private val attemptPort: QuestionAttemptPort,
    private val reviewStatePort: ReviewStatePort,
    private val profilePort: ProfilePort,
    private val usageDailyPort: UsageDailyPort,
    private val clockPort: ClockPort,
    private val spacedRepetitionService: SpacedRepetitionService = SpacedRepetitionService()
) {
    private val log = LoggerFactory.getLogger(SubmitReviewUseCase::class.java)

    companion object {
        const val FREE_DAILY_REVIEW_LIMIT = 20
    }

    data class Command(
        val userId: UserId,
        val questionId: QuestionId,
        val isCorrect: Boolean,
        val selectedChoiceIds: List<java.util.UUID>? = null
    )

    fun execute(command: Command): SubmitReviewResult {
        log.info("📝 SubmitReview - userId: {}, questionId: {}, correct: {}",
            command.userId.value, command.questionId.value, command.isCorrect)

        val now = clockPort.now()
        val today = clockPort.today()

        // 1. Charger le profil (ou créer si nouveau)
        val profile = profilePort.findById(command.userId)
            ?: profilePort.save(Profile(command.userId))

        // 2. Vérifier le quota (sauf si premium)
        if (!profile.premium) {
            val usage = usageDailyPort.findByUserIdAndDate(command.userId, today)
                ?: UsageDaily(command.userId, today, 0)

            if (usage.reviewsCount >= FREE_DAILY_REVIEW_LIMIT) {
                log.warn("⚠️ QUOTA_EXCEEDED - userId: {}, used: {}, limit: {}",
                    command.userId.value, usage.reviewsCount, FREE_DAILY_REVIEW_LIMIT)
                return SubmitReviewResult.QuotaExceeded(
                    quotaLimit = FREE_DAILY_REVIEW_LIMIT,
                    quotaUsed = usage.reviewsCount
                )
            }
        }

        // 3. Vérifier que la question existe
        val question = questionPort.findById(command.questionId)
        if (question == null) {
            log.warn("❌ Question not found: {}", command.questionId.value)
            return SubmitReviewResult.QuestionNotFound(command.questionId)
        }

        // 4. Enregistrer la tentative
        val attempt = QuestionAttempt(
            questionId = command.questionId,
            userId = command.userId,
            isCorrect = command.isCorrect,
            selectedChoiceIds = command.selectedChoiceIds,
            timestamp = now
        )
        attemptPort.save(attempt)

        // 5. Mettre à jour l'état de révision (Spaced Repetition)
        val currentState = reviewStatePort.findByUserIdAndQuestionId(command.userId, command.questionId)
        val newState = spacedRepetitionService.updateReviewState(
            currentState = currentState,
            questionId = command.questionId,
            userId = command.userId,
            isCorrect = command.isCorrect,
            now = now
        )
        val savedState = reviewStatePort.save(newState)

        // 6. Incrémenter le compteur de quota
        val updatedUsage = usageDailyPort.incrementReviewCount(command.userId, today)

        log.info("✅ Review submitted - userId: {}, questionId: {}, newMastery: {}, usage: {}/{}",
            command.userId.value, command.questionId.value, savedState.masteryLevel,
            updatedUsage.reviewsCount, FREE_DAILY_REVIEW_LIMIT)

        return SubmitReviewResult.Success(
            reviewState = savedState,
            usage = UsageInfo(
                date = today.toString(),
                reviewsUsed = updatedUsage.reviewsCount,
                reviewsLimit = FREE_DAILY_REVIEW_LIMIT,
                premium = profile.premium
            )
        )
    }
}
