package com.eventticketing.backend.service

import com.eventticketing.backend.repository.TokenBlacklistRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TokenBlacklistService(
    private val tokenBlacklistRepository: TokenBlacklistRepository
) {
    private val logger = LoggerFactory.getLogger(TokenBlacklistService::class.java)
    
    /**
     * Kiểm tra xem token có trong blacklist không
     */
    fun isTokenBlacklisted(token: String): Boolean {
        try {
            return tokenBlacklistRepository.existsByToken(token)
        } catch (e: Exception) {
            logger.error("Lỗi khi kiểm tra token trong blacklist: ${e.message}")
            // Nếu có lỗi, trả về false để cho phép token (an toàn hơn là chặn token)
            return false
        }
    }
    
    /**
     * Xóa các token đã hết hạn khỏi blacklist mỗi ngày lúc 2 giờ sáng
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    fun cleanupExpiredTokens() {
        try {
            val now = LocalDateTime.now()
            tokenBlacklistRepository.deleteExpiredTokens(now)
            logger.info("Đã xóa các token hết hạn khỏi blacklist")
        } catch (e: Exception) {
            logger.error("Lỗi khi xóa token hết hạn: ${e.message}")
        }
    }
} 