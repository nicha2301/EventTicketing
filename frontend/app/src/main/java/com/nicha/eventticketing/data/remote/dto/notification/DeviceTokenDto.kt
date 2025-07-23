package com.nicha.eventticketing.data.remote.dto.notification

/**
 * DTO cho token thiết bị
 */
data class DeviceTokenDto(
    val id: String? = null,
    val token: String,
    val deviceType: String, // ANDROID, IOS, WEB
    val isActive: Boolean = true,
    val createdAt: String? = null
)

/**
 * DTO cho yêu cầu đăng ký token thiết bị
 */
data class DeviceTokenRequestDto(
    val token: String,
    val deviceType: String // ANDROID, IOS, WEB
)

/**
 * DTO cho yêu cầu đăng ký/hủy đăng ký topic
 */
data class TopicSubscriptionDto(
    val topic: String
) 