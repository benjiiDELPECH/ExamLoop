package com.examloop.features.learning.infra

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * JPA Entities — Mapping vers la base de données
 * 
 * Ces entités sont séparées des modèles domain pour :
 * - Éviter les annotations JPA dans le domain
 * - Permettre des mappings différents
 * - Garder le domain pur
 */

@Entity
@Table(name = "profiles")
class ProfileEntity(
    @Id
    @Column(name = "user_id")
    var userId: String = "",
    
    @Column(nullable = false)
    var premium: Boolean = false,
    
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "goals")
class GoalEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    
    @Column(name = "device_id", nullable = false)
    var deviceId: String = "",
    
    @Column(nullable = false)
    var title: String = "",
    
    @Column
    var description: String? = null,
    
    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = false,
    
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "items")
class QuestionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    
    @Column(name = "goal_id", nullable = false)
    var goalId: Long = 0,
    
    @Column(name = "device_id", nullable = false)
    var deviceId: String = "",
    
    @Column(nullable = false, columnDefinition = "TEXT")
    var question: String = "",
    
    @Column(nullable = false, columnDefinition = "TEXT")
    var answer: String = "",
    
    @Column(nullable = false)
    var box: Int = 1,
    
    @Column(name = "next_review")
    var nextReview: Instant? = null,
    
    @Column(nullable = false)
    var difficulty: String = "MEDIUM",
    
    @Column
    var chapter: String? = null,
    
    @Column(columnDefinition = "TEXT")
    var explanation: String? = null,
    
    @Column(name = "item_type", nullable = false)
    var itemType: String = "SINGLE_CHOICE",
    
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "question_attempts")
class QuestionAttemptEntity(
    @Id
    @Column(columnDefinition = "uuid")
    var id: UUID = UUID.randomUUID(),
    
    @Column(name = "question_id", nullable = false)
    var questionId: Long = 0,
    
    @Column(name = "user_id", nullable = false)
    var userId: String = "",
    
    @Column(name = "is_correct", nullable = false)
    var isCorrect: Boolean = false,
    
    @Column(name = "selected_choice_ids", columnDefinition = "TEXT")
    var selectedChoiceIds: String? = null,
    
    @Column(nullable = false)
    var timestamp: Instant = Instant.now()
)

@Entity
@Table(name = "review_states")
@IdClass(ReviewStateId::class)
class ReviewStateEntity(
    @Id
    @Column(name = "question_id")
    var questionId: Long = 0,
    
    @Id
    @Column(name = "user_id")
    var userId: String = "",
    
    @Column(name = "mastery_level", nullable = false)
    var masteryLevel: String = "NOVICE",
    
    @Column(name = "success_count", nullable = false)
    var successCount: Int = 0,
    
    @Column(name = "fail_count", nullable = false)
    var failCount: Int = 0,
    
    @Column(name = "last_reviewed_at")
    var lastReviewedAt: Instant? = null,
    
    @Column(name = "next_review_at", nullable = false)
    var nextReviewAt: Instant = Instant.now(),
    
    @Column(name = "optimal_interval_days", nullable = false)
    var optimalIntervalDays: Int = 1
)

data class ReviewStateId(
    var questionId: Long = 0,
    var userId: String = ""
) : java.io.Serializable

@Entity
@Table(name = "usage_daily")
@IdClass(UsageDailyId::class)
class UsageDailyEntity(
    @Id
    @Column(name = "user_id")
    var userId: String = "",
    
    @Id
    @Column(name = "day")
    var day: LocalDate = LocalDate.now(),
    
    @Column(name = "reviews_count", nullable = false)
    var reviewsCount: Int = 0
)

data class UsageDailyId(
    var userId: String = "",
    var day: LocalDate = LocalDate.now()
) : java.io.Serializable
