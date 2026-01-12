package com.evidentia.rating.adapters.web

import com.evidentia.common.domain.TenantId
import com.evidentia.rating.adapters.web.dto.RatingDto
import com.evidentia.rating.adapters.web.dto.ResourceRatingSummaryDto
import com.evidentia.rating.adapters.web.dto.UserAccountDto
import com.evidentia.rating.adapters.web.dto.UserRatingStatisticsDto
import com.evidentia.rating.adapters.web.dto.CreateRatingRequest
import com.evidentia.rating.adapters.web.dto.UpdateRatingRequest
import com.evidentia.rating.adapters.web.mapper.RatingMapper
import com.evidentia.rating.application.RatingService
import com.evidentia.rating.domain.RatingId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/ratings")
class RatingController(
    private val ratingService: RatingService
) {
    @PostMapping
    fun createRating(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: CreateRatingRequest
    ): ResponseEntity<RatingDto> {
        val tenantId = extractTenantId(jwt)
        val raterId = extractUserId(jwt)
        
        val ratingValue = RatingMapper.toRatingValue(request.value)
        val rating = ratingService.createRating(
            tenantId = TenantId(tenantId),
            raterId = raterId,
            resourceType = request.resourceType,
            resourceId = request.resourceId,
            value = ratingValue,
            comment = request.comment
        )
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(RatingMapper.toDto(rating))
    }
    
    @PutMapping("/{ratingId}")
    fun updateRating(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable ratingId: UUID,
        @Valid @RequestBody request: UpdateRatingRequest
    ): ResponseEntity<RatingDto> {
        val tenantId = extractTenantId(jwt)
        
        val newValue = request.value?.let { RatingMapper.toRatingValue(it) }
        val rating = ratingService.updateRating(
            ratingId = RatingId(ratingId),
            tenantId = TenantId(tenantId),
            newValue = newValue,
            newComment = request.comment
        )
        
        return ResponseEntity.ok(RatingMapper.toDto(rating))
    }
    
    @DeleteMapping("/{ratingId}")
    fun deleteRating(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable ratingId: UUID
    ): ResponseEntity<Void> {
        val tenantId = extractTenantId(jwt)
        
        ratingService.deleteRating(
            ratingId = RatingId(ratingId),
            tenantId = TenantId(tenantId)
        )
        
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/{ratingId}")
    fun getRating(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable ratingId: UUID
    ): ResponseEntity<RatingDto> {
        val tenantId = extractTenantId(jwt)
        
        val rating = ratingService.getRating(
            ratingId = RatingId(ratingId),
            tenantId = TenantId(tenantId)
        )
        
        return ResponseEntity.ok(RatingMapper.toDto(rating))
    }
    
    @GetMapping("/resource/{resourceType}/{resourceId}")
    fun getRatingsForResource(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable resourceType: String,
        @PathVariable resourceId: String
    ): ResponseEntity<List<RatingDto>> {
        val tenantId = extractTenantId(jwt)
        
        val ratings = ratingService.getRatingsForResource(
            tenantId = TenantId(tenantId),
            resourceType = resourceType,
            resourceId = resourceId
        )
        
        return ResponseEntity.ok(ratings.map { RatingMapper.toDto(it) })
    }
    
    @GetMapping("/resource/{resourceType}/{resourceId}/summary")
    fun getResourceRatingSummary(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable resourceType: String,
        @PathVariable resourceId: String
    ): ResponseEntity<ResourceRatingSummaryDto> {
        val tenantId = extractTenantId(jwt)
        
        val summary = ratingService.getResourceRatingSummary(
            tenantId = TenantId(tenantId),
            resourceType = resourceType,
            resourceId = resourceId
        )
        
        return ResponseEntity.ok(RatingMapper.toDto(summary))
    }
    
    @GetMapping("/resource/{resourceType}/{resourceId}/user")
    fun getUserRatingForResource(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable resourceType: String,
        @PathVariable resourceId: String
    ): ResponseEntity<RatingDto?> {
        val tenantId = extractTenantId(jwt)
        val raterId = extractUserId(jwt)
        
        val rating = ratingService.getOrCreateUserRating(
            tenantId = TenantId(tenantId),
            raterId = raterId,
            resourceType = resourceType,
            resourceId = resourceId
        )
        
        return ResponseEntity.ok(rating?.let { RatingMapper.toDto(it) })
    }
    
    @GetMapping("/user/{raterId}")
    fun getRatingsByUser(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable raterId: String
    ): ResponseEntity<List<RatingDto>> {
        val tenantId = extractTenantId(jwt)
        
        val ratings = ratingService.getRatingsByUser(
            tenantId = TenantId(tenantId),
            raterId = raterId
        )
        
        return ResponseEntity.ok(ratings.map { RatingMapper.toDto(it) })
    }
    
    @GetMapping("/my-ratings")
    fun getMyRatings(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<RatingDto>> {
        val tenantId = extractTenantId(jwt)
        val raterId = extractUserId(jwt)
        
        val ratings = ratingService.getRatingsByUser(
            tenantId = TenantId(tenantId),
            raterId = raterId
        )
        
        return ResponseEntity.ok(ratings.map { RatingMapper.toDto(it) })
    }
    
    @GetMapping("/account/me")
    fun getMyAccount(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<UserAccountDto> {
        val tenantId = extractTenantId(jwt)
        val userId = extractUserId(jwt)
        val email = extractEmail(jwt)
        val name = extractName(jwt)
        
        val account = UserAccountDto(
            userId = userId,
            email = email,
            name = name,
            tenantId = tenantId
        )
        
        return ResponseEntity.ok(account)
    }
    
    @GetMapping("/account/me/statistics")
    fun getMyRatingStatistics(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<UserRatingStatisticsDto> {
        val tenantId = extractTenantId(jwt)
        val raterId = extractUserId(jwt)
        
        val statistics = ratingService.getUserRatingStatistics(
            tenantId = TenantId(tenantId),
            raterId = raterId
        )
        
        return ResponseEntity.ok(RatingMapper.toDto(statistics))
    }
    
    @GetMapping("/user/{raterId}/statistics")
    fun getUserRatingStatistics(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable raterId: String
    ): ResponseEntity<UserRatingStatisticsDto> {
        val tenantId = extractTenantId(jwt)
        
        val statistics = ratingService.getUserRatingStatistics(
            tenantId = TenantId(tenantId),
            raterId = raterId
        )
        
        return ResponseEntity.ok(RatingMapper.toDto(statistics))
    }
    
    private fun extractTenantId(jwt: Jwt): String {
        return jwt.getClaimAsString("tid") 
            ?: jwt.getClaimAsString("tenant_id") 
            ?: throw IllegalStateException("No tenant ID in token")
    }
    
    private fun extractUserId(jwt: Jwt): String {
        return jwt.getClaimAsString("sub")
            ?: jwt.getClaimAsString("oid")
            ?: jwt.subject
            ?: throw IllegalStateException("No user ID in token")
    }
    
    private fun extractEmail(jwt: Jwt): String? {
        return jwt.getClaimAsString("email")
            ?: jwt.getClaimAsString("preferred_username")
            ?: jwt.getClaimAsString("upn")
    }
    
    private fun extractName(jwt: Jwt): String? {
        return jwt.getClaimAsString("name")
            ?: run {
                val given = jwt.getClaimAsString("given_name")
                val family = jwt.getClaimAsString("family_name")
                when {
                    given != null && family != null -> "$given $family"
                    given != null -> given
                    family != null -> family
                    else -> null
                }
            }
    }
}
