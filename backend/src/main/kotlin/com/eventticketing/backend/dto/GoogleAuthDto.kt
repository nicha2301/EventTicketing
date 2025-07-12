package com.eventticketing.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * DTO cho request đăng nhập bằng Google
 */
data class GoogleAuthRequestDto(
    @field:NotBlank(message = "ID token không được để trống")
    val idToken: String,
    
    @field:NotBlank(message = "Email không được để trống")
    @field:Email(message = "Email không hợp lệ")
    val email: String,
    
    @field:NotBlank(message = "Tên không được để trống")
    val name: String,
    
    val profilePictureUrl: String? = null
) 