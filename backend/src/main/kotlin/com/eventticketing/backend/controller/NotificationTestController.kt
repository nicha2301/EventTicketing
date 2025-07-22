package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.dto.notification.NotificationTestRequest
import com.eventticketing.backend.service.FirebaseMessagingService
import com.eventticketing.backend.util.PushNotificationService
import com.eventticketing.backend.util.ResponseBuilder
import com.eventticketing.backend.util.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test API", description = "API để kiểm tra các chức năng của hệ thống")
@SecurityRequirement(name = "bearerAuth")
class NotificationTestController(
    private val pushNotificationService: PushNotificationService,
    private val securityUtils: SecurityUtils
) {
    
    @PostMapping("/notification")
    @Operation(summary = "Gửi thông báo đẩy thử nghiệm đến thiết bị của người dùng hiện tại")
    @PreAuthorize("isAuthenticated()")
    fun sendTestNotification(
        @RequestBody request: NotificationTestRequest
    ): ResponseEntity<ApiResponse<Boolean>> {
        val userId = securityUtils.getCurrentUserId()
        
        val title = request.title ?: "Thông báo thử nghiệm"
        val body = request.body ?: "Đây là một thông báo thử nghiệm từ hệ thống Event Ticketing"
        val data = request.data ?: mapOf("type" to "TEST_NOTIFICATION")
        
        pushNotificationService.sendNotification(userId.toString(), title, body, data)
        
        return ResponseBuilder.success(
            true,
            "Đã gửi thông báo thử nghiệm đến các thiết bị của bạn"
        )
    }
} 