package com.eventticketing.backend.scheduler

import com.eventticketing.backend.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class NotificationCleanupScheduler(
    private val notificationRepository: NotificationRepository
) {
    private val logger = LoggerFactory.getLogger(NotificationCleanupScheduler::class.java)
    
    @Value("\${app.notification.retention-days:90}")
    private val retentionDays: Int = 90
    
    // Chạy vào lúc 3:00 AM vào ngày đầu tiên mỗi tháng
    @Scheduled(cron = "0 0 3 1 * *")
    @Transactional
    fun cleanupOldNotifications() {
        logger.info("Starting notification cleanup scheduler")
        
        try {
            val cutoffDate = LocalDateTime.now().minusDays(retentionDays.toLong())
            
            val deletedCount = notificationRepository.deleteOlderThan(cutoffDate)
            
            logger.info("Deleted $deletedCount notifications older than $cutoffDate")
        } catch (e: Exception) {
            logger.error("Error in notification cleanup scheduler: ${e.message}", e)
        }
    }
} 