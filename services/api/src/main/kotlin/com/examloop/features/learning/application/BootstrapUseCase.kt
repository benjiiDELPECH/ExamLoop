package com.examloop.features.learning.application

import com.examloop.features.learning.domain.*
import org.slf4j.LoggerFactory

/**
 * BootstrapUseCase — Initialisation de l'application au lancement
 * 
 * Responsabilités :
 * 1. Créer ou récupérer le profil utilisateur
 * 2. Initialiser le quota du jour
 * 3. Calculer les statistiques du dashboard
 */
class BootstrapUseCase(
    private val profilePort: ProfilePort,
    private val usageDailyPort: UsageDailyPort,
    private val reviewStatePort: ReviewStatePort,
    private val goalPort: GoalPort,
    private val questionPort: QuestionPort,
    private val clockPort: ClockPort
) {
    private val log = LoggerFactory.getLogger(BootstrapUseCase::class.java)

    companion object {
        const val FREE_DAILY_REVIEW_LIMIT = 20
    }

    data class Command(
        val userId: UserId
    )

    fun execute(command: Command): BootstrapResult {
        log.info("🚀 Bootstrap - userId: {}", command.userId.value)

        val now = clockPort.now()
        val today = clockPort.today()

        // 1. Upsert Profile
        val profile = profilePort.findById(command.userId)
            ?: profilePort.save(Profile(userId = command.userId, createdAt = now))

        // 2. Upsert UsageDaily
        val usage = usageDailyPort.findByUserIdAndDate(command.userId, today)
            ?: usageDailyPort.save(UsageDaily(command.userId, today, 0))

        // 3. Calculer les stats du dashboard
        val reviewStates = reviewStatePort.findByUserId(command.userId)
        val dueForReview = reviewStatePort.findDueForReview(command.userId, now)
        
        val userGoals = goalPort.findByUserId(command.userId)
        val publicGoals = goalPort.findPublicGoals()
        val allGoals = (userGoals + publicGoals).distinctBy { it.id }

        val totalQuestions = allGoals.sumOf { goal ->
            questionPort.findByGoalId(goal.id).size
        }

        val masteredCount = reviewStates.count { it.masteryLevel == MasteryLevel.MASTERED }

        val dashboard = DashboardInfo(
            dueCount = dueForReview.size,
            totalQuestions = totalQuestions,
            goalsCount = allGoals.size,
            masteredCount = masteredCount
        )

        log.info("✅ Bootstrap complete - userId: {}, premium: {}, due: {}, mastered: {}",
            command.userId.value, profile.premium, dashboard.dueCount, dashboard.masteredCount)

        return BootstrapResult.Success(
            profile = profile,
            usage = UsageInfo(
                date = today.toString(),
                reviewsUsed = usage.reviewsCount,
                reviewsLimit = FREE_DAILY_REVIEW_LIMIT,
                premium = profile.premium
            ),
            dashboard = dashboard
        )
    }
}
