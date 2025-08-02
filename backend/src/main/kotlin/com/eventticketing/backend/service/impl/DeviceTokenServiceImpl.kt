package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.notification.DeviceTokenRequest
import com.eventticketing.backend.dto.notification.DeviceTokenResponse
import com.eventticketing.backend.entity.DeviceToken
import com.eventticketing.backend.entity.DeviceType
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.repository.DeviceTokenRepository
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.service.DeviceTokenService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class DeviceTokenServiceImpl(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val userRepository: UserRepository
) : DeviceTokenService {
    
    @Transactional
    override fun registerToken(userId: UUID?, request: DeviceTokenRequest): DeviceTokenResponse {
        val user = userRepository.findById(userId!!)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }
        
        val existingTokens = deviceTokenRepository.findByToken(request.token)
        
        if (existingTokens.isNotEmpty()) {
            if (existingTokens.size > 1) {
                val sortedTokens = existingTokens.sortedByDescending { it.createdAt }
                val keepToken = sortedTokens.first()
                val tokensToDelete = sortedTokens.drop(1)
                
                tokensToDelete.forEach { tokenToDelete ->
                    deviceTokenRepository.deleteById(tokenToDelete.id!!)
                }
                
                val token = keepToken
                if (token.user.id != userId || !token.isActive) {
                    if (token.user.id != userId) {
                        deviceTokenRepository.deleteById(token.id!!)
                    }
                    
                    val newToken = DeviceToken(
                        user = user,
                        token = request.token,
                        deviceType = request.deviceType,
                        isActive = true
                    )
                    
                    val savedToken = deviceTokenRepository.save(newToken)
                    
                    return mapToResponse(savedToken)
                } else {
                    return mapToResponse(token)
                }
            } else {
                val token = existingTokens.first()
                if (token.user.id != userId || !token.isActive) {
                    if (token.user.id != userId) {
                        deviceTokenRepository.deleteById(token.id!!)
                    }
                    
                    val newToken = DeviceToken(
                        user = user,
                        token = request.token,
                        deviceType = request.deviceType,
                        isActive = true
                    )
                    
                    val savedToken = deviceTokenRepository.save(newToken)
                    
                    return mapToResponse(savedToken)
                } else {
                    return mapToResponse(token)
                }
            }
        } else {
            val newToken = DeviceToken(
                user = user,
                token = request.token,
                deviceType = request.deviceType,
                isActive = true
            )
            
            val savedToken = deviceTokenRepository.save(newToken)
            
            return mapToResponse(savedToken)
        }
    }
    
    override fun getTokensByUserId(userId: UUID?): List<DeviceTokenResponse> {
        return deviceTokenRepository.findByUserId(userId!!)
            .map { mapToResponse(it) }
    }
    
    override fun getActiveTokensByUserId(userId: UUID): List<DeviceTokenResponse> {
        return deviceTokenRepository.findByUserIdAndIsActiveTrue(userId)
            .map { mapToResponse(it) }
    }
    
    override fun getTokensByUserIdAndDeviceType(userId: UUID, deviceType: DeviceType): List<DeviceTokenResponse> {
        return deviceTokenRepository.findByUserIdAndDeviceType(userId, deviceType)
            .map { mapToResponse(it) }
    }
    
    @Transactional
    override fun deactivateToken(userId: UUID, tokenId: UUID): Boolean {
        val token = deviceTokenRepository.findById(tokenId)
            .orElseThrow { ResourceNotFoundException("Token not found with id: $tokenId") }
        
        if (token.user.id != userId) {
            return false
        }
        
        val updatedToken = token.copy(isActive = false)
        deviceTokenRepository.save(updatedToken)
        
        return true
    }
    
    @Transactional
    override fun deactivateAllTokens(userId: UUID): Int {
        val tokens = deviceTokenRepository.findByUserId(userId)
        
        tokens.forEach { token ->
            val updatedToken = token.copy(isActive = false)
            deviceTokenRepository.save(updatedToken)
        }
        
        return tokens.size
    }
    
    @Transactional
    override fun deleteToken(userId: UUID?, tokenId: UUID): Boolean {
        val token = deviceTokenRepository.findById(tokenId)
            .orElseThrow { ResourceNotFoundException("Token not found with id: $tokenId") }
        
        if (token.user.id != userId) {
            return false
        }
        
        deviceTokenRepository.deleteById(tokenId)
        
        return true
    }
    
    @Transactional
    override fun deleteAllTokens(userId: UUID): Int {
        val count = deviceTokenRepository.deleteByUserId(userId)
        
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