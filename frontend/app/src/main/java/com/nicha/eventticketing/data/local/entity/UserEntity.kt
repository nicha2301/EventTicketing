package com.nicha.eventticketing.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String?,
    val email: String?,
    val phone: String?,
    val avatarUrl: String?,
    val role: String?,
    val gender: String?,
    val birthday: String?,
    val address: String?,
    val createdAt: String?,
    val updatedAt: String?
) 