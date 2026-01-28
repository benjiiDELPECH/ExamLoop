package com.examloop.features.learning.domain

import org.slf4j.LoggerFactory

/**
 * AdaptiveDifficultyService — Service de difficulté adaptative (Domain pur)
 *
 * Analyse les performances utilisateur et ajuste la difficulté des questions.
 * Crée une expérience d'apprentissage personnalisée qui challenge l'utilisateur
 * de manière appropriée selon ses compétences démontrées.
 *
 * ALGORITHME:
 * - Analyse les N dernières tentatives dans le domaine
 * - Calcule le taux de réussite
 * - Ajuste le niveau de difficulté dynamiquement
 * - Évite le "ping-pong" avec dampening (minimum 3 tentatives)
 *
 * Ce service est STATELESS et PUR - aucune dépendance Spring, aucun I/O.
 */
class AdaptiveDifficultyService {

    private val log = LoggerFactory.getLogger(AdaptiveDifficultyService::class.java)

    companion object {
        // ✅ PARAMÈTRES DE L'ALGORITHME - Ajustables selon le profil d'apprentissage
        const val ANALYSIS_WINDOW = 10          // Dernières N tentatives analysées
        const val PROMOTION_THRESHOLD = 0.75    // 75% de réussite pour monter
        const val DEMOTION_THRESHOLD = 0.40     // 40% de réussite pour descendre
        const val MIN_ATTEMPTS_FOR_ADAPTATION = 3  // Minimum avant adaptation
    }

    /**
     * ✅ ALGORITHME PRINCIPAL
     * Calcule la difficulté optimale pour la prochaine question basée sur les performances.
     *
     * @param recentAttempts Les tentatives récentes de l'utilisateur (triées par date décroissante)
     * @param currentDifficulty La difficulté actuelle
     * @return La difficulté adaptée
     */
    fun calculateAdaptiveDifficulty(
        recentAttempts: List<QuestionAttempt>,
        currentDifficulty: QuestionDifficulty
    ): AdaptiveDifficultyResult {
        log.debug("🧠 Calculating adaptive difficulty - attempts: {}, current: {}", 
            recentAttempts.size, currentDifficulty)

        // Pas assez de données pour adapter
        if (recentAttempts.size < MIN_ATTEMPTS_FOR_ADAPTATION) {
            log.debug("🔧 Not enough data ({} attempts), keeping current difficulty: {}", 
                recentAttempts.size, currentDifficulty)
            return AdaptiveDifficultyResult(
                recommendedDifficulty = currentDifficulty,
                reason = AdaptationReason.INSUFFICIENT_DATA,
                metrics = PerformanceMetrics(recentAttempts.size, 0.0)
            )
        }

        // Analyser les performances
        val metrics = analyzePerformance(recentAttempts.take(ANALYSIS_WINDOW))

        // Appliquer l'algorithme adaptatif
        return applyAdaptiveAlgorithm(currentDifficulty, metrics)
    }

    /**
     * ✅ ANALYSE DES PERFORMANCES
     * Extrait les métriques de performance des tentatives récentes.
     */
    private fun analyzePerformance(attempts: List<QuestionAttempt>): PerformanceMetrics {
        val totalAttempts = attempts.size
        val successfulAttempts = attempts.count { it.isCorrect }
        val successRate = if (totalAttempts > 0) {
            successfulAttempts.toDouble() / totalAttempts
        } else {
            0.0
        }

        return PerformanceMetrics(
            totalAttempts = totalAttempts,
            successRate = successRate
        )
    }

    /**
     * ✅ ALGORITHME ADAPTATIF
     * Détermine l'ajustement de difficulté basé sur les métriques de performance.
     */
    private fun applyAdaptiveAlgorithm(
        current: QuestionDifficulty,
        metrics: PerformanceMetrics
    ): AdaptiveDifficultyResult {
        // Haute performance → augmenter la difficulté
        if (metrics.successRate >= PROMOTION_THRESHOLD) {
            val promoted = current.canPromoteTo()
            if (promoted != null) {
                log.debug("📈 PROMOTING difficulty: {} → {}", current, promoted)
                return AdaptiveDifficultyResult(
                    recommendedDifficulty = promoted,
                    reason = AdaptationReason.PROMOTED,
                    metrics = metrics
                )
            }
        }

        // Basse performance → diminuer la difficulté
        if (metrics.successRate <= DEMOTION_THRESHOLD) {
            val demoted = current.canDemoteTo()
            if (demoted != null) {
                log.debug("📉 DEMOTING difficulty: {} → {}", current, demoted)
                return AdaptiveDifficultyResult(
                    recommendedDifficulty = demoted,
                    reason = AdaptationReason.DEMOTED,
                    metrics = metrics
                )
            }
        }

        // Performance stable → maintenir la difficulté
        log.debug("⚡ MAINTAINING difficulty: {}", current)
        return AdaptiveDifficultyResult(
            recommendedDifficulty = current,
            reason = AdaptationReason.MAINTAINED,
            metrics = metrics
        )
    }
}

/**
 * Résultat de l'algorithme de difficulté adaptative.
 */
data class AdaptiveDifficultyResult(
    val recommendedDifficulty: QuestionDifficulty,
    val reason: AdaptationReason,
    val metrics: PerformanceMetrics
)

/**
 * Raison de l'adaptation de difficulté.
 */
enum class AdaptationReason {
    INSUFFICIENT_DATA,  // Pas assez de données pour adapter
    PROMOTED,           // Difficulté augmentée (bonnes performances)
    DEMOTED,            // Difficulté diminuée (performances faibles)
    MAINTAINED          // Difficulté maintenue (performances stables)
}

/**
 * Métriques de performance utilisateur.
 */
data class PerformanceMetrics(
    val totalAttempts: Int,
    val successRate: Double
)
