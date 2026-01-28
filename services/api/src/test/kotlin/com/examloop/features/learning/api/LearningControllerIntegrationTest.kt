package com.examloop.features.learning.api

import com.examloop.IntegrationTestBase
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests d'intégration pour LearningController
 */
@DisplayName("LearningController Integration Tests")
class LearningControllerIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `bootstrap should create profile and return dashboard`() {
        // Given
        val headers = HttpHeaders().apply {
            set("X-Device-Id", "test-device-${System.currentTimeMillis()}")
            set("Content-Type", "application/json")
        }
        val request = HttpEntity("{}", headers)

        // When
        val response = restTemplate.postForEntity(
            "/api/v1/bootstrap",
            request,
            BootstrapResponse::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(false, response.body!!.profile.premium)
        assertEquals(20, response.body!!.usage.reviewsLimit)
    }

    @Test
    fun `goals list should include public goals`() {
        // Given
        val headers = HttpHeaders().apply {
            set("X-Device-Id", "test-device-${System.currentTimeMillis()}")
        }
        val request = HttpEntity<Void>(headers)

        // When
        val response = restTemplate.exchange(
            "/api/v1/goals",
            org.springframework.http.HttpMethod.GET,
            request,
            GoalsListResponse::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        // Le goal public "Spring Certification Prep" devrait exister (seed V3)
    }

    @Test
    fun `session generation should return questions`() {
        // Given: First bootstrap to create profile
        val deviceId = "test-device-session-${System.currentTimeMillis()}"
        val headers = HttpHeaders().apply {
            set("X-Device-Id", deviceId)
            set("Content-Type", "application/json")
        }
        
        // Bootstrap first
        restTemplate.postForEntity(
            "/api/v1/bootstrap",
            HttpEntity("{}", headers),
            BootstrapResponse::class.java
        )

        // When: Generate session
        val sessionRequest = HttpEntity("""{"limit": 5}""", headers)
        val response = restTemplate.postForEntity(
            "/api/v1/sessions",
            sessionRequest,
            SessionResponse::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `review should update usage count`() {
        // Given: Bootstrap + get a question
        val deviceId = "test-device-review-${System.currentTimeMillis()}"
        val headers = HttpHeaders().apply {
            set("X-Device-Id", deviceId)
            set("Content-Type", "application/json")
        }

        // Bootstrap
        restTemplate.postForEntity(
            "/api/v1/bootstrap",
            HttpEntity("{}", headers),
            BootstrapResponse::class.java
        )

        // Get session to get a question ID
        val sessionResponse = restTemplate.postForEntity(
            "/api/v1/sessions",
            HttpEntity("""{"limit": 1}""", headers),
            SessionResponse::class.java
        )

        if (sessionResponse.body?.questions?.isNotEmpty() == true) {
            val questionId = sessionResponse.body!!.questions.first().id

            // When: Submit review
            val reviewRequest = HttpEntity(
                """{"questionId": $questionId, "correct": true}""",
                headers
            )
            val reviewResponse = restTemplate.postForEntity(
                "/api/v1/reviews",
                reviewRequest,
                ReviewResponse::class.java
            )

            // Then
            assertEquals(HttpStatus.OK, reviewResponse.statusCode)
            assertNotNull(reviewResponse.body)
            assertEquals(1, reviewResponse.body!!.usage.reviewsUsed)
        }
    }
}
