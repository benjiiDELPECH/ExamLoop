package com.examloop.features.examgeneration.application

import com.examloop.features.examgeneration.domain.*
import com.examloop.features.learning.domain.UserId

/**
 * Ports pour la génération d'examens par IA
 */

/**
 * Port principal pour la génération de questions
 */
interface QuestionGeneratorPort {
    /**
     * Génère des questions à partir d'un sujet
     */
    suspend fun generateQuestions(
        config: ExamGenerationConfig
    ): GenerationResult

    /**
     * Analyse un sujet pour identifier les sous-thèmes
     */
    suspend fun analyzeTopic(topic: String): TopicAnalysis
}

/**
 * Port pour la vérification de la qualité des questions
 */
interface QuestionQualityPort {
    /**
     * Vérifie et améliore la qualité d'une question
     */
    suspend fun validateAndImprove(
        question: GeneratedQuestion,
        existingQuestions: List<GeneratedQuestion>
    ): QualityCheckResult
}

data class QualityCheckResult(
    val isValid: Boolean,
    val improvedQuestion: GeneratedQuestion?,
    val issues: List<QualityIssue>
)

data class QualityIssue(
    val type: QualityIssueType,
    val description: String,
    val severity: IssueSeverity
)

enum class QualityIssueType {
    DUPLICATE,
    AMBIGUOUS_QUESTION,
    INCORRECT_ANSWER,
    MISSING_EXPLANATION,
    DIFFICULTY_MISMATCH,
    GRAMMAR_ERROR
}

enum class IssueSeverity {
    WARNING,
    ERROR
}
