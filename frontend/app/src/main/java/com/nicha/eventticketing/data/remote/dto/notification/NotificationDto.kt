package com.nicha.eventticketing.data.remote.dto.notification

/**
 * DTO cho thông tin thông báo
 */
data class NotificationDto(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val relatedId: String?,
    val relatedType: String?,
    val createdAt: String,
    val imageUrl: String?
)

/**
 * DTO cho tùy chọn thông báo
 */
data class NotificationPreferencesDto(
    val email: NotificationChannelPreferencesDto,
    val push: NotificationChannelPreferencesDto
)

/**
 * DTO cho tùy chọn kênh thông báo
 */
data class NotificationChannelPreferencesDto(
    val enabled: Boolean,
    val accountNotifications: Boolean,
    val eventReminders: Boolean,
    val commentNotifications: Boolean,
    val ratingNotifications: Boolean,
    val marketingNotifications: Boolean
) 