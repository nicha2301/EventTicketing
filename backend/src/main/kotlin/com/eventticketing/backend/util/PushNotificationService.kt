package com.eventticketing.backend.util

import com.eventticketing.backend.config.NotificationConfig
import com.eventticketing.backend.entity.DeviceToken
import com.eventticketing.backend.repository.DeviceTokenRepository
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Aps
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.google.firebase.messaging.WebpushConfig
import com.google.firebase.messaging.WebpushNotification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.function.Function

@Service
class PushNotificationService(
    private val notificationConfig: NotificationConfig,
    private val deviceTokenRepository: DeviceTokenRepository
) {
    private val logger = LoggerFactory.getLogger(PushNotificationService::class.java)
    
    /**
     * Gửi thông báo đẩy cho người dùng thông qua token thiết bị cụ thể
     */
    fun sendNotificationToToken(token: String, title: String, body: String, data: Map<String, String> = emptyMap()) {
        if (!notificationConfig.push.enabled) {
            logger.info("Push notifications are disabled. Would send notification to token: $token - $title - $body")
            return
        }
        
        try {
            val message = Message.builder()
                .setToken(token)
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build()
                )
                .putAllData(data)
                .setAndroidConfig(createAndroidConfig(title, body))
                .setApnsConfig(createApnsConfig(title))
                .setWebpushConfig(createWebpushConfig(title, body))
                .build()
                
            // Gửi thông báo bất đồng bộ
            val response = FirebaseMessaging.getInstance().sendAsync(message)
            
            // Xử lý kết quả bất đồng bộ
            CompletableFuture.supplyAsync {
                try {
                    return@supplyAsync response.get()
                } catch (e: Exception) {
                    logger.error("Failed to send message to token: $token, error: ${e.message}")
                    throw e
                }
            }.thenAccept { messageId ->
                logger.info("Successfully sent message: $messageId to token: $token")
            }.exceptionally { e ->
                logger.error("Failed to send message to token: $token, error: ${e.message}")
                null
            }
        } catch (e: Exception) {
            logger.error("Error sending push notification to token: $token - ${e.message}", e)
        }
    }
    
    /**
     * Gửi thông báo đẩy cho một người dùng cụ thể
     */
    fun sendNotification(userId: String, title: String, body: String, data: Map<String, String> = emptyMap()) {
        if (!notificationConfig.push.enabled) {
            logger.info("Push notifications are disabled. Would send notification to user $userId: $title - $body")
            return
        }
        
        try {
            val userIdUUID = UUID.fromString(userId)
            val deviceTokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userIdUUID)
            
            if (deviceTokens.isEmpty()) {
                logger.info("No active device tokens found for user: $userId")
                return
            }
            
            logger.info("Sending push notification to ${deviceTokens.size} devices for user: $userId")
            
            // Gửi thông báo đến tất cả thiết bị của người dùng
            for (deviceToken in deviceTokens) {
                sendNotificationToToken(deviceToken.token, title, body, data)
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid UUID format for user: $userId", e)
        } catch (e: Exception) {
            logger.error("Error sending push notification to user: $userId - ${e.message}", e)
        }
    }
    
    /**
     * Gửi thông báo đẩy hàng loạt đến nhiều người dùng
     */
    fun sendBatchNotifications(userIds: List<UUID>, title: String, body: String, data: Map<String, String> = emptyMap()) {
        if (!notificationConfig.push.enabled) {
            logger.info("Push notifications are disabled. Would send notification to ${userIds.size} users")
            return
        }
        
        userIds.forEach { userId ->
            sendNotification(userId.toString(), title, body, data)
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
    
    /**
     * Tạo cấu hình Android cho thông báo
     */
    private fun createAndroidConfig(title: String, body: String): AndroidConfig {
        return AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .setNotification(
                AndroidNotification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .setIcon("ic_notification")
                    .setSound("default")
                    .setColor("#4a6fa5")
                    .build()
            )
            .build()
    }
    
    /**
     * Tạo cấu hình APNS (iOS) cho thông báo
     */
    private fun createApnsConfig(title: String): ApnsConfig {
        return ApnsConfig.builder()
            .setAps(
                Aps.builder()
                    .setAlert(title)
                    .setSound("default")
                    .setBadge(1)
                    .build()
            )
            .build()
    }
    
    /**
     * Tạo cấu hình Webpush cho thông báo
     */
    private fun createWebpushConfig(title: String, body: String): WebpushConfig {
        return WebpushConfig.builder()
            .setNotification(
                WebpushNotification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .setIcon("/images/logo.png")
                    .build()
            )
            .build()
    }
} 