package com.nicha.eventticketing.domain.model

import java.util.Date

/**
 * Domain model cho User, được sử dụng trong business logic
 */
data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String?,
    val phoneNumber: String?,
    val role: UserRole,
    val isVerified: Boolean,
    val createdAt: Date,
    val updatedAt: Date
)

enum class UserRole {
    USER, ORGANIZER, ADMIN, UNKNOWN;
    
    companion object {
        fun fromString(role: String): UserRole {
            return try {
                valueOf(role.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
} 