package com.nicha.eventticketing.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String?,
    val address: String?,
    val bio: String?,
    val role: String,
    val enabled: Boolean,
    val createdAt: Date,
    val lastUpdatedLocally: Date = Date()
) 