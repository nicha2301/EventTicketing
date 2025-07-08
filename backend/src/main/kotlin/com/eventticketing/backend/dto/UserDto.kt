package com.eventticketing.backend.dto

import com.eventticketing.backend.entity.UserRole
import java.time.LocalDateTime
import java.util.*

data class UserDto(
    val id: UUID?,
    val email: String,
    val fullName: String,
    val phoneNumber: String?,
    val role: UserRole,
    val enabled: Boolean,
    val createdAt: LocalDateTime
)

data class UserCreateDto(
    val email: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String?,
    val role: UserRole = UserRole.USER
)

data class UserUpdateDto(
    val fullName: String?,
    val phoneNumber: String?,
    val enabled: Boolean?
)

data class UserAuthResponseDto(
    val id: UUID,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val token: String
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class PasswordChangeDto(
    val currentPassword: String,
    val newPassword: String
) 