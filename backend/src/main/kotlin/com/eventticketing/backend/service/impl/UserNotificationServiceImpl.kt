package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.notification.NotificationCountResponse
import com.eventticketing.backend.dto.notification.NotificationDto
import com.eventticketing.backend.dto.notification.NotificationPreferencesRequest
import com.eventticketing.backend.dto.notification.NotificationResponse
import com.eventticketing.backend.entity.Notification
import com.eventticketing.backend.entity.NotificationType
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.repository.NotificationRepository
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.service.UserNotificationService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class UserNotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper
) : UserNotificationService {
    
    private val logger = LoggerFactory.getLogger(UserNotificationServiceImpl::class.java)
    
    @Transactional
    override fun createNotification(notificationDto: NotificationDto): NotificationResponse {
        logger.debug("Creating notification for user: ${notificationDto.userId}")
        
        val user = userRepository.findById(notificationDto.userId!!)
            .orElseThrow { ResourceNotFoundException("User not found with id: ${notificationDto.userId}") }
        
        val notification = Notification(
            user = user,
            title = notificationDto.title,
            content = notificationDto.content,
            notificationType = notificationDto.notificationType,
            referenceId = notificationDto.referenceId,
            referenceType = notificationDto.referenceType,
            isRead = false
        )
        
        val savedNotification = notificationRepository.save(notification)
        logger.debug("Created notification: ${savedNotification.id}")
        
        return mapToResponse(savedNotification)
    }
    
    override fun getUserNotifications(userId: UUID?, pageable: Pageable): Page<NotificationResponse> {
        logger.debug("Getting notifications for user: $userId")

        return notificationRepository.findByUserId(userId!!, pageable)
            .map { mapToResponse(it) }
    }
    
    override fun getUnreadNotifications(userId: UUID?, pageable: Pageable): Page<NotificationResponse> {
        logger.debug("Getting unread notifications for user: $userId")
        
        return notificationRepository.findByUserIdAndIsRead(userId!!, false, pageable)
            .map { mapToResponse(it) }
    }
    
    override fun getNotificationsByType(userId: UUID?, type: NotificationType, pageable: Pageable): Page<NotificationResponse> {
        logger.debug("Getting notifications for user: $userId and type: $type")
        
        return notificationRepository.findByUserIdAndNotificationType(userId!!, type, pageable)
            .map { mapToResponse(it) }
    }
    
    @Transactional
    override fun markAsRead(userId: UUID?, notificationId: UUID): NotificationResponse {
        logger.debug("Marking notification as read: $notificationId for user: $userId")
        
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { ResourceNotFoundException("Notification not found with id: $notificationId") }
        
        // Kiểm tra xem thông báo có thuộc về user không
        if (notification.user.id != userId) {
            logger.warn("Notification $notificationId does not belong to user $userId")
            throw ResourceNotFoundException("Notification not found for user")
        }
        
        // Đánh dấu là đã đọc
        if (!notification.isRead) {
            val updatedNotification = notification.copy(
                isRead = true,
                readAt = LocalDateTime.now()
            )
            
            val saved = notificationRepository.save(updatedNotification)
            logger.debug("Notification marked as read: $notificationId")
            
            return mapToResponse(saved)
        }
        
        return mapToResponse(notification)
    }
    
    @Transactional
    override fun markAllAsRead(userId: UUID?): Int {
        logger.debug("Marking all notifications as read for user: $userId")
        
        val count = notificationRepository.markAllAsRead(userId!!)
        logger.debug("Marked $count notifications as read for user: $userId")
        
        return count
    }
    
    override fun countUnreadNotifications(userId: UUID?): NotificationCountResponse {
        logger.debug("Counting unread notifications for user: $userId")
        
        val count = notificationRepository.countByUserIdAndIsReadFalse(userId!!)
        
        return NotificationCountResponse(count)
    }
    
    @Transactional
    override fun deleteNotification(userId: UUID?, notificationId: UUID): Boolean {
        logger.debug("Deleting notification: $notificationId for user: $userId")
        
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { ResourceNotFoundException("Notification not found with id: $notificationId") }
        
        // Kiểm tra xem thông báo có thuộc về user không
        if (notification.user.id != userId) {
            logger.warn("Notification $notificationId does not belong to user $userId")
            return false
        }
        
        notificationRepository.deleteById(notificationId)
        logger.debug("Notification deleted: $notificationId")
        
        return true
    }
    
    @Transactional
    override fun deleteAllNotifications(userId: UUID?): Int {
        logger.debug("Deleting all notifications for user: $userId")
        
        // Lấy tất cả thông báo của user
        val notifications = notificationRepository.findByUserId(userId!!, Pageable.unpaged()).content
        
        notificationRepository.deleteAll(notifications)
        logger.debug("Deleted ${notifications.size} notifications for user: $userId")
        
        return notifications.size
    }
    
    override fun getNotificationPreferences(userId: UUID?): NotificationPreferencesRequest {
        logger.debug("Getting notification preferences for user: $userId")
        
        val user = userRepository.findById(userId!!)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }
        
        // Nếu không có preferences, trả về mặc định
        if (user.notificationPreferences.isEmpty()) {
            return NotificationPreferencesRequest()
        }
        
        // Convert Map to NotificationPreferencesRequest
        return try {
            val jsonString = objectMapper.writeValueAsString(user.notificationPreferences)
            objectMapper.readValue(jsonString, NotificationPreferencesRequest::class.java)
        } catch (e: Exception) {
            logger.error("Error parsing notification preferences for user: $userId", e)
            NotificationPreferencesRequest()
        }
    }
    
    @Transactional
    override fun updateNotificationPreferences(userId: UUID?, preferences: NotificationPreferencesRequest): NotificationPreferencesRequest {
        logger.debug("Updating notification preferences for user: $userId")
        
        val user = userRepository.findById(userId!!)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }
        
        // Convert preferences to Map<String, Any>
        val preferencesMap: Map<String, Any> = objectMapper.convertValue(preferences, Map::class.java) as Map<String, Any>
        
        // Cập nhật preferences
        user.notificationPreferences = preferencesMap
        userRepository.save(user)
        
        logger.debug("Updated notification preferences for user: $userId")
        
        return preferences
    }
    
    /**
     * Map Notification entity sang NotificationResponse
     */
    private fun mapToResponse(notification: Notification): NotificationResponse {
        return NotificationResponse(
            id = notification.id!!,
            title = notification.title,
            content = notification.content,
            notificationType = notification.notificationType,
            referenceId = notification.referenceId,
            referenceType = notification.referenceType,
            isRead = notification.isRead,
            readAt = notification.readAt,
            createdAt = notification.createdAt
        )
    }
} 