package com.nicha.eventticketing.data.remote.dto.auth

/**
 * DTO cho yêu cầu đặt lại mật khẩu
 */
data class ResetPasswordRequestDto(
    val token: String,
    val password: String,
    val confirmPassword: String
) 