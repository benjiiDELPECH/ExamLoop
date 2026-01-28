package com.examloop.shared.config

import com.examloop.features.learning.application.*
import com.examloop.features.learning.domain.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * UseCaseConfiguration — Configuration des Use Cases
 * 
 * Les Use Cases sont créés ici sans annotations Spring,
 * ce qui permet de les tester sans le contexte Spring.
 */
@Configuration
class UseCaseConfiguration {

    @Bean
    fun submitReviewUseCase(
        questionPort: QuestionPort,
        attemptPort: QuestionAttemptPort,
        reviewStatePort: ReviewStatePort,
        profilePort: ProfilePort,
        usageDailyPort: UsageDailyPort,
        clockPort: ClockPort
    ): SubmitReviewUseCase = SubmitReviewUseCase(
        questionPort = questionPort,
        attemptPort = attemptPort,
        reviewStatePort = reviewStatePort,
        profilePort = profilePort,
        usageDailyPort = usageDailyPort,
        clockPort = clockPort
    )

    @Bean
    fun generateSessionUseCase(
        questionPort: QuestionPort,
        goalPort: GoalPort,
        attemptPort: QuestionAttemptPort,
        reviewStatePort: ReviewStatePort,
        clockPort: ClockPort
    ): GenerateSessionUseCase = GenerateSessionUseCase(
        questionPort = questionPort,
        goalPort = goalPort,
        attemptPort = attemptPort,
        reviewStatePort = reviewStatePort,
        clockPort = clockPort
    )

    @Bean
    fun bootstrapUseCase(
        profilePort: ProfilePort,
        usageDailyPort: UsageDailyPort,
        reviewStatePort: ReviewStatePort,
        goalPort: GoalPort,
        questionPort: QuestionPort,
        clockPort: ClockPort
    ): BootstrapUseCase = BootstrapUseCase(
        profilePort = profilePort,
        usageDailyPort = usageDailyPort,
        reviewStatePort = reviewStatePort,
        goalPort = goalPort,
        questionPort = questionPort,
        clockPort = clockPort
    )
}
