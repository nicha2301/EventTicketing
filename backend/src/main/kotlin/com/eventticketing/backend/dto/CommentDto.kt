package com.eventticketing.backend.dto

import com.eventticketing.backend.entity.CommentStatus
import java.time.LocalDateTime
import java.util.*

data class CommentDto(
    val id: UUID?,
    val content: String,
    val eventId: UUID,
    val userId: UUID,
    val username: String,
    val userAvatar: String?,
    val parentId: UUID?,
    val status: CommentStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val replyCount: Int = 0,
    val replies: List<CommentDto> = emptyList()
)

data class CommentRequest(
    val content: String,
    val eventId: UUID,
    val parentId: UUID? = null
)

data class CommentUpdateRequest(
    val content: String
)

data class CommentStatusUpdateRequest(
    val status: CommentStatus
)

data class CommentReportRequest(
    val reason: String
)

data class CommentResponse(
    val id: UUID?,
    val content: String,
    val eventId: UUID,
    val eventTitle: String,
    val userId: UUID,
    val username: String,
    val userAvatar: String?,
    val parentId: UUID?,
    val status: CommentStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val replyCount: Int = 0,
    val replies: List<CommentDto>? = null
)

data class CommentPageResponse(
    val comments: List<CommentResponse>,
    val currentPage: Int,
    val totalItems: Long,
    val totalPages: Int
) 