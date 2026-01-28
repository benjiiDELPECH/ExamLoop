package com.examloop

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.prompt.ChatOptions

@SpringBootApplication(exclude = [
    org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration::class
])
class ExamLoopApplication

fun main(args: Array<String>) {
    runApplication<ExamLoopApplication>(*args)
}

/**
 * Mock AI Configuration for local testing without OpenAI API key
 */
@Configuration
class MockAiConfiguration {
    
    @Bean
    fun chatModel(): ChatModel = object : ChatModel {
        override fun call(prompt: Prompt): ChatResponse {
            val mockResponse = """
            {
              "mainTopic": "Spring Framework",
              "subtopics": ["Spring Core", "Spring Boot", "Spring Security"],
              "suggestedChapters": ["IoC", "DI", "AOP"],
              "estimatedDepth": "INTERMEDIATE",
              "relatedConcepts": ["Java", "Maven", "REST"]
            }
            """.trimIndent()
            return ChatResponse(listOf(Generation(AssistantMessage(mockResponse))))
        }
        
        override fun getDefaultOptions(): ChatOptions? = null
    }
}
