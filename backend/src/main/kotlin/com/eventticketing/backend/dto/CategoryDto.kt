package com.eventticketing.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.*

data class CategoryDto(
    val id: UUID? = null,
    
    @field:NotBlank(message = "Tên danh mục không được để trống")
    @field:Size(min = 2, max = 50, message = "Tên danh mục phải từ 2 đến 50 ký tự")
    val name: String,
    
    @field:Size(max = 255, message = "Mô tả danh mục không được vượt quá 255 ký tự")
    val description: String? = null,
    
    val iconUrl: String? = null,
    
    val isActive: Boolean = true,
    
    val createdAt: LocalDateTime? = null,
    
    val updatedAt: LocalDateTime? = null
) 