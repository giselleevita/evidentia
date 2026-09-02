package com.evidentia.rating.adapters.web

import com.evidentia.common.domain.TenantId
import com.evidentia.common.security.SecurityConfig
import com.evidentia.common.web.RateLimitFilter
import com.evidentia.common.web.TenantFilter
import com.evidentia.rating.application.RatingRepository
import com.evidentia.rating.application.RatingService
import com.evidentia.rating.domain.Rating
import com.evidentia.rating.domain.RatingId
import com.evidentia.rating.domain.RatingValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.UUID

@WebMvcTest(RatingController::class)
@Import(
    SecurityConfig::class,
    TenantFilter::class,
    RateLimitFilter::class,
    RatingControllerSecurityTest.TestBeans::class,
)
class RatingControllerSecurityTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repository: RecordingRatingRepository

    @AfterEach
    fun resetState() {
        repository.clear()
    }

    @Test
    fun `my ratings requires authentication`() {
        mockMvc.get("/api/v1/ratings/my-ratings")
            .andExpect {
                status { isUnauthorized() }
            }

        assertEquals(emptyList<TenantId>(), repository.findByRaterTenants)
    }

    @Test
    fun `authenticated rating requests require a tenant claim`() {
        mockMvc.get("/api/v1/ratings/my-ratings") {
            with(jwt().authorities(SimpleGrantedAuthority("ROLE_User")))
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(emptyList<TenantId>(), repository.findByRaterTenants)
    }

    @Test
    fun `service role cannot access rating endpoints`() {
        mockMvc.get("/api/v1/ratings/my-ratings") {
            with(jwtFor("Service"))
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(emptyList<TenantId>(), repository.findByRaterTenants)
    }

    @Test
    fun `tenant header mismatch is rejected before rating service access`() {
        mockMvc.get("/api/v1/ratings/my-ratings") {
            with(jwtFor("User", tenantId = "tenant-a"))
            header("X-Tenant-Id", "tenant-b")
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(emptyList<TenantId>(), repository.findByRaterTenants)
    }

    @Test
    fun `create rating returns ApiResponse with tenant scoped data`() {
        mockMvc.post("/api/v1/ratings") {
            with(jwtFor("User", subject = "alice"))
            contentType = MediaType.APPLICATION_JSON
            content = """{"resourceType":"Evidence","resourceId":"ev-1","value":5,"comment":"useful"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.tenantId") { value("tenant-a") }
            jsonPath("$.data.raterId") { value("alice") }
            jsonPath("$.data.value") { value(5) }
            jsonPath("$.error") { doesNotExist() }
        }

        assertEquals(listOf(TenantId("tenant-a")), repository.savedTenants)
    }

    @Test
    fun `my ratings returns only ratings for validated tenant and subject`() {
        repository.seed(ratingFor(TenantId("tenant-a"), "alice", "ev-1"))
        repository.seed(ratingFor(TenantId("tenant-a"), "bob", "ev-2"))
        repository.seed(ratingFor(TenantId("tenant-b"), "alice", "ev-3"))

        mockMvc.get("/api/v1/ratings/my-ratings") {
            with(jwtFor("User", tenantId = "tenant-a", subject = "alice"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.length()") { value(1) }
            jsonPath("$.data[0].tenantId") { value("tenant-a") }
            jsonPath("$.data[0].raterId") { value("alice") }
        }

        assertEquals(listOf(TenantId("tenant-a")), repository.findByRaterTenants)
    }

    private fun jwtFor(
        role: String,
        tenantId: String = "tenant-a",
        subject: String = "${role.lowercase()}@example.com",
    ): JwtRequestPostProcessor =
        jwt()
            .jwt {
                it.subject(subject)
                    .claim("tid", tenantId)
                    .claim("roles", listOf(role))
            }
            .authorities(SimpleGrantedAuthority("ROLE_$role"))

    private fun ratingFor(
        tenantId: TenantId,
        raterId: String,
        resourceId: String,
    ): Rating =
        Rating(
            id = RatingId(UUID.randomUUID()),
            tenantId = tenantId,
            raterId = raterId,
            resourceType = "Evidence",
            resourceId = resourceId,
            value = RatingValue.FOUR,
        )

    @TestConfiguration
    class TestBeans {
        @Bean
        fun ratingRepository(): RecordingRatingRepository = RecordingRatingRepository()

        @Bean
        fun ratingService(repository: RatingRepository): RatingService = RatingService(repository)
    }

    class RecordingRatingRepository : RatingRepository {
        private val ratings = mutableMapOf<RatingId, Rating>()
        val findByRaterTenants = mutableListOf<TenantId>()
        val savedTenants = mutableListOf<TenantId>()

        fun seed(rating: Rating) {
            ratings[rating.id] = rating
        }

        fun clear() {
            ratings.clear()
            findByRaterTenants.clear()
            savedTenants.clear()
        }

        override fun save(rating: Rating): Rating {
            savedTenants += rating.tenantId
            ratings[rating.id] = rating
            return rating
        }

        override fun findById(id: RatingId): Rating? = ratings[id]

        override fun findByTenantIdAndResource(
            tenantId: TenantId,
            resourceType: String,
            resourceId: String,
        ): List<Rating> =
            ratings.values.filter {
                it.tenantId == tenantId &&
                    it.resourceType == resourceType &&
                    it.resourceId == resourceId
            }

        override fun findByTenantIdAndRaterId(tenantId: TenantId, raterId: String): List<Rating> {
            findByRaterTenants += tenantId
            return ratings.values.filter { it.tenantId == tenantId && it.raterId == raterId }
        }

        override fun findByTenantIdAndResourceAndRaterId(
            tenantId: TenantId,
            resourceType: String,
            resourceId: String,
            raterId: String,
        ): Rating? =
            ratings.values.find {
                it.tenantId == tenantId &&
                    it.resourceType == resourceType &&
                    it.resourceId == resourceId &&
                    it.raterId == raterId
            }

        override fun delete(id: RatingId) {
            ratings.remove(id)
        }

        override fun getAverageRatingForResource(
            tenantId: TenantId,
            resourceType: String,
            resourceId: String,
        ): Double? =
            findByTenantIdAndResource(tenantId, resourceType, resourceId)
                .map { it.value.value }
                .average()
                .takeUnless { it.isNaN() }

        override fun getRatingCountForResource(
            tenantId: TenantId,
            resourceType: String,
            resourceId: String,
        ): Int =
            findByTenantIdAndResource(tenantId, resourceType, resourceId).size
    }
}
