package com.eventticketing.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

data class CloudinaryImageRequest(
    @field:NotBlank(message = "Public ID không được để trống")
    val publicId: String,
    
    @field:NotBlank(message = "Secure URL không được để trống")
    val secureUrl: String,
    
    @field:PositiveOrZero(message = "Chiều rộng phải là số dương hoặc 0")
    val width: Int,
    
    @field:PositiveOrZero(message = "Chiều cao phải là số dương hoặc 0")
    val height: Int,
    
    val isPrimary: Boolean = false
)
