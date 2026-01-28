package com.examloop.features.learning.application

import com.examloop.features.learning.domain.*
import java.time.Instant
import java.time.LocalDate

/**
 * Ports — Interfaces définissant les contrats avec l'infrastructure
 * 
 * Ces interfaces vivent dans la couche Application et sont implémentées
 * par la couche Infra. Le domain n'a aucune connaissance de ces ports.
 */

// ============================================================================
// QUESTION PORT
// ============================================================================

interface QuestionPort {
    fun findById(id: QuestionId): Question?
    fun findByGoalId(goalId: GoalId): List<Question>
    fun findByGoalIdAndDifficulty(goalId: GoalId, difficulty: QuestionDifficulty): List<Question>
    fun save(question: Question): Question
    fun count(): Long
}

// ============================================================================
// GOAL PORT
// ============================================================================

interface GoalPort {
    fun findById(id: GoalId): Goal?
    fun findByUserId(userId: UserId): List<Goal>
    fun findPublicGoals(): List<Goal>
    fun save(goal: Goal): Goal
    fun existsById(id: GoalId): Boolean
}

data class Goal(
    val id: GoalId,
    val userId: UserId,
    val title: String,
    val description: String?,
    val isPublic: Boolean = false,
    val createdAt: Instant = Instant.now()
)

// ============================================================================
// QUESTION ATTEMPT PORT
// ============================================================================

interface QuestionAttemptPort {
    fun findByUserId(userId: UserId): List<QuestionAttempt>
    fun findByUserIdAndQuestionId(userId: UserId, questionId: QuestionId): List<QuestionAttempt>
    fun findByUserIdAndGoalId(userId: UserId, goalId: GoalId): List<QuestionAttempt>
    fun findRecentByUserId(userId: UserId, limit: Int): List<QuestionAttempt>
    fun save(attempt: QuestionAttempt): QuestionAttempt
    fun countByUserId(userId: UserId): Long
}

// ============================================================================
// REVIEW STATE PORT
// ============================================================================

interface ReviewStatePort {
    fun findByUserIdAndQuestionId(userId: UserId, questionId: QuestionId): ReviewState?
    fun findByUserId(userId: UserId): List<ReviewState>
    fun findDueForReview(userId: UserId, now: Instant): List<ReviewState>
    fun save(state: ReviewState): ReviewState
    fun saveAll(states: List<ReviewState>): List<ReviewState>
}

// ============================================================================
// PROFILE PORT
// ============================================================================

interface ProfilePort {
    fun findById(userId: UserId): Profile?
    fun save(profile: Profile): Profile
    fun existsById(userId: UserId): Boolean
}

data class Profile(
    val userId: UserId,
    val premium: Boolean = false,
    val createdAt: Instant = Instant.now()
)

// ============================================================================
// USAGE DAILY PORT (Quota tracking)
// ============================================================================

interface UsageDailyPort {
    fun findByUserIdAndDate(userId: UserId, date: LocalDate): UsageDaily?
    fun save(usage: UsageDaily): UsageDaily
    fun incrementReviewCount(userId: UserId, date: LocalDate): UsageDaily
}

data class UsageDaily(
    val userId: UserId,
    val date: LocalDate,
    val reviewsCount: Int = 0
)

// ============================================================================
// CLOCK PORT (For testability)
// ============================================================================

interface ClockPort {
    fun now(): Instant
    fun today(): LocalDate
}
