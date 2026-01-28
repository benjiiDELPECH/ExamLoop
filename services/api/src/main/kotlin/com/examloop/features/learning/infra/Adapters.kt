package com.examloop.features.learning.infra

import com.examloop.features.learning.application.*
import com.examloop.features.learning.domain.*
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

/**
 * Adapters — Implémentation des Ports avec JPA
 * 
 * Ces adapters font le mapping entre les entités JPA et les modèles domain.
 */

@Component
class ProfileAdapter(
    private val repository: ProfileJpaRepository
) : ProfilePort {

    override fun findById(userId: UserId): Profile? =
        repository.findById(userId.value).orElse(null)?.toDomain()

    override fun save(profile: Profile): Profile {
        val entity = profile.toEntity()
        return repository.save(entity).toDomain()
    }

    override fun existsById(userId: UserId): Boolean =
        repository.existsById(userId.value)

    private fun ProfileEntity.toDomain() = Profile(
        userId = UserId(userId),
        premium = premium,
        createdAt = createdAt
    )

    private fun Profile.toEntity() = ProfileEntity(
        userId = userId.value,
        premium = premium,
        createdAt = createdAt
    )
}

@Component
class GoalAdapter(
    private val repository: GoalJpaRepository
) : GoalPort {

    override fun findById(id: GoalId): Goal? =
        repository.findById(id.value).orElse(null)?.toDomain()

    override fun findByUserId(userId: UserId): List<Goal> =
        repository.findByDeviceId(userId.value).map { it.toDomain() }

    override fun findPublicGoals(): List<Goal> =
        repository.findByIsPublicTrue().map { it.toDomain() }

    override fun save(goal: Goal): Goal {
        val entity = goal.toEntity()
        return repository.save(entity).toDomain()
    }

    override fun existsById(id: GoalId): Boolean =
        repository.existsById(id.value)

    private fun GoalEntity.toDomain() = Goal(
        id = GoalId(id!!),
        userId = UserId(deviceId),
        title = title,
        description = description,
        isPublic = isPublic,
        createdAt = createdAt
    )

    private fun Goal.toEntity() = GoalEntity(
        id = if (id.value == 0L) null else id.value,
        deviceId = userId.value,
        title = title,
        description = description,
        isPublic = isPublic,
        createdAt = createdAt,
        updatedAt = Instant.now()
    )
}

@Component
class QuestionAdapter(
    private val repository: QuestionJpaRepository
) : QuestionPort {

    override fun findById(id: QuestionId): Question? =
        repository.findById(id.value).orElse(null)?.toDomain()

    override fun findByGoalId(goalId: GoalId): List<Question> =
        repository.findByGoalId(goalId.value).map { it.toDomain() }

    override fun findByGoalIdAndDifficulty(goalId: GoalId, difficulty: QuestionDifficulty): List<Question> =
        repository.findByGoalIdAndDifficulty(goalId.value, difficulty.name)
            .map { it.toDomain() }

    override fun save(question: Question): Question {
        val entity = question.toEntity()
        return repository.save(entity).toDomain()
    }

    override fun count(): Long = repository.count()

    private fun QuestionEntity.toDomain() = Question(
        id = QuestionId(id!!),
        goalId = GoalId(goalId),
        type = QuestionType.valueOf(itemType),
        prompt = question,
        answer = answer,
        explanation = explanation,
        difficulty = QuestionDifficulty.valueOf(difficulty),
        chapter = chapter,
        createdAt = createdAt
    )

    private fun Question.toEntity() = QuestionEntity(
        id = if (id.value == 0L) null else id.value,
        goalId = goalId.value,
        deviceId = "SYSTEM",
        question = prompt,
        answer = answer,
        explanation = explanation,
        difficulty = difficulty.name,
        chapter = chapter,
        itemType = type.name,
        createdAt = createdAt,
        updatedAt = Instant.now()
    )
}

