package com.eventticketing.backend.service

import com.eventticketing.backend.dto.notification.DeviceTokenRequest
import com.eventticketing.backend.dto.notification.DeviceTokenResponse
import com.eventticketing.backend.entity.DeviceType
import java.util.*

interface DeviceTokenService {
    
    /**
     * Đăng ký token thiết bị mới
     */
    fun registerToken(userId: UUID?, request: DeviceTokenRequest): DeviceTokenResponse
    
    /**
     * Lấy tất cả token của một người dùng
     */
    fun getTokensByUserId(userId: UUID?): List<DeviceTokenResponse>
    
    /**
     * Lấy tất cả token active của một người dùng
     */
    fun getActiveTokensByUserId(userId: UUID): List<DeviceTokenResponse>
    
    /**
     * Lấy tất cả token của một người dùng theo loại thiết bị
     */
    fun getTokensByUserIdAndDeviceType(userId: UUID, deviceType: DeviceType): List<DeviceTokenResponse>
    
    /**
     * Vô hiệu hóa token
     */
    fun deactivateToken(userId: UUID, tokenId: UUID): Boolean
    
    /**
     * Vô hiệu hóa tất cả token của một người dùng
     */
    fun deactivateAllTokens(userId: UUID): Int
    
    /**
     * Xóa token
     */
    fun deleteToken(userId: UUID?, tokenId: UUID): Boolean
    
    /**
     * Xóa tất cả token của một người dùng
     */
    fun deleteAllTokens(userId: UUID): Int
} 