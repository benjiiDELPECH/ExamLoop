package com.examloop.features.learning.domain

import org.slf4j.LoggerFactory
import java.time.Instant

/**
 * HybridLearningService — Orchestration de l'algorithme hybride (Domain pur)
 *
 * Combine AdaptiveDifficulty et SpacedRepetition pour créer une expérience
 * d'apprentissage optimale. Équilibre intelligemment entre :
 * - Questions de RÉVISION (à revoir, oubliées)
 * - Questions de DÉCOUVERTE (nouvelles, niveau adapté)
 *
 * ALGORITHME:
 * 1. Récupérer les questions à réviser (Spaced Repetition)
 * 2. Récupérer les questions de découverte (Adaptive Difficulty)
 * 3. Déterminer la stratégie de distribution optimale
 * 4. Équilibrer selon la stratégie
 * 5. Retourner le mix optimal
 *
 * Ce service est STATELESS et PUR - aucune dépendance Spring, aucun I/O.
 * Il reçoit toutes les données nécessaires en paramètres.
 */
class HybridLearningService(
    private val adaptiveDifficultyService: AdaptiveDifficultyService = AdaptiveDifficultyService(),
    private val spacedRepetitionService: SpacedRepetitionService = SpacedRepetitionService()
) {
    private val log = LoggerFactory.getLogger(HybridLearningService::class.java)

    /**
     * ✅ GÉNÉRATION DE SESSION HYBRIDE
     * Point d'entrée principal pour générer une session d'apprentissage optimale.
     *
     * @param config Configuration de la session
     * @param availableQuestions Toutes les questions disponibles pour le goal
     * @param userAttempts Historique des tentatives de l'utilisateur
     * @param reviewStates États de révision actuels
     * @param now Instant actuel (injectable pour tests)
     * @return Liste de questions optimisée pour la session
     */
    fun generateSession(
        config: SessionConfig,
        availableQuestions: List<Question>,
        userAttempts: List<QuestionAttempt>,
        reviewStates: Map<QuestionId, ReviewState>,
        now: Instant = Instant.now()
    ): HybridSessionResult {
        log.info("🧠 HYBRID SESSION START - questions: {}, attempts: {}, config: {}",
            availableQuestions.size, userAttempts.size, config)

        // Cas spécial : pas de questions disponibles
        if (availableQuestions.isEmpty()) {
            log.warn("⚠️ No questions available")
            return HybridSessionResult(
                questions = emptyList(),
                strategy = DistributionStrategy.EXPLORATION_FOCUSED,
                reviewCount = 0,
                discoveryCount = 0
            )
        }

        // Cas spécial : utilisateur nouveau (pas d'historique)
        if (userAttempts.isEmpty()) {
            log.info("🌱 INTRODUCTION MODE - New user, no history")
            return generateIntroductionSession(config, availableQuestions)
        }

        // Mode hybride : utilisateur avec historique
        return generateHybridSession(config, availableQuestions, userAttempts, reviewStates, now)
    }

    /**
     * ✅ MODE INTRODUCTION (Utilisateur nouveau)
     * Sélectionne des questions variées pour démarrer l'apprentissage.
     */
    private fun generateIntroductionSession(
        config: SessionConfig,
        availableQuestions: List<Question>
    ): HybridSessionResult {
        // Sélectionner des questions de différentes difficultés pour démarrer
        val selectedQuestions = availableQuestions
            .shuffled()
            .take(config.maxQuestions)

        log.info("🌱 INTRODUCTION SESSION - selected: {} questions", selectedQuestions.size)

        return HybridSessionResult(
            questions = selectedQuestions,
            strategy = DistributionStrategy.EXPLORATION_FOCUSED,
            reviewCount = 0,
            discoveryCount = selectedQuestions.size
        )
    }

    /**
     * ✅ MODE HYBRIDE (Utilisateur avec historique)
     * Combine révision et découverte selon la stratégie optimale.
     */
    private fun generateHybridSession(
        config: SessionConfig,
        availableQuestions: List<Question>,
        userAttempts: List<QuestionAttempt>,
        reviewStates: Map<QuestionId, ReviewState>,
        now: Instant
    ): HybridSessionResult {
        // Phase 1: Déterminer la stratégie de distribution
        val strategy = config.distributionStrategy 
            ?: determineOptimalStrategy(userAttempts)
        log.info("🎯 Strategy: {} - {}", strategy.name, strategy.description)

        // Phase 2: Identifier les questions à réviser
        val reviewQuestions = selectReviewQuestions(
            availableQuestions, userAttempts, reviewStates, config.maxQuestions, now
        )
        log.info("🔄 Review questions: {}", reviewQuestions.size)

        // Phase 3: Identifier les questions de découverte
        val answeredQuestionIds = userAttempts.map { it.questionId }.toSet()
        val discoveryQuestions = selectDiscoveryQuestions(
            availableQuestions, answeredQuestionIds, userAttempts, config
        )
        log.info("🌟 Discovery questions: {}", discoveryQuestions.size)

        // Phase 4: Équilibrer selon la stratégie
        val (selectedReview, selectedDiscovery) = balanceQuestions(
            reviewQuestions, discoveryQuestions, config.maxQuestions, strategy
        )

        // Combiner et mélanger
        val finalQuestions = (selectedReview + selectedDiscovery).shuffled()

        log.info("✅ HYBRID SESSION COMPLETE - review: {}, discovery: {}, total: {}",
            selectedReview.size, selectedDiscovery.size, finalQuestions.size)

        return HybridSessionResult(
            questions = finalQuestions,
            strategy = strategy,
            reviewCount = selectedReview.size,
            discoveryCount = selectedDiscovery.size
        )
    }

    /**
     * ✅ DÉTERMINATION DE LA STRATÉGIE OPTIMALE
     */
    private fun determineOptimalStrategy(userAttempts: List<QuestionAttempt>): DistributionStrategy {
        val totalAnswered = userAttempts.map { it.questionId }.distinct().size
        val avgSuccessRate = if (userAttempts.isNotEmpty()) {
            userAttempts.count { it.isCorrect }.toDouble() / userAttempts.size
        } else {
            0.0
        }

        return DistributionStrategy.determineOptimal(totalAnswered, avgSuccessRate)
    }

    /**
     * ✅ SÉLECTION DES QUESTIONS À RÉVISER (Spaced Repetition)
     */
    private fun selectReviewQuestions(
        availableQuestions: List<Question>,
        userAttempts: List<QuestionAttempt>,
        reviewStates: Map<QuestionId, ReviewState>,
        maxQuestions: Int,
        now: Instant
    ): List<Question> {
        // Grouper les tentatives par question
        val attemptsByQuestion = userAttempts.groupBy { it.questionId }

        // Analyser chaque question répondue
        val analyses = availableQuestions
            .filter { attemptsByQuestion.containsKey(it.id) }
            .mapNotNull { question ->
                val attempts = attemptsByQuestion[question.id] ?: return@mapNotNull null
                try {
                    spacedRepetitionService.analyzeQuestion(question, attempts, now)
                } catch (e: Exception) {
                    log.warn("Failed to analyze question {}: {}", question.id, e.message)
                    null
                }
            }

        // Sélectionner celles qui nécessitent révision
        return spacedRepetitionService.selectQuestionsForReview(analyses, maxQuestions, now)
    }

    /**
     * ✅ SÉLECTION DES QUESTIONS DE DÉCOUVERTE (Adaptive Difficulty)
     */
    private fun selectDiscoveryQuestions(
        availableQuestions: List<Question>,
        answeredQuestionIds: Set<QuestionId>,
        userAttempts: List<QuestionAttempt>,
        config: SessionConfig
    ): List<Question> {
        // Questions jamais répondues
        val unansweredQuestions = availableQuestions
            .filter { it.id !in answeredQuestionIds }

        if (unansweredQuestions.isEmpty()) {
            return emptyList()
        }

        // Déterminer la difficulté adaptative
        val currentDifficulty = config.difficultyFilter ?: QuestionDifficulty.MEDIUM
        val adaptiveResult = adaptiveDifficultyService.calculateAdaptiveDifficulty(
            userAttempts.sortedByDescending { it.timestamp },
            currentDifficulty
        )

        val targetDifficulty = adaptiveResult.recommendedDifficulty
        log.debug("🎯 Adaptive difficulty: {} (reason: {})", 
            targetDifficulty, adaptiveResult.reason)

        // Filtrer par difficulté (avec fallback)
        val atTargetDifficulty = unansweredQuestions.filter { it.difficulty == targetDifficulty }
        
        return if (atTargetDifficulty.isNotEmpty()) {
            atTargetDifficulty.shuffled()
        } else {
            // Fallback : élargir la recherche
            log.debug("📊 No questions at difficulty {}, expanding search", targetDifficulty)
            unansweredQuestions.shuffled()
        }
    }

    /**
     * ✅ ÉQUILIBRAGE ENTRE RÉVISION ET DÉCOUVERTE
     */
    private fun balanceQuestions(
        reviewQuestions: List<Question>,
        discoveryQuestions: List<Question>,
        maxQuestions: Int,
        strategy: DistributionStrategy
    ): Pair<List<Question>, List<Question>> {
        // Cas simples
        if (reviewQuestions.isEmpty() && discoveryQuestions.isEmpty()) {
            return Pair(emptyList(), emptyList())
        }
        if (reviewQuestions.isEmpty()) {
            return Pair(emptyList(), discoveryQuestions.take(maxQuestions))
        }
        if (discoveryQuestions.isEmpty()) {
            return Pair(reviewQuestions.take(maxQuestions), emptyList())
        }

        // Équilibrage selon la stratégie
        val targetReviewCount = (maxQuestions * strategy.reviewRatio).toInt()
        val targetDiscoveryCount = maxQuestions - targetReviewCount

        val actualReviewCount = minOf(targetReviewCount, reviewQuestions.size)
        val actualDiscoveryCount = minOf(targetDiscoveryCount, discoveryQuestions.size)

        // Si on n'atteint pas le max, compléter avec l'autre type
        val remainingSlots = maxQuestions - actualReviewCount - actualDiscoveryCount
        val finalReviewCount = if (discoveryQuestions.size <= actualDiscoveryCount && reviewQuestions.size > actualReviewCount) {
            minOf(actualReviewCount + remainingSlots, reviewQuestions.size)
        } else {
            actualReviewCount
        }
        val finalDiscoveryCount = if (reviewQuestions.size <= actualReviewCount && discoveryQuestions.size > actualDiscoveryCount) {
            minOf(actualDiscoveryCount + remainingSlots, discoveryQuestions.size)
        } else {
            actualDiscoveryCount
        }

        log.debug("⚖️ Balancing - target: {}R/{}D, actual: {}R/{}D",
            targetReviewCount, targetDiscoveryCount, finalReviewCount, finalDiscoveryCount)

        return Pair(
            reviewQuestions.take(finalReviewCount),
            discoveryQuestions.take(finalDiscoveryCount)
        )
    }
}

/**
 * Résultat de la génération d'une session hybride.
 */
data class HybridSessionResult(
    val questions: List<Question>,
    val strategy: DistributionStrategy,
    val reviewCount: Int,
    val discoveryCount: Int
) {
    val totalCount: Int get() = questions.size
    
    fun isEmpty(): Boolean = questions.isEmpty()
}
