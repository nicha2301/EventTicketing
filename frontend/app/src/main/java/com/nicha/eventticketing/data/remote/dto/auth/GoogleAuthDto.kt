package com.nicha.eventticketing.data.remote.dto.auth

/**
 * DTO cho request đăng nhập bằng Google
 */
data class GoogleAuthRequestDto(
    val idToken: String,
    val email: String,
    val name: String,
    val profilePictureUrl: String?
) 