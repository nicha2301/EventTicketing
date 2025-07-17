package com.nicha.eventticketing.data.remote.dto.organizer

/**
 * DTO cho thông tin tổ chức sự kiện
 */
data class OrganizerDto(
    val id: String,
    val name: String,
    val description: String?,
    val email: String,
    val phone: String?,
    val website: String?,
    val logoUrl: String?,
    val bannerUrl: String?,
    val socialLinks: Map<String, String>?,
    val userId: String,
    val isVerified: Boolean,
    val createdAt: String,
    val updatedAt: String
)

/**
 * DTO cho việc tạo Organizer mới
 */
data class OrganizerCreateDto(
    val name: String,
    val description: String?,
    val email: String,
    val phone: String?,
    val website: String?,
    val logoUrl: String?,
    val bannerUrl: String?,
    val socialLinks: Map<String, String>?
)

/**
 * DTO cho việc cập nhật Organizer
 */
data class OrganizerUpdateDto(
    val name: String?,
    val description: String?,
    val email: String?,
    val phone: String?,
    val website: String?,
    val logoUrl: String?,
    val bannerUrl: String?,
    val socialLinks: Map<String, String>?
) 