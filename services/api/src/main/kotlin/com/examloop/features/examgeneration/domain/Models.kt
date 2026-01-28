package com.examloop.features.examgeneration.domain

import com.examloop.features.learning.domain.QuestionDifficulty
import com.examloop.features.learning.domain.QuestionType
import java.time.Instant

/**
 * Domain Models pour la génération d'examens par IA
 */

/**
 * Configuration de génération d'examen
 */
data class ExamGenerationConfig(
    val topic: String,
    val description: String? = null,
    val questionCount: Int = 10,
    val targetDifficulty: DifficultyMix = DifficultyMix.BALANCED,
    val questionTypes: List<QuestionType> = listOf(QuestionType.SINGLE_CHOICE),
    val language: String = "fr",
    val includeExplanations: Boolean = true
)

/**
 * Mix de difficulté pour la génération
 */
enum class DifficultyMix(val description: String) {
    EASY_FOCUSED("Majorité facile - 60% Easy, 30% Medium, 10% Hard"),
    BALANCED("Équilibré - 30% Easy, 40% Medium, 30% Hard"),
    HARD_FOCUSED("Majorité difficile - 10% Easy, 30% Medium, 60% Hard"),
    PROGRESSIVE("Progressif - commence facile, finit difficile")
}

/**
 * Question générée par l'IA (avant validation/sauvegarde)
 */
data class GeneratedQuestion(
    val prompt: String,
    val type: QuestionType,
    val choices: List<GeneratedChoice>?,
    val answer: String,
    val explanation: String?,
    val difficulty: QuestionDifficulty,
    val tags: List<String> = emptyList(),
    val chapter: String? = null
)

data class GeneratedChoice(
    val label: String,
    val isCorrect: Boolean
)

/**
 * Résultat de l'analyse du sujet
 */
data class TopicAnalysis(
    val mainTopic: String,
    val subtopics: List<String>,
    val suggestedChapters: List<String>,
    val estimatedDepth: TopicDepth,
    val relatedConcepts: List<String>
)

enum class TopicDepth {
    BEGINNER,      // Concepts de base
    INTERMEDIATE,  // Concepts intermédiaires
    ADVANCED,      // Concepts avancés
    EXPERT         // Concepts experts/edge cases
}

/**
 * Résultat de la génération
 */
data class GenerationResult(
    val questions: List<GeneratedQuestion>,
    val topicAnalysis: TopicAnalysis,
    val generationMetadata: GenerationMetadata
)

data class GenerationMetadata(
    val model: String,
    val tokensUsed: Int,
    val generationTimeMs: Long,
    val timestamp: Instant = Instant.now()
)