@Component
class QuestionAttemptAdapter(
    private val repository: QuestionAttemptJpaRepository
) : QuestionAttemptPort {

    override fun findByUserId(userId: UserId): List<QuestionAttempt> =
        repository.findByUserId(userId.value).map { it.toDomain() }

    override fun findByUserIdAndQuestionId(userId: UserId, questionId: QuestionId): List<QuestionAttempt> =
        repository.findByUserIdAndQuestionId(userId.value, questionId.value)
            .map { it.toDomain() }

    override fun findByUserIdAndGoalId(userId: UserId, goalId: GoalId): List<QuestionAttempt> =
        repository.findByUserIdAndGoalId(userId.value, goalId.value)
            .map { it.toDomain() }

    override fun findRecentByUserId(userId: UserId, limit: Int): List<QuestionAttempt> =
        repository.findByUserIdOrderByTimestampDesc(userId.value)
            .take(limit)
            .map { it.toDomain() }

    override fun save(attempt: QuestionAttempt): QuestionAttempt {
        val entity = attempt.toEntity()
        return repository.save(entity).toDomain()
    }

    override fun countByUserId(userId: UserId): Long =
        repository.countByUserId(userId.value)

    private fun QuestionAttemptEntity.toDomain() = QuestionAttempt(
        id = id,
        questionId = QuestionId(questionId),
        userId = UserId(userId),
        isCorrect = isCorrect,
        timestamp = timestamp
    )

    private fun QuestionAttempt.toEntity() = QuestionAttemptEntity(
        id = id,
        questionId = questionId.value,
        userId = userId.value,
        isCorrect = isCorrect,
        timestamp = timestamp
    )
}

@Component
class ReviewStateAdapter(
    private val repository: ReviewStateJpaRepository
) : ReviewStatePort {

    override fun findByUserIdAndQuestionId(userId: UserId, questionId: QuestionId): ReviewState? =
        repository.findByUserIdAndQuestionId(userId.value, questionId.value)
            ?.toDomain()

    override fun findByUserId(userId: UserId): List<ReviewState> =
        repository.findByUserId(userId.value).map { it.toDomain() }

    override fun findDueForReview(userId: UserId, now: Instant): List<ReviewState> =
        repository.findDueForReview(userId.value, now).map { it.toDomain() }

    override fun save(state: ReviewState): ReviewState {
        val entity = state.toEntity()
        return repository.save(entity).toDomain()
    }

    override fun saveAll(states: List<ReviewState>): List<ReviewState> =
        repository.saveAll(states.map { it.toEntity() }).map { it.toDomain() }

    private fun ReviewStateEntity.toDomain() = ReviewState(
        questionId = QuestionId(questionId),
        userId = UserId(userId),
        masteryLevel = MasteryLevel.valueOf(masteryLevel),
        successCount = successCount,
        failCount = failCount,
        lastReviewedAt = lastReviewedAt,
        nextReviewAt = nextReviewAt,
        optimalIntervalDays = optimalIntervalDays
    )

    private fun ReviewState.toEntity() = ReviewStateEntity(
        questionId = questionId.value,
        userId = userId.value,
        masteryLevel = masteryLevel.name,
        successCount = successCount,
        failCount = failCount,
        lastReviewedAt = lastReviewedAt,
        nextReviewAt = nextReviewAt,
        optimalIntervalDays = optimalIntervalDays
    )
}

@Component
class UsageDailyAdapter(
    private val repository: UsageDailyJpaRepository
) : UsageDailyPort {

    override fun findByUserIdAndDate(userId: UserId, date: LocalDate): UsageDaily? =
        repository.findByUserIdAndDay(userId.value, date)?.toDomain()

    override fun save(usage: UsageDaily): UsageDaily {
        val entity = usage.toEntity()
        return repository.save(entity).toDomain()
    }

    @Transactional
    override fun incrementReviewCount(userId: UserId, date: LocalDate): UsageDaily {
        val existing = repository.findByUserIdAndDay(userId.value, date)
        
        return if (existing != null) {
            existing.reviewsCount++
            repository.save(existing).toDomain()
        } else {
            val newUsage = UsageDailyEntity(
                userId = userId.value,
                day = date,
                reviewsCount = 1
            )
            repository.save(newUsage).toDomain()
        }
    }

    private fun UsageDailyEntity.toDomain() = UsageDaily(
        userId = UserId(userId),
        date = day,
        reviewsCount = reviewsCount
    )

    private fun UsageDaily.toEntity() = UsageDailyEntity(
        userId = userId.value,
        day = date,
        reviewsCount = reviewsCount
    )
}

@Component
class SystemClockAdapter : ClockPort {
    override fun now(): Instant = Instant.now()
    override fun today(): LocalDate = LocalDate.now()
}
