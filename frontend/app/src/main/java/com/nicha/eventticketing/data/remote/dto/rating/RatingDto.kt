package com.nicha.eventticketing.data.remote.dto.rating

/**
 * DTO cho thông tin đánh giá
 */
data class RatingDto(
    val id: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    val eventId: String,
    val score: Int,
    val review: String?,
    val createdAt: String? = null,
    val updatedAt: String? = null
) 