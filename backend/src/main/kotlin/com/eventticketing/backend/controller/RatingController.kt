package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.security.CurrentUser
import com.eventticketing.backend.service.RatingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/ratings")
@Tag(name = "Rating", description = "API để quản lý đánh giá")
class RatingController(private val ratingService: RatingService) {

    @PostMapping
    @Operation(summary = "Tạo hoặc cập nhật đánh giá", security = [SecurityRequirement(name = "bearer-key")])
    fun createOrUpdateRating(
        @Valid @RequestBody ratingRequest: RatingRequest,
        @CurrentUser currentUserId: UUID
    ): ResponseEntity<RatingResponse> {
        val rating = ratingService.createOrUpdateRating(ratingRequest, currentUserId)
        return ResponseEntity.status(HttpStatus.CREATED).body(rating)
    }

    @PutMapping("/{ratingId}")
    @Operation(summary = "Cập nhật đánh giá", security = [SecurityRequirement(name = "bearer-key")])
    fun updateRating(
        @PathVariable ratingId: UUID,
        @Valid @RequestBody ratingUpdateRequest: RatingUpdateRequest,
        @CurrentUser currentUserId: UUID
    ): ResponseEntity<RatingResponse> {
        val updatedRating = ratingService.updateRating(ratingId, ratingUpdateRequest, currentUserId)
        return ResponseEntity.ok(updatedRating)
    }

    @DeleteMapping("/{ratingId}")
    @Operation(summary = "Xóa đánh giá", security = [SecurityRequirement(name = "bearer-key")])
    fun deleteRating(
        @PathVariable ratingId: UUID,
        @CurrentUser currentUserId: UUID
    ): ResponseEntity<Void> {
        ratingService.deleteRating(ratingId, currentUserId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{ratingId}")
    @Operation(summary = "Lấy đánh giá theo ID")
    fun getRatingById(@PathVariable ratingId: UUID): ResponseEntity<RatingResponse> {
        val rating = ratingService.getRatingById(ratingId)
        return ResponseEntity.ok(rating)
    }

    @GetMapping("/events/{eventId}/user/{userId}")
    @Operation(summary = "Lấy đánh giá của người dùng cho sự kiện")
    fun getUserRatingForEvent(
        @PathVariable userId: UUID,
        @PathVariable eventId: UUID
    ): ResponseEntity<RatingResponse?> {
        val rating = ratingService.getUserRatingForEvent(userId, eventId)
        return if (rating != null) {
            ResponseEntity.ok(rating)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/events/{eventId}")
    @Operation(summary = "Lấy danh sách đánh giá của một sự kiện")
    fun getRatingsByEventId(
        @PathVariable eventId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<RatingPageResponse> {
        val ratings = ratingService.getRatingsByEventId(eventId, page, size)
        return ResponseEntity.ok(ratings)
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy danh sách đánh giá của người dùng hiện tại", security = [SecurityRequirement(name = "bearer-key")])
    fun getRatingsByCurrentUser(
        @CurrentUser currentUserId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<RatingPageResponse> {
        val ratings = ratingService.getRatingsByCurrentUser(currentUserId, page, size)
        return ResponseEntity.ok(ratings)
    }

    @PutMapping("/{ratingId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Cập nhật trạng thái đánh giá (Admin/Organizer)", security = [SecurityRequirement(name = "bearer-key")])
    fun updateRatingStatus(
        @PathVariable ratingId: UUID,
        @Valid @RequestBody statusUpdateRequest: RatingStatusUpdateRequest,
        @CurrentUser currentUserId: UUID
    ): ResponseEntity<RatingResponse> {
        val updatedRating = ratingService.updateRatingStatus(ratingId, statusUpdateRequest, currentUserId)
        return ResponseEntity.ok(updatedRating)
    }

    @PostMapping("/{ratingId}/report")
    @Operation(summary = "Báo cáo đánh giá không phù hợp", security = [SecurityRequirement(name = "bearer-key")])
    fun reportRating(
        @PathVariable ratingId: UUID,
        @Valid @RequestBody reportRequest: RatingReportRequest,
        @CurrentUser currentUserId: UUID
    ): ResponseEntity<Void> {
        ratingService.reportRating(ratingId, reportRequest, currentUserId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/admin/reported")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách đánh giá bị báo cáo (Admin)", security = [SecurityRequirement(name = "bearer-key")])
    fun getReportedRatings(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<RatingPageResponse> {
        val ratings = ratingService.getReportedRatings(page, size)
        return ResponseEntity.ok(ratings)
    }

    @GetMapping("/events/{eventId}/statistics")
    @Operation(summary = "Lấy thống kê đánh giá của một sự kiện")
    fun getRatingStatistics(@PathVariable eventId: UUID): ResponseEntity<RatingStatisticsResponse> {
        val statistics = ratingService.getRatingStatistics(eventId)
        return ResponseEntity.ok(statistics)
    }
} 