package com.nicha.eventticketing.domain.service

import com.google.firebase.messaging.FirebaseMessaging
import com.nicha.eventticketing.config.AppConfig
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service để xử lý việc đăng ký FCM token với server
 */
@Singleton
class FcmTokenService @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    
    /**
     * Đăng ký token FCM hiện tại với server
     */
    fun registerFcmToken() {
        val scope = CoroutineScope(Dispatchers.IO)
        
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Timber.d("Đăng ký FCM token sau đăng nhập: $token")
                    
                    subscribeToTopics()
                    
                    scope.launch {
                        notificationRepository.registerDeviceToken(token, "ANDROID").collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    Timber.d("Đăng ký token sau đăng nhập thành công: ${result.data?.id}")
                                }
                                is Resource.Error -> {
                                    Timber.e("Đăng ký token sau đăng nhập thất bại: ${result.message}")
                                }
                                is Resource.Loading -> {
                                    // Đang xử lý
                                }
                            }
                        }
                    }
                } else {
                    Timber.e(task.exception, "Không thể lấy FCM token")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đăng ký FCM token")
        }
    }
    
    /**
     * Đăng ký nhận thông báo từ các topic
     */
    private fun subscribeToTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic(AppConfig.Notification.TOPIC_ALL_USERS)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Đăng ký nhận thông báo từ topic '${AppConfig.Notification.TOPIC_ALL_USERS}' thành công")
                } else {
                    Timber.e("Đăng ký nhận thông báo từ topic '${AppConfig.Notification.TOPIC_ALL_USERS}' thất bại: ${task.exception}")
                }
            }
            
    }
} 