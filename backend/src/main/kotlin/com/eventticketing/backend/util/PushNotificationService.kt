package com.eventticketing.backend.util

import com.eventticketing.backend.config.NotificationConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PushNotificationService(
    private val notificationConfig: NotificationConfig
) {
    private val logger = LoggerFactory.getLogger(PushNotificationService::class.java)
    
    /**
     * Gửi thông báo đẩy cho người dùng
     */
    fun sendNotification(userId: String, title: String, body: String, data: Map<String, String> = emptyMap()) {
        if (!notificationConfig.push.enabled) {
            logger.info("Push notifications are disabled. Would send notification to user $userId: $title - $body")
            return
        }
        
        try {
            // TODO: Implement Firebase Cloud Messaging integration
            // Sử dụng Firebase Admin SDK để gửi thông báo
            // Cần thêm dependency: implementation("com.google.firebase:firebase-admin:9.2.0")
            
            logger.info("Sent push notification to user $userId: $title - $body")
        } catch (e: Exception) {
            logger.error("Failed to send push notification to user $userId: ${e.message}")
        }
    }
    
    /**
     * Gửi thông báo đẩy về sự kiện mới
     */
    fun sendNewEventNotification(userId: String, eventName: String, eventId: String) {
        val title = "Sự kiện mới"
        val body = "Sự kiện mới '$eventName' vừa được tạo. Khám phá ngay!"
        val data = mapOf(
            "eventId" to eventId,
            "type" to "NEW_EVENT"
        )
        
        sendNotification(userId, title, body, data)
    }
    
    /**
     * Gửi thông báo đẩy về sự kiện sắp diễn ra
     */
    fun sendEventReminderNotification(userId: String, eventName: String, eventId: String, hoursLeft: Int) {
        val title = "Nhắc nhở sự kiện"
        val body = "Sự kiện '$eventName' sẽ diễn ra trong $hoursLeft giờ nữa. Đừng quên tham gia!"
        val data = mapOf(
            "eventId" to eventId,
            "type" to "EVENT_REMINDER",
            "hoursLeft" to hoursLeft.toString()
        )
        
        sendNotification(userId, title, body, data)
    }
    
    /**
     * Gửi thông báo đẩy về bình luận mới
     */
    fun sendNewCommentNotification(userId: String, eventName: String, commenterName: String, eventId: String) {
        val title = "Bình luận mới"
        val body = "$commenterName vừa bình luận về sự kiện '$eventName'"
        val data = mapOf(
            "eventId" to eventId,
            "type" to "NEW_COMMENT"
        )
        
        sendNotification(userId, title, body, data)
    }
    
    /**
     * Gửi thông báo đẩy về đánh giá mới
     */
    fun sendNewRatingNotification(userId: String, eventName: String, raterName: String, rating: Int, eventId: String) {
        val title = "Đánh giá mới"
        val body = "$raterName vừa đánh giá $rating sao cho sự kiện '$eventName'"
        val data = mapOf(
            "eventId" to eventId,
            "type" to "NEW_RATING",
            "rating" to rating.toString()
        )
        
        sendNotification(userId, title, body, data)
    }
    
    /**
     * Gửi thông báo đẩy về vé mới được mua
     */
    fun sendNewTicketPurchaseNotification(organizerId: String, eventName: String, buyerName: String, eventId: String) {
        val title = "Vé mới được mua"
        val body = "$buyerName vừa mua vé cho sự kiện '$eventName'"
        val data = mapOf(
            "eventId" to eventId,
            "type" to "NEW_TICKET_PURCHASE"
        )
        
        sendNotification(organizerId, title, body, data)
    }
} 