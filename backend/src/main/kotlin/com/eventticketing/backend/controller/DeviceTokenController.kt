package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.dto.notification.DeviceTokenRequest
import com.eventticketing.backend.dto.notification.DeviceTokenResponse
import com.eventticketing.backend.dto.notification.TopicSubscriptionRequest
import com.eventticketing.backend.service.DeviceTokenService
import com.eventticketing.backend.service.FirebaseMessagingService
import com.eventticketing.backend.util.ResponseBuilder
import com.eventticketing.backend.util.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Device Token API", description = "API để quản lý token thiết bị cho push notification")
@SecurityRequirement(name = "bearerAuth")
class DeviceTokenController(
    private val deviceTokenService: DeviceTokenService,
    private val firebaseMessagingService: FirebaseMessagingService,
    private val securityUtils: SecurityUtils
) {
    
    @PostMapping("/tokens")
    @Operation(summary = "Đăng ký token thiết bị mới")
    @PreAuthorize("isAuthenticated()")
    fun registerDeviceToken(
        @RequestBody request: DeviceTokenRequest
    ): ResponseEntity<ApiResponse<DeviceTokenResponse>> {
        val userId = securityUtils.getCurrentUserId()
        val response = deviceTokenService.registerToken(userId, request)
        return ResponseBuilder.created(response, "Đăng ký token thiết bị thành công")
    }
    
    @GetMapping("/tokens")
    @Operation(summary = "Lấy tất cả token thiết bị của người dùng hiện tại")
    @PreAuthorize("isAuthenticated()")
    fun getUserDeviceTokens(): ResponseEntity<ApiResponse<List<DeviceTokenResponse>>> {
        val userId = securityUtils.getCurrentUserId()
        val tokens = deviceTokenService.getTokensByUserId(userId)
        return ResponseBuilder.success(tokens, "Lấy danh sách token thiết bị thành công")
    }
    
    @DeleteMapping("/tokens/{tokenId}")
    @Operation(summary = "Xóa token thiết bị")
    @PreAuthorize("isAuthenticated()")
    fun deleteDeviceToken(@PathVariable tokenId: UUID): ResponseEntity<ApiResponse<Boolean>> {
        val userId = securityUtils.getCurrentUserId()
        val success = deviceTokenService.deleteToken(userId, tokenId)
        
        return if (success) {
            ResponseBuilder.success(true, "Xóa token thiết bị thành công")
        } else {
            ResponseBuilder.notFound("Không tìm thấy token hoặc bạn không có quyền xóa")
        }
    }
    
    @DeleteMapping("/tokens")
    @Operation(summary = "Xóa tất cả token thiết bị của người dùng hiện tại")
    @PreAuthorize("isAuthenticated()")
    fun deleteAllDeviceTokens(): ResponseEntity<ApiResponse<Int>> {
        val userId = securityUtils.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val count = deviceTokenService.deleteAllTokens(userId)
        return ResponseBuilder.success(count, "Đã xóa $count token thiết bị")
    }
    
    @PostMapping("/topic/subscribe")
    @Operation(summary = "Đăng ký nhận thông báo theo chủ đề")
    @PreAuthorize("isAuthenticated()")
    fun subscribeToTopic(@RequestBody request: TopicSubscriptionRequest): ResponseEntity<ApiResponse<Boolean>> {
        val userId = securityUtils.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val deviceTokens = deviceTokenService.getActiveTokensByUserId(userId)
        
        if (deviceTokens.isEmpty()) {
            return ResponseBuilder.error("Không tìm thấy token thiết bị nào. Hãy đăng ký token trước.", HttpStatus.BAD_REQUEST)
        }
        
        var successCount = 0
        for (token in deviceTokens) {
            if (firebaseMessagingService.subscribeToTopic(token.token, request.topic)) {
                successCount++
            }
        }
        
        return if (successCount > 0) {
            ResponseBuilder.success(
                true,
                "Đã đăng ký $successCount/${deviceTokens.size} thiết bị vào chủ đề ${request.topic}"
            )
        } else {
            ResponseBuilder.error(
                "Không thể đăng ký vào chủ đề", 
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }
    
    @PostMapping("/topic/unsubscribe")
    @Operation(summary = "Hủy đăng ký nhận thông báo theo chủ đề")
    @PreAuthorize("isAuthenticated()")
    fun unsubscribeFromTopic(@RequestBody request: TopicSubscriptionRequest): ResponseEntity<ApiResponse<Boolean>> {
        val userId = securityUtils.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val deviceTokens = deviceTokenService.getActiveTokensByUserId(userId)
        
        if (deviceTokens.isEmpty()) {
            return ResponseBuilder.error("Không tìm thấy token thiết bị nào.", HttpStatus.BAD_REQUEST)
        }
        
        var successCount = 0
        for (token in deviceTokens) {
            if (firebaseMessagingService.unsubscribeFromTopic(token.token, request.topic)) {
                successCount++
            }
        }
        
        return if (successCount > 0) {
            ResponseBuilder.success(
                true, 
                "Đã hủy đăng ký $successCount/${deviceTokens.size} thiết bị khỏi chủ đề ${request.topic}"
            )
        } else {
            ResponseBuilder.error(
                "Không thể hủy đăng ký khỏi chủ đề", 
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }
} 