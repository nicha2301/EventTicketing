package com.eventticketing.backend.dto.notification

import com.eventticketing.backend.entity.NotificationType
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

data class TestNotificationRequest(
    @Schema(description = "ID của người dùng nhận thông báo (để trống nếu gửi cho người dùng hiện tại)")
    val userId: UUID? = null,
    
    @Schema(description = "Tiêu đề thông báo", example = "Nhắc nhở sự kiện", required = true)
    val title: String,
    
    @Schema(description = "Nội dung thông báo", example = "Sự kiện sẽ diễn ra vào ngày mai", required = true)
    val content: String,
    
    @Schema(description = "Loại thông báo", example = "EVENT_REMINDER", required = true)
    val notificationType: NotificationType,
    
    @Schema(description = "ID tham chiếu (ví dụ: ID sự kiện)", example = "550e8400-e29b-41d4-a716-446655440000")
    val referenceId: UUID? = null,
    
    @Schema(description = "Loại tham chiếu", example = "EVENT")
    val referenceType: String? = null
) 