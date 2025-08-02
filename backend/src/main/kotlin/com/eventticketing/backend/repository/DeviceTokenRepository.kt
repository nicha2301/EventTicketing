package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.DeviceToken
import com.eventticketing.backend.entity.DeviceType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DeviceTokenRepository : JpaRepository<DeviceToken, UUID> {
    
    /**
     * Tìm token theo userId
     */
    fun findByUserId(userId: UUID): List<DeviceToken>
    
    /**
     * Tìm token theo userId và isActive
     */
    fun findByUserIdAndIsActiveTrue(userId: UUID): List<DeviceToken>
    
    /**
     * Tìm token theo token value
     */
    fun findByToken(token: String): List<DeviceToken>
    
    /**
     * Tìm token duy nhất theo token value
     */
    fun findFirstByToken(token: String): Optional<DeviceToken>
    
    /**
     * Tìm token theo userId và deviceType
     */
    fun findByUserIdAndDeviceType(userId: UUID, deviceType: DeviceType): List<DeviceToken>
    
    /**
     * Xóa token theo userId
     */
    fun deleteByUserId(userId: UUID): Int
    
    /**
     * Xóa token theo token value
     */
    fun deleteByToken(token: String): Int
} 