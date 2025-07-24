package com.nicha.eventticketing.domain.mapper

import com.nicha.eventticketing.data.local.entity.UserEntity
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.domain.model.User
import com.nicha.eventticketing.domain.model.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper để chuyển đổi giữa UserDto và User domain model
 */
@Singleton
class UserMapper @Inject constructor() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    fun mapToDomainModel(dto: UserDto): User {
        return User(
            id = dto.id,
            email = dto.email,
            fullName = dto.fullName,
            avatarUrl = dto.profilePictureUrl,
            phoneNumber = dto.phoneNumber,
            role = UserRole.fromString(dto.role),
            isVerified = dto.enabled,
            createdAt = dto.createdAt?.let { parseDate(it) } ?: Date(),
            updatedAt = Date() 
        )
    }

    fun dtoToEntity(dto: UserDto): UserEntity {
        return UserEntity(
            id = dto.id,
            fullName = dto.fullName,
            email = dto.email,
            phone = dto.phoneNumber,
            avatarUrl = dto.profilePictureUrl,
            role = dto.role,
            gender = null,
            birthday = null,
            address = null,
            createdAt = dto.createdAt,
            updatedAt = dto.createdAt
        )
    }
    
    fun entityToDto(entity: UserEntity): UserDto {
        return UserDto(
            id = entity.id,
            email = entity.email ?: "",
            fullName = entity.fullName ?: "",
            profilePictureUrl = entity.avatarUrl,
            phoneNumber = entity.phone,
            role = entity.role ?: "USER",
            enabled = true,
            createdAt = entity.createdAt
        )
    }

    private fun parseDate(dateString: String): Date {
        return try {
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
} 