package com.eventticketing.backend.dto

import com.eventticketing.backend.entity.RatingStatus
import java.time.LocalDateTime
import java.util.*

data class RatingDto(
    val id: UUID?,
    val score: Int,
    val review: String?,
    val eventId: UUID,
    val userId: UUID,
    val username: String,
    val userAvatar: String?,
    val status: RatingStatus,
    val isReported: Boolean,
    val reportReason: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class RatingRequest(
    val score: Int,
    val review: String?,
    val eventId: UUID
)

data class RatingUpdateRequest(
    val score: Int,
    val review: String?
)

data class RatingStatusUpdateRequest(
    val status: RatingStatus
)

data class RatingReportRequest(
    val reason: String
)

data class RatingResponse(
    val id: UUID?,
    val score: Int,
    val review: String?,
    val eventId: UUID,
    val eventTitle: String,
    val userId: UUID,
    val username: String,
    val userAvatar: String?,
    val status: RatingStatus,
    val isReported: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class RatingPageResponse(
    val ratings: List<RatingResponse>,
    val currentPage: Int,
    val totalItems: Long,
    val totalPages: Int
)

data class RatingStatisticsResponse(
    val eventId: UUID,
    val averageRating: Double,
    val totalRatings: Long,
    val ratingCounts: Map<Int, Long>  // Key: score (1-5), Value: count
) 