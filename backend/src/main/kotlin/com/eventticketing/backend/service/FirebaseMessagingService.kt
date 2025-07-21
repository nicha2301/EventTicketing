package com.eventticketing.backend.service

import com.eventticketing.backend.config.NotificationConfig
import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

@Service
class FirebaseMessagingService(
    private val notificationConfig: NotificationConfig
) {
    private val logger = LoggerFactory.getLogger(FirebaseMessagingService::class.java)
    private val firebaseMessaging by lazy { FirebaseMessaging.getInstance() }

    fun sendMessageToTopic(topic: String, title: String, body: String, data: Map<String, String> = emptyMap()): String? {
        if (!notificationConfig.push.enabled) {
            logger.info("Push notifications are disabled. Would send message to topic: $topic")
            return null
        }

        val message = Message.builder()
            .setTopic(topic)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .putAllData(data)
            .build()

        try {
            val response = firebaseMessaging.send(message)
            logger.info("Successfully sent message to topic $topic: $response")
            return response
        } catch (e: FirebaseMessagingException) {
            logger.error("Failed to send message to topic $topic: ${e.message}", e)
            return null
        }
    }

    /**
     * Gửi thông báo đến một danh sách các token thiết bị
     */
    fun sendMulticastMessage(tokens: List<String>, title: String, body: String, data: Map<String, String> = emptyMap()): BatchResponse? {
        if (!notificationConfig.push.enabled) {
            logger.info("Push notifications are disabled. Would send message to ${tokens.size} tokens")
            return null
        }

        if (tokens.isEmpty()) {
            logger.warn("No tokens provided for multicast message")
            return null
        }

        val message = MulticastMessage.builder()
            .addAllTokens(tokens)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .putAllData(data)
            .build()

        try {
            val response = firebaseMessaging.sendMulticast(message)
            logger.info("Sent multicast message: ${response.successCount} successful, ${response.failureCount} failed")
            return response
        } catch (e: Exception) {
            logger.error("Failed to send multicast message: ${e.message}", e)
            return null
        }
    }

    /**
     * Đăng ký một token với một topic
     */
    fun subscribeToTopic(token: String, topic: String): Boolean {
        if (!notificationConfig.push.enabled) {
            logger.info("Push notifications are disabled. Would subscribe token to topic: $topic")
            return false
        }

        try {
            firebaseMessaging.subscribeToTopic(listOf(token), topic)
            logger.info("Successfully subscribed token to topic: $topic")
            return true
        } catch (e: FirebaseMessagingException) {
            logger.error("Failed to subscribe token to topic $topic: ${e.message}", e)
            return false
        }
    }

    /**
     * Hủy đăng ký một token khỏi một topic
     */
    fun unsubscribeFromTopic(token: String, topic: String): Boolean {
        if (!notificationConfig.push.enabled) {
            logger.info("Push notifications are disabled. Would unsubscribe token from topic: $topic")
            return false
        }

        try {
            firebaseMessaging.unsubscribeFromTopic(listOf(token), topic)
            logger.info("Successfully unsubscribed token from topic: $topic")
            return true
        } catch (e: FirebaseMessagingException) {
            logger.error("Failed to unsubscribe token from topic $topic: ${e.message}", e)
            return false
        }
    }

    /**
     * Gửi thông báo không đồng bộ đến một token
     */
    fun sendAsyncMessage(token: String, title: String, body: String, data: Map<String, String> = emptyMap()): CompletableFuture<String> {
        if (!notificationConfig.push.enabled) {
            logger.info("Push notifications are disabled. Would send async message to token: $token")
            return CompletableFuture.completedFuture("Push notifications disabled")
        }

        val message = Message.builder()
            .setToken(token)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .putAllData(data)
            .build()

        // Chuyển đổi ApiFuture thành CompletableFuture
        val future = CompletableFuture<String>()
        
        try {
            val apiFuture = firebaseMessaging.sendAsync(message)
            apiFuture.addListener({
                try {
                    future.complete(apiFuture.get())
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                }
            }, Runnable::run)
        } catch (e: Exception) {
            future.completeExceptionally(e)
        }
        
        return future
    }
} 