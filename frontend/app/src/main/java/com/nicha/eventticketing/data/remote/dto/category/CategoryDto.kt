package com.nicha.eventticketing.data.remote.dto.category

/**
 * DTO cho thông tin danh mục
 */
data class CategoryDto(
    val id: String,
    val name: String,
    val description: String?,
    val iconUrl: String?,
    val isActive: Boolean,
    val parentId: String?,
    val parentName: String?,
    val createdAt: String?,
    val updatedAt: String?
)

/**
 * DTO cho việc tạo Category mới
 */
data class CategoryCreateDto(
    val name: String,
    val description: String?,
    val iconUrl: String?
)

/**
 * DTO cho việc cập nhật Category
 */
data class CategoryUpdateDto(
    val name: String?,
    val description: String?,
    val iconUrl: String?
)
