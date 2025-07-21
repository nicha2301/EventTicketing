package com.nicha.eventticketing.data.remote.dto.notification

/**
 * DTO cho thông tin thông báo
 */
data class NotificationDto(
    val id: String,
    val title: String,
    val content: String,
    val notificationType: String,
    val referenceId: String? = null,
    val referenceType: String? = null,
    val isRead: Boolean = false,
    val readAt: String? = null,
    val createdAt: String
)

/**
 * DTO cho số lượng thông báo chưa đọc
 */
data class UnreadCountDto(
    val unreadCount: Int
)

/**
 * DTO cho kết quả đánh dấu đọc tất cả thông báo
 */
data class MarkAllReadResultDto(
    val markedCount: Int
)

/**
 * DTO cho kết quả xóa tất cả thông báo
 */
data class DeleteAllResultDto(
    val deletedCount: Int
)

/**
 * DTO cho cài đặt thông báo của người dùng
 */
data class NotificationPreferencesDto(
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val inAppNotifications: Boolean = true,
    val eventReminders: Boolean = true,
    val commentNotifications: Boolean = true,
    val ratingNotifications: Boolean = true,
    val ticketUpdates: Boolean = true,
    val marketingNotifications: Boolean = false
)