package com.examloop.features.examgeneration.infra

import com.examloop.features.examgeneration.application.QuestionGeneratorPort
import com.examloop.features.examgeneration.domain.*
import com.examloop.features.learning.domain.QuestionDifficulty
import com.examloop.features.learning.domain.QuestionType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * SpringAiQuestionGenerator — Implémentation avec Spring AI
 * 
 * Utilise un LLM (OpenAI, Claude, etc.) pour générer des questions.
 */
@Component
class SpringAiQuestionGenerator(
    private val chatModel: ChatModel,
    private val objectMapper: ObjectMapper,
    @Value("\${examloop.ai.model:gpt-4o-mini}") private val modelName: String
) : QuestionGeneratorPort {

    private val log = LoggerFactory.getLogger(SpringAiQuestionGenerator::class.java)
    private val chatClient = ChatClient.create(chatModel)

    override suspend fun generateQuestions(config: ExamGenerationConfig): GenerationResult {
        log.info("🤖 Generating {} questions for topic: {}", config.questionCount, config.topic)
        
        val startTime = System.currentTimeMillis()

        // 1. Analyser le sujet
        val topicAnalysis = analyzeTopic(config.topic)

        // 2. Générer les questions
        val prompt = buildGenerationPrompt(config, topicAnalysis)
        
        val response = chatClient.prompt()
            .user(prompt)
            .call()
            .content() ?: throw RuntimeException("Empty response from AI model")

        // 3. Parser la réponse
        val questions = parseQuestionsFromResponse(response, config)

        val generationTime = System.currentTimeMillis() - startTime
        log.info("✅ Generated {} questions in {}ms", questions.size, generationTime)

        return GenerationResult(
            questions = questions,
            topicAnalysis = topicAnalysis,
            generationMetadata = GenerationMetadata(
                model = modelName,
                tokensUsed = estimateTokens(prompt + response),
                generationTimeMs = generationTime
            )
        )
    }

    override suspend fun analyzeTopic(topic: String): TopicAnalysis {
        val prompt = """
            Analyse le sujet suivant pour un examen technique:
            
            Sujet: $topic
            
            Réponds en JSON avec ce format exact:
            {
              "mainTopic": "nom principal du sujet",
              "subtopics": ["sous-thème 1", "sous-thème 2", ...],
              "suggestedChapters": ["chapitre 1", "chapitre 2", ...],
              "estimatedDepth": "BEGINNER|INTERMEDIATE|ADVANCED|EXPERT",
              "relatedConcepts": ["concept lié 1", "concept lié 2", ...]
            }
            
            Retourne UNIQUEMENT le JSON, sans markdown ni explication.
        """.trimIndent()

        val response = chatClient.prompt()
            .user(prompt)
            .call()
            .content() ?: return TopicAnalysis(
                mainTopic = topic,
                subtopics = emptyList(),
                suggestedChapters = emptyList(),
                estimatedDepth = TopicDepth.INTERMEDIATE,
                relatedConcepts = emptyList()
            )

        return try {
            val cleanJson = response.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            
            val json = objectMapper.readTree(cleanJson)
            
            TopicAnalysis(
                mainTopic = json.get("mainTopic")?.asText() ?: topic,
                subtopics = json.get("subtopics")?.map { it.asText() } ?: emptyList(),
                suggestedChapters = json.get("suggestedChapters")?.map { it.asText() } ?: emptyList(),
                estimatedDepth = try {
                    TopicDepth.valueOf(json.get("estimatedDepth")?.asText() ?: "INTERMEDIATE")
                } catch (e: Exception) {
                    TopicDepth.INTERMEDIATE
                },
                relatedConcepts = json.get("relatedConcepts")?.map { it.asText() } ?: emptyList()
            )
        } catch (e: Exception) {
            log.warn("⚠️ Failed to parse topic analysis, using defaults: {}", e.message)
            TopicAnalysis(
                mainTopic = topic,
                subtopics = emptyList(),
                suggestedChapters = emptyList(),
                estimatedDepth = TopicDepth.INTERMEDIATE,
                relatedConcepts = emptyList()
            )
        }
    }

    private fun buildGenerationPrompt(config: ExamGenerationConfig, analysis: TopicAnalysis): String {
        val difficultyDistribution = when (config.targetDifficulty) {
            DifficultyMix.EASY_FOCUSED -> "60% EASY, 30% MEDIUM, 10% HARD"
            DifficultyMix.BALANCED -> "30% EASY, 40% MEDIUM, 30% HARD"
            DifficultyMix.HARD_FOCUSED -> "10% EASY, 30% MEDIUM, 60% HARD"
            DifficultyMix.PROGRESSIVE -> "commence par EASY, termine par HARD"
        }

        val questionTypeInstructions = config.questionTypes.joinToString("\n") { type ->
            when (type) {
                QuestionType.SINGLE_CHOICE -> "- SINGLE_CHOICE: 4 choix, une seule bonne réponse"
                QuestionType.MULTIPLE_CHOICE -> "- MULTIPLE_CHOICE: 4-6 choix, plusieurs bonnes réponses possibles"
                QuestionType.OPEN -> "- OPEN: question ouverte, réponse textuelle attendue"
            }
        }

        return """
            Tu es un expert en création de questions d'examen technique de haute qualité.
            
            SUJET: ${config.topic}
            ${config.description?.let { "DESCRIPTION: $it" } ?: ""}
            
            ANALYSE DU SUJET:
            - Thème principal: ${analysis.mainTopic}
            - Sous-thèmes à couvrir: ${analysis.subtopics.joinToString(", ")}
            - Chapitres suggérés: ${analysis.suggestedChapters.joinToString(", ")}
            - Concepts liés: ${analysis.relatedConcepts.joinToString(", ")}
            
            CONFIGURATION:
            - Nombre de questions: ${config.questionCount}
            - Répartition difficulté: $difficultyDistribution
            - Types de questions:
            $questionTypeInstructions
            - Langue: ${config.language}
            - Inclure explications: ${config.includeExplanations}
            
            RÈGLES DE QUALITÉ:
            1. Chaque question doit tester un concept spécifique
            2. Les mauvaises réponses (MCQ) doivent être plausibles
            3. Les explications doivent être pédagogiques
            4. Éviter les questions ambiguës
            5. Varier les sous-thèmes couverts
            6. La bonne réponse ne doit pas être évidente par sa formulation
            
            FORMAT DE RÉPONSE (JSON array):
            [
              {
                "prompt": "Question claire et précise",
                "type": "SINGLE_CHOICE|MULTIPLE_CHOICE|OPEN",
                "choices": [
                  {"label": "Réponse A", "isCorrect": false},
                  {"label": "Réponse B", "isCorrect": true},
                  {"label": "Réponse C", "isCorrect": false},
                  {"label": "Réponse D", "isCorrect": false}
                ],
                "answer": "La bonne réponse (texte pour OPEN, ou label pour MCQ)",
                "explanation": "Explication pédagogique détaillée",
                "difficulty": "EASY|MEDIUM|HARD|VERY_HARD",
                "chapter": "Nom du chapitre/section",
                "tags": ["tag1", "tag2"]
              }
            ]
            
            IMPORTANT: Retourne UNIQUEMENT le JSON array, sans markdown ni texte additionnel.
            Génère exactement ${config.questionCount} questions.
        """.trimIndent()
    }

    private fun parseQuestionsFromResponse(response: String, config: ExamGenerationConfig): List<GeneratedQuestion> {
        return try {
            val cleanJson = response.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonArray = objectMapper.readTree(cleanJson)
            
            jsonArray.mapNotNull { node ->
                try {
                    GeneratedQuestion(
                        prompt = node.get("prompt")?.asText() ?: return@mapNotNull null,
                        type = try {
                            QuestionType.valueOf(node.get("type")?.asText() ?: "SINGLE_CHOICE")
                        } catch (e: Exception) {
                            QuestionType.SINGLE_CHOICE
                        },
                        choices = node.get("choices")?.map { choice ->
                            GeneratedChoice(
                                label = choice.get("label")?.asText() ?: "",
                                isCorrect = choice.get("isCorrect")?.asBoolean() ?: false
                            )
                        },
                        answer = node.get("answer")?.asText() ?: "",
                        explanation = node.get("explanation")?.asText(),
                        difficulty = try {
                            QuestionDifficulty.valueOf(node.get("difficulty")?.asText() ?: "MEDIUM")
                        } catch (e: Exception) {
                            QuestionDifficulty.MEDIUM
                        },
                        chapter = node.get("chapter")?.asText(),
                        tags = node.get("tags")?.map { it.asText() } ?: emptyList()
                    )
                } catch (e: Exception) {
                    log.warn("⚠️ Failed to parse question: {}", e.message)
                    null
                }
            }
        } catch (e: Exception) {
            log.error("❌ Failed to parse questions response: {}", e.message)
            emptyList()
        }
    }

    private fun estimateTokens(text: String): Int {
        // Estimation grossière: ~4 chars par token
        return text.length / 4
    }
}
