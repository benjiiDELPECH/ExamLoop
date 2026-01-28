package com.examloop.features.learning.application

import com.examloop.features.learning.domain.*
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * GenerateSessionUseCase — Générer une session d'apprentissage
 * 
 * Utilise l'algorithme hybride pour créer une session optimale :
 * - Questions à réviser (Spaced Repetition)
 * - Questions nouvelles (Adaptive Difficulty)
 * - Équilibrage selon la stratégie de distribution
 */
class GenerateSessionUseCase(
    private val questionPort: QuestionPort,
    private val goalPort: GoalPort,
    private val attemptPort: QuestionAttemptPort,
    private val reviewStatePort: ReviewStatePort,
    private val clockPort: ClockPort,
    private val hybridLearningService: HybridLearningService = HybridLearningService()
) {
    private val log = LoggerFactory.getLogger(GenerateSessionUseCase::class.java)

    data class Command(
        val userId: UserId,
        val goalId: GoalId?,
        val maxQuestions: Int = 10,
        val difficultyFilter: QuestionDifficulty? = null,
        val distributionStrategy: DistributionStrategy? = null
    )

    fun execute(command: Command): GenerateSessionResult {
        log.info("🎯 GenerateSession - userId: {}, goalId: {}, max: {}",
            command.userId.value, command.goalId?.value, command.maxQuestions)

        val now = clockPort.now()

        // 1. Récupérer les questions disponibles
        val availableQuestions = if (command.goalId != null) {
            // Vérifier que le goal existe
            if (!goalPort.existsById(command.goalId)) {
                log.warn("❌ Goal not found: {}", command.goalId.value)
                return GenerateSessionResult.GoalNotFound(command.goalId)
            }
            questionPort.findByGoalId(command.goalId)
        } else {
            // Toutes les questions publiques
            goalPort.findPublicGoals()
                .flatMap { questionPort.findByGoalId(it.id) }
        }

        if (availableQuestions.isEmpty()) {
            log.warn("⚠️ No questions available for session")
            return GenerateSessionResult.NoQuestionsAvailable(
                goalId = command.goalId
            )
        }

        // 2. Récupérer l'historique utilisateur
        val userAttempts = if (command.goalId != null) {
            attemptPort.findByUserIdAndGoalId(command.userId, command.goalId)
        } else {
            attemptPort.findByUserId(command.userId)
        }

        // 3. Récupérer les états de révision
        val reviewStates = reviewStatePort.findByUserId(command.userId)
            .associateBy { it.questionId }

        // 4. Créer la configuration de session
        val config = SessionConfig(
            goalId = command.goalId,
            maxQuestions = command.maxQuestions,
            difficultyFilter = command.difficultyFilter,
            distributionStrategy = command.distributionStrategy
        )

        // 5. Générer la session avec l'algorithme hybride
        val result = hybridLearningService.generateSession(
            config = config,
            availableQuestions = availableQuestions,
            userAttempts = userAttempts,
            reviewStates = reviewStates,
            now = now
        )

        if (result.isEmpty()) {
            log.warn("⚠️ Hybrid algorithm returned no questions")
            return GenerateSessionResult.NoQuestionsAvailable(
                goalId = command.goalId,
                message = "Toutes les questions ont été révisées récemment. Revenez plus tard."
            )
        }

        val sessionId = "session_${UUID.randomUUID().toString().take(12)}"

        log.info("✅ Session generated - sessionId: {}, questions: {}, strategy: {}, review: {}, discovery: {}",
            sessionId, result.totalCount, result.strategy, result.reviewCount, result.discoveryCount)

        return GenerateSessionResult.Success(
            sessionId = sessionId,
            questions = result.questions,
            strategy = result.strategy,
            reviewCount = result.reviewCount,
            discoveryCount = result.discoveryCount
        )
    }
}
