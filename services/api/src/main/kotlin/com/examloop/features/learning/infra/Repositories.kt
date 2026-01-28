package com.examloop.features.learning.infra

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Spring Data JPA Repositories
 */

@Repository
interface ProfileJpaRepository : JpaRepository<ProfileEntity, String>

@Repository
interface GoalJpaRepository : JpaRepository<GoalEntity, Long> {
    fun findByDeviceId(deviceId: String): List<GoalEntity>
    fun findByIsPublicTrue(): List<GoalEntity>
}

@Repository
interface QuestionJpaRepository : JpaRepository<QuestionEntity, Long> {
    fun findByGoalId(goalId: Long): List<QuestionEntity>
    fun findByGoalIdAndDifficulty(goalId: Long, difficulty: String): List<QuestionEntity>
    fun findByDeviceId(deviceId: String): List<QuestionEntity>
}

@Repository
interface QuestionAttemptJpaRepository : JpaRepository<QuestionAttemptEntity, UUID> {
    fun findByUserId(userId: String): List<QuestionAttemptEntity>
    fun findByUserIdAndQuestionId(userId: String, questionId: Long): List<QuestionAttemptEntity>
    fun findByUserIdOrderByTimestampDesc(userId: String): List<QuestionAttemptEntity>
    fun countByUserId(userId: String): Long
    
    @Query("SELECT a FROM QuestionAttemptEntity a WHERE a.userId = :userId AND a.questionId IN (SELECT q.id FROM QuestionEntity q WHERE q.goalId = :goalId)")
    fun findByUserIdAndGoalId(@Param("userId") userId: String, @Param("goalId") goalId: Long): List<QuestionAttemptEntity>
}

@Repository
interface ReviewStateJpaRepository : JpaRepository<ReviewStateEntity, ReviewStateId> {
    fun findByUserId(userId: String): List<ReviewStateEntity>
    fun findByUserIdAndQuestionId(userId: String, questionId: Long): ReviewStateEntity?
    
    @Query("SELECT r FROM ReviewStateEntity r WHERE r.userId = :userId AND r.nextReviewAt <= :now")
    fun findDueForReview(@Param("userId") userId: String, @Param("now") now: Instant): List<ReviewStateEntity>
}

@Repository
interface UsageDailyJpaRepository : JpaRepository<UsageDailyEntity, UsageDailyId> {
    fun findByUserIdAndDay(userId: String, day: LocalDate): UsageDailyEntity?
    
    @Modifying
    @Query("UPDATE UsageDailyEntity u SET u.reviewsCount = u.reviewsCount + 1 WHERE u.userId = :userId AND u.day = :day")
    fun incrementReviewCount(@Param("userId") userId: String, @Param("day") day: LocalDate): Int
}
