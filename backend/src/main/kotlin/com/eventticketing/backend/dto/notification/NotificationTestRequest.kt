package com.eventticketing.backend.dto.notification

/**
 * Request DTO để kiểm tra gửi push notification
 */
data class NotificationTestRequest(
    val title: String? = null,
    val body: String? = null,
    val data: Map<String, String>? = null
) 