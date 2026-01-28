package com.examloop.features.examgeneration.config

import com.examloop.features.examgeneration.application.*
import com.examloop.features.examgeneration.infra.SpringAiQuestionGenerator
import com.examloop.features.learning.application.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration pour la feature de génération d'examens par IA
 */
@Configuration
class ExamGenerationConfiguration {

    @Bean
    fun generateExamUseCase(
        profilePort: ProfilePort,
        goalPort: GoalPort,
        questionPort: QuestionPort,
        questionGeneratorPort: QuestionGeneratorPort,
        clockPort: ClockPort
    ): GenerateExamUseCase = GenerateExamUseCase(
        profilePort = profilePort,
        goalPort = goalPort,
        questionPort = questionPort,
        questionGeneratorPort = questionGeneratorPort,
        clockPort = clockPort
    )
}

/**
 * Configuration mock pour les tests sans OpenAI
 */
@Configuration
@ConditionalOnProperty(name = ["examloop.ai.mock"], havingValue = "true")
class MockAiConfiguration {
    
    @Bean
    fun mockQuestionGeneratorPort(): QuestionGeneratorPort = MockQuestionGenerator()
}

/**
 * Mock generator pour les tests
 */
class MockQuestionGenerator : QuestionGeneratorPort {
    override suspend fun generateQuestions(config: com.examloop.features.examgeneration.domain.ExamGenerationConfig): com.examloop.features.examgeneration.domain.GenerationResult {
        val questions = (1..config.questionCount).map { i ->
            com.examloop.features.examgeneration.domain.GeneratedQuestion(
                prompt = "Question mock #$i sur ${config.topic}",
                type = config.questionTypes.first(),
                choices = listOf(
                    com.examloop.features.examgeneration.domain.GeneratedChoice("Option A", false),
                    com.examloop.features.examgeneration.domain.GeneratedChoice("Option B", true),
                    com.examloop.features.examgeneration.domain.GeneratedChoice("Option C", false),
                    com.examloop.features.examgeneration.domain.GeneratedChoice("Option D", false)
                ),
                answer = "Option B",
                explanation = "Explication mock pour la question $i",
                difficulty = com.examloop.features.learning.domain.QuestionDifficulty.MEDIUM,
                chapter = "Chapitre Mock",
                tags = listOf("mock", "test")
            )
        }

        return com.examloop.features.examgeneration.domain.GenerationResult(
            questions = questions,
            topicAnalysis = com.examloop.features.examgeneration.domain.TopicAnalysis(
                mainTopic = config.topic,
                subtopics = listOf("Sous-thème 1", "Sous-thème 2"),
                suggestedChapters = listOf("Chapitre 1", "Chapitre 2"),
                estimatedDepth = com.examloop.features.examgeneration.domain.TopicDepth.INTERMEDIATE,
                relatedConcepts = listOf("Concept 1", "Concept 2")
            ),
            generationMetadata = com.examloop.features.examgeneration.domain.GenerationMetadata(
                model = "mock",
                tokensUsed = 0,
                generationTimeMs = 100
            )
        )
    }

    override suspend fun analyzeTopic(topic: String): com.examloop.features.examgeneration.domain.TopicAnalysis {
        return com.examloop.features.examgeneration.domain.TopicAnalysis(
            mainTopic = topic,
            subtopics = listOf("Sous-thème 1", "Sous-thème 2"),
            suggestedChapters = listOf("Chapitre 1", "Chapitre 2"),
            estimatedDepth = com.examloop.features.examgeneration.domain.TopicDepth.INTERMEDIATE,
            relatedConcepts = listOf("Concept 1", "Concept 2")
        )
    }
}
