package com.nicha.eventticketing.data.remote.dto.comment

/**
 * DTO cho thông tin bình luận
 */
data class CommentDto(
    val id: String,
    val content: String,
    val userId: String,
    val userName: String,
    val userProfilePicture: String?,
    val eventId: String,
    val parentId: String?,
    val createdAt: String,
    val updatedAt: String?,
    val replies: List<CommentDto>?
)

/**
 * DTO cho tạo bình luận mới
 */
data class CommentCreateDto(
    val content: String,
    val eventId: String,
    val parentId: String? = null
) 