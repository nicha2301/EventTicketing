package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.notification.*
import com.eventticketing.backend.entity.NotificationType
import com.eventticketing.backend.service.DeviceTokenService
import com.eventticketing.backend.service.UserNotificationService
import com.eventticketing.backend.util.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification API", description = "API để quản lý thông báo và cài đặt thông báo")
@SecurityRequirement(name = "bearerAuth")
class NotificationController(
    private val userNotificationService: UserNotificationService,
    private val deviceTokenService: DeviceTokenService,
    private val securityUtils: SecurityUtils
) {
    
    @GetMapping
    @Operation(summary = "Lấy tất cả thông báo của người dùng hiện tại")
    @PreAuthorize("isAuthenticated()")
    fun getUserNotifications(
        @PageableDefault(size = 20, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<NotificationResponse>> {
        val userId = securityUtils.getCurrentUserId()
        return ResponseEntity.ok(userNotificationService.getUserNotifications(userId, pageable))
    }
    
    @GetMapping("/unread")
    @Operation(summary = "Lấy thông báo chưa đọc của người dùng hiện tại")
    @PreAuthorize("isAuthenticated()")
    fun getUnreadNotifications(
        @PageableDefault(size = 20, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<NotificationResponse>> {
        val userId = securityUtils.getCurrentUserId()
        return ResponseEntity.ok(userNotificationService.getUnreadNotifications(userId, pageable))
    }
    
    @GetMapping("/type/{type}")
    @Operation(summary = "Lấy thông báo theo loại")
    @PreAuthorize("isAuthenticated()")
    fun getNotificationsByType(
        @PathVariable type: NotificationType,
        @PageableDefault(size = 20, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<NotificationResponse>> {
        val userId = securityUtils.getCurrentUserId()
        return ResponseEntity.ok(userNotificationService.getNotificationsByType(userId, type, pageable))
    }
    
    @GetMapping("/count")
    @Operation(summary = "Đếm số thông báo chưa đọc")
    @PreAuthorize("isAuthenticated()")
    fun countUnreadNotifications(): ResponseEntity<NotificationCountResponse> {
        val userId = securityUtils.getCurrentUserId()
        return ResponseEntity.ok(userNotificationService.countUnreadNotifications(userId))
    }
    
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Đánh dấu thông báo là đã đọc")
    @PreAuthorize("isAuthenticated()")
    fun markAsRead(@PathVariable notificationId: UUID): ResponseEntity<NotificationResponse> {
        val userId = securityUtils.getCurrentUserId()
        return ResponseEntity.ok(userNotificationService.markAsRead(userId, notificationId))
    }
    
    @PutMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả thông báo là đã đọc")
    @PreAuthorize("isAuthenticated()")
    fun markAllAsRead(): ResponseEntity<Map<String, Int>> {
        val userId = securityUtils.getCurrentUserId()
        val count = userNotificationService.markAllAsRead(userId)
        return ResponseEntity.ok(mapOf("markedCount" to count))
    }
    
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Xóa thông báo")
    @PreAuthorize("isAuthenticated()")
    fun deleteNotification(@PathVariable notificationId: UUID): ResponseEntity<Void> {
        val userId = securityUtils.getCurrentUserId()
        val success = userNotificationService.deleteNotification(userId, notificationId)
        
        return if (success) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping
    @Operation(summary = "Xóa tất cả thông báo")
    @PreAuthorize("isAuthenticated()")
    fun deleteAllNotifications(): ResponseEntity<Map<String, Int>> {
        val userId = securityUtils.getCurrentUserId()
        val count = userNotificationService.deleteAllNotifications(userId)
        return ResponseEntity.ok(mapOf("deletedCount" to count))
    }
    
    @GetMapping("/preferences")
    @Operation(summary = "Lấy cài đặt thông báo")
    @PreAuthorize("isAuthenticated()")
    fun getNotificationPreferences(): ResponseEntity<NotificationPreferencesRequest> {
        val userId = securityUtils.getCurrentUserId()
        return ResponseEntity.ok(userNotificationService.getNotificationPreferences(userId))
    }
    
    @PutMapping("/preferences")
    @Operation(summary = "Cập nhật cài đặt thông báo")
    @PreAuthorize("isAuthenticated()")
    fun updateNotificationPreferences(
        @RequestBody preferences: NotificationPreferencesRequest
    ): ResponseEntity<NotificationPreferencesRequest> {
        val userId = securityUtils.getCurrentUserId()
        return ResponseEntity.ok(userNotificationService.updateNotificationPreferences(userId, preferences))
    }
    
    @PostMapping("/devices")
    @Operation(summary = "Đăng ký token thiết bị mới")
    @PreAuthorize("isAuthenticated()")
    fun registerDeviceToken(
        @RequestBody request: DeviceTokenRequest
    ): ResponseEntity<DeviceTokenResponse> {
        val userId = securityUtils.getCurrentUserId()
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(deviceTokenService.registerToken(userId, request))
    }
    
    @GetMapping("/devices")
    @Operation(summary = "Lấy tất cả token thiết bị")
    @PreAuthorize("isAuthenticated()")
    fun getDeviceTokens(): ResponseEntity<List<DeviceTokenResponse>> {
        val userId = securityUtils.getCurrentUserId()
        return ResponseEntity.ok(deviceTokenService.getTokensByUserId(userId))
    }
    
    @DeleteMapping("/devices/{tokenId}")
    @Operation(summary = "Xóa token thiết bị")
    @PreAuthorize("isAuthenticated()")
    fun deleteDeviceToken(@PathVariable tokenId: UUID): ResponseEntity<Void> {
        val userId = securityUtils.getCurrentUserId()
        val success = deviceTokenService.deleteToken(userId, tokenId)
        
        return if (success) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
} 