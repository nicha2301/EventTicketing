package com.eventticketing.backend.dto.notification

import com.eventticketing.backend.entity.DeviceType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.*

data class DeviceTokenDto(
    val id: UUID? = null,
    val userId: UUID? = null,
    val token: String,
    val deviceType: DeviceType,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class DeviceTokenRequest(
    @field:NotBlank(message = "Token không được để trống")
    val token: String,
    
    @field:NotNull(message = "Loại thiết bị không được để trống")
    val deviceType: DeviceType
)

data class DeviceTokenResponse(
    val id: UUID,
    val token: String,
    val deviceType: DeviceType,
    val isActive: Boolean,
    val createdAt: LocalDateTime
) 