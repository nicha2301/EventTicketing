package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.notification.DeviceTokenRequest
import com.eventticketing.backend.dto.notification.DeviceTokenResponse
import com.eventticketing.backend.entity.DeviceToken
import com.eventticketing.backend.entity.DeviceType
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.repository.DeviceTokenRepository
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.service.DeviceTokenService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class DeviceTokenServiceImpl(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val userRepository: UserRepository
) : DeviceTokenService {
    
    private val logger = LoggerFactory.getLogger(DeviceTokenServiceImpl::class.java)
    
    @Transactional
    override fun registerToken(userId: UUID?, request: DeviceTokenRequest): DeviceTokenResponse {
        logger.debug("Registering device token for user: $userId")
        
        val user = userRepository.findById(userId!!)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }
        
        // Kiểm tra xem token đã tồn tại chưa
        val existingToken = deviceTokenRepository.findByToken(request.token)
        
        if (existingToken.isPresent) {
            val token = existingToken.get()
            // Nếu token đã tồn tại nhưng thuộc về user khác hoặc đã bị vô hiệu hóa
            if (token.user.id != userId || !token.isActive) {
                // Vô hiệu hóa token cũ (nếu thuộc về user khác)
                if (token.user.id != userId) {
                    deviceTokenRepository.deleteById(token.id!!)
                    logger.debug("Deleted existing token belonging to another user")
                }
                
                // Tạo token mới
                val newToken = DeviceToken(
                    user = user,
                    token = request.token,
                    deviceType = request.deviceType,
                    isActive = true
                )
                
                val savedToken = deviceTokenRepository.save(newToken)
                logger.debug("Created new device token: ${savedToken.id}")
                
                return mapToResponse(savedToken)
            } else {
                // Token đã tồn tại và thuộc về user hiện tại
                logger.debug("Token already exists for user: $userId")
                return mapToResponse(token)
            }
        } else {
            // Tạo token mới
            val newToken = DeviceToken(
                user = user,
                token = request.token,
                deviceType = request.deviceType,
                isActive = true
            )
            
            val savedToken = deviceTokenRepository.save(newToken)
            logger.debug("Created new device token: ${savedToken.id}")
            
            return mapToResponse(savedToken)
        }
    }
    
    override fun getTokensByUserId(userId: UUID?): List<DeviceTokenResponse> {
        logger.debug("Getting all tokens for user: $userId")
        
        return deviceTokenRepository.findByUserId(userId!!)
            .map { mapToResponse(it) }
    }
    
    override fun getActiveTokensByUserId(userId: UUID): List<DeviceTokenResponse> {
        logger.debug("Getting active tokens for user: $userId")
        
        return deviceTokenRepository.findByUserIdAndIsActiveTrue(userId)
            .map { mapToResponse(it) }
    }
    
    override fun getTokensByUserIdAndDeviceType(userId: UUID, deviceType: DeviceType): List<DeviceTokenResponse> {
        logger.debug("Getting tokens for user: $userId and device type: $deviceType")
        
        return deviceTokenRepository.findByUserIdAndDeviceType(userId, deviceType)
            .map { mapToResponse(it) }
    }
    
    @Transactional
    override fun deactivateToken(userId: UUID, tokenId: UUID): Boolean {
        logger.debug("Deactivating token: $tokenId for user: $userId")
        
        val token = deviceTokenRepository.findById(tokenId)
            .orElseThrow { ResourceNotFoundException("Token not found with id: $tokenId") }
        
        // Kiểm tra xem token có thuộc về user không
        if (token.user.id != userId) {
            logger.warn("Token $tokenId does not belong to user $userId")
            return false
        }
        
        // Vô hiệu hóa token
        val updatedToken = token.copy(isActive = false)
        deviceTokenRepository.save(updatedToken)
        logger.debug("Token deactivated: $tokenId")
        
        return true
    }
    
    @Transactional
    override fun deactivateAllTokens(userId: UUID): Int {
        logger.debug("Deactivating all tokens for user: $userId")
        
        val tokens = deviceTokenRepository.findByUserId(userId)
        
        tokens.forEach { token ->
            val updatedToken = token.copy(isActive = false)
            deviceTokenRepository.save(updatedToken)
        }
        
        logger.debug("Deactivated ${tokens.size} tokens for user: $userId")
        
        return tokens.size
    }
    
    @Transactional
    override fun deleteToken(userId: UUID?, tokenId: UUID): Boolean {
        logger.debug("Deleting token: $tokenId for user: $userId")
        
        val token = deviceTokenRepository.findById(tokenId)
            .orElseThrow { ResourceNotFoundException("Token not found with id: $tokenId") }
        
        // Kiểm tra xem token có thuộc về user không
        if (token.user.id != userId) {
            logger.warn("Token $tokenId does not belong to user $userId")
            return false
        }
        
        // Xóa token
        deviceTokenRepository.deleteById(tokenId)
        logger.debug("Token deleted: $tokenId")
        
        return true
    }
    
    @Transactional
    override fun deleteAllTokens(userId: UUID): Int {
        logger.debug("Deleting all tokens for user: $userId")
        
        val count = deviceTokenRepository.deleteByUserId(userId)
        logger.debug("Deleted $count tokens for user: $userId")
        
        return count
    }
    
    /**
     * Map DeviceToken entity sang DeviceTokenResponse
     */
    private fun mapToResponse(deviceToken: DeviceToken): DeviceTokenResponse {
        return DeviceTokenResponse(
            id = deviceToken.id!!,
            token = deviceToken.token,
            deviceType = deviceToken.deviceType,
            isActive = deviceToken.isActive,
            createdAt = deviceToken.createdAt
        )
    }
} 