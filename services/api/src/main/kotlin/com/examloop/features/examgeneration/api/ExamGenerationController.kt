package com.examloop.features.examgeneration.api

import com.examloop.features.examgeneration.application.*
import com.examloop.features.examgeneration.domain.*
import com.examloop.features.learning.api.ProblemDetail
import com.examloop.features.learning.domain.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * ExamGenerationController — API pour la génération d'examens par IA
 */
@RestController
@RequestMapping("/api/v1/exams")
class ExamGenerationController(
    private val generateExamUseCase: GenerateExamUseCase
) {
    private val log = LoggerFactory.getLogger(ExamGenerationController::class.java)

    @PostMapping("/generate")
    fun generateExam(
        @RequestHeader("X-Device-Id") deviceId: String,
        @RequestBody request: GenerateExamRequest
    ): ResponseEntity<*> = runBlocking {
        log.info("🧠 POST /exams/generate - topic: {}, count: {}", request.topic, request.questionCount)

        // Validation
        if (request.topic.isBlank()) {
            return@runBlocking ResponseEntity.badRequest().body(
                ProblemDetail(
                    title = "Validation error",
                    status = 400,
                    detail = "Le sujet est requis",
                    errorCode = "VALIDATION_FAILED"
                )
            )
        }

        if (request.questionCount !in 1..50) {
            return@runBlocking ResponseEntity.badRequest().body(
                ProblemDetail(
                    title = "Validation error",
                    status = 400,
                    detail = "Le nombre de questions doit être entre 1 et 50",
                    errorCode = "VALIDATION_FAILED"
                )
            )
        }

        val command = GenerateExamUseCase.Command(
            userId = UserId(deviceId),
            topic = request.topic.trim(),
            description = request.description?.trim(),
            questionCount = request.questionCount,
            difficultyMix = request.difficultyMix?.let { 
                try { DifficultyMix.valueOf(it) } catch (e: Exception) { DifficultyMix.BALANCED }
            } ?: DifficultyMix.BALANCED,
            questionTypes = request.questionTypes?.mapNotNull { 
                try { QuestionType.valueOf(it) } catch (e: Exception) { null }
            } ?: listOf(QuestionType.SINGLE_CHOICE),
            language = request.language ?: "fr"
        )

        when (val result = generateExamUseCase.execute(command)) {
            is GenerateExamResult.Success -> {
                ResponseEntity.status(HttpStatus.CREATED).body(
                    GenerateExamResponse(
                        examId = result.goal.id.value,
                        title = result.goal.title,
                        description = result.goal.description,
                        questionsGenerated = result.questions.size,
                        questions = result.questions.map { it.toResponse() },
                        topicAnalysis = result.topicAnalysis.toResponse(),
                        metadata = result.metadata.toResponse()
                    )
                )
            }
            is GenerateExamResult.NotPremium -> {
                ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(
                    ProblemDetail(
                        title = "Premium required",
                        status = 402,
                        detail = result.message,
                        errorCode = "PREMIUM_REQUIRED",
                        meta = mapOf(
                            "upgradeUrl" to "/api/v1/billing/checkout"
                        )
                    )
                )
            }
            is GenerateExamResult.GenerationFailed -> {
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    ProblemDetail(
                        title = "Generation failed",
                        status = 503,
                        detail = result.message,
                        errorCode = "GENERATION_FAILED"
                    )
                )
            }
        }
    }
}

// ============================================================================
// DTOs
// ============================================================================

data class GenerateExamRequest(
    val topic: String,
    val description: String? = null,
    val questionCount: Int = 10,
    val difficultyMix: String? = null, // EASY_FOCUSED, BALANCED, HARD_FOCUSED, PROGRESSIVE
    val questionTypes: List<String>? = null, // SINGLE_CHOICE, MULTIPLE_CHOICE, OPEN
    val language: String? = "fr"
)

data class GenerateExamResponse(
    val examId: Long,
    val title: String,
    val description: String?,
    val questionsGenerated: Int,
    val questions: List<GeneratedQuestionResponse>,
    val topicAnalysis: TopicAnalysisResponse,
    val metadata: GenerationMetadataResponse
)

data class GeneratedQuestionResponse(
    val id: Long,
    val prompt: String,
    val type: String,
    val choices: List<ChoiceResponse>?,
    val difficulty: String,
    val chapter: String?,
    val tags: List<String>
)

data class ChoiceResponse(
    val id: String,
    val label: String
    // isCorrect not exposed
)

data class TopicAnalysisResponse(
    val mainTopic: String,
    val subtopics: List<String>,
    val suggestedChapters: List<String>,
    val depth: String
)

data class GenerationMetadataResponse(
    val model: String,
    val tokensUsed: Int,
    val generationTimeMs: Long
)

// ============================================================================
// MAPPERS
// ============================================================================

private fun Question.toResponse() = GeneratedQuestionResponse(
    id = id.value,
    prompt = prompt,
    type = type.name,
    choices = choices?.map { ChoiceResponse(id = it.id.toString(), label = it.label) },
    difficulty = difficulty.name,
    chapter = chapter,
    tags = tags
)

private fun TopicAnalysis.toResponse() = TopicAnalysisResponse(
    mainTopic = mainTopic,
    subtopics = subtopics,
    suggestedChapters = suggestedChapters,
    depth = estimatedDepth.name
)

private fun GenerationMetadata.toResponse() = GenerationMetadataResponse(
    model = model,
    tokensUsed = tokensUsed,
    generationTimeMs = generationTimeMs
)
