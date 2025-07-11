package com.eventticketing.backend.dto.notification

import com.eventticketing.backend.entity.NotificationType
import java.time.LocalDateTime
import java.util.*

data class NotificationDto(
    val id: UUID? = null,
    val userId: UUID? = null,
    val title: String,
    val content: String,
    val notificationType: NotificationType,
    val referenceId: UUID? = null,
    val referenceType: String? = null,
    val isRead: Boolean = false,
    val readAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null
)

data class NotificationResponse(
    val id: UUID,
    val title: String,
    val content: String,
    val notificationType: NotificationType,
    val referenceId: UUID? = null,
    val referenceType: String? = null,
    val isRead: Boolean,
    val readAt: LocalDateTime? = null,
    val createdAt: LocalDateTime
)

data class NotificationCountResponse(
    val unreadCount: Long
)

data class NotificationPreferencesRequest(
    val email: NotificationChannelPreference = NotificationChannelPreference(),
    val push: NotificationChannelPreference = NotificationChannelPreference()
)

data class NotificationChannelPreference(
    val enabled: Boolean = true,
    val accountNotifications: Boolean = true,
    val eventReminders: Boolean = true,
    val commentNotifications: Boolean = true,
    val ratingNotifications: Boolean = true,
    val marketingNotifications: Boolean = false
) 