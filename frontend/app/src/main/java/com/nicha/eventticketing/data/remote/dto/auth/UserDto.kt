package com.nicha.eventticketing.data.remote.dto.auth

import java.util.Date
import com.squareup.moshi.Json

data class UserDto(
    val id: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String?,
    val role: String,
    val enabled: Boolean,
    val profilePictureUrl: String? = null,
    val createdAt: String? = null
)

data class UserCreateDto(
    val email: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String?,
    val role: String = "USER"
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class UserAuthResponseDto(
    val id: String,
    val email: String,
    val fullName: String,
    val role: String,
    val token: String,
    @Json(name = "tokenType") val tokenType: String = "Bearer",
    @Json(name = "expiresIn") val expiresIn: Int = 86400,
    val profilePictureUrl: String? = null
)

data class UserUpdateDto(
    val fullName: String?,
    val phoneNumber: String?,
    val profilePictureUrl: String? = null,
    val enabled: Boolean? = null
)

data class ChangePasswordDto(
    val currentPassword: String,
    val newPassword: String
) 