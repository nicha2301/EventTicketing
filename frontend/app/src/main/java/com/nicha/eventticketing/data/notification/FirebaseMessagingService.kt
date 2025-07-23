package com.nicha.eventticketing.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nicha.eventticketing.MainActivity
import com.nicha.eventticketing.R
import com.nicha.eventticketing.config.AppConfig.Notification
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.leolin.shortcutbadger.ShortcutBadger
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class EventTicketingFirebaseMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var notificationRepository: NotificationRepository
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private var badgeCount = 0

    /**
     * Xử lý khi nhận được thông báo mới
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("Nhận thông báo từ: ${remoteMessage.from}")
        
        Timber.d("RemoteMessage details - messageId: ${remoteMessage.messageId}, " +
                "messageType: ${remoteMessage.messageType}, " +
                "collapseKey: ${remoteMessage.collapseKey}, " +
                "priority: ${remoteMessage.priority}, " +
                "ttl: ${remoteMessage.ttl}")
        
        incrementBadgeCount()
        
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Data payload: ${remoteMessage.data}")
            
            handleDataPayload(remoteMessage.data)
        }
        
        remoteMessage.notification?.let {
            Timber.d("Notification payload: ${it.title} - ${it.body}")
            
            showNotification(it.title, it.body, remoteMessage.data)
        }
    }
    
    /**
     * Xử lý khi token FCM được cập nhật
     */
    override fun onNewToken(token: String) {
        Timber.d("Nhận token FCM mới: $token")
        
        // Đăng ký token mới với server
        registerTokenWithServer(token)
    }
    
    /**
     * Xử lý data payload của thông báo
     */
    private fun handleDataPayload(data: Map<String, String>) {
        val title = data["title"]
        val body = data["body"]
        val type = data["type"]
        val referenceId = data["referenceId"]
        
        if (title != null && body != null) {
            showNotification(title, body, data)
        }
        
        // Xử lý theo loại thông báo
        when (type) {
            Notification.TYPE_EVENT_REMINDER -> {
                // Xử lý thông báo nhắc nhở sự kiện
            }
            Notification.TYPE_NEW_COMMENT -> {
                // Xử lý thông báo bình luận mới
            }
            Notification.TYPE_TICKET_PURCHASED -> {
                // Xử lý thông báo mua vé
            }
            // Thêm các loại thông báo khác ở đây
        }
    }
    
    /**
     * Hiển thị thông báo với thông tin từ data payload
     */
    private fun showNotification(title: String?, body: String?, data: Map<String, String> = emptyMap()) {
        if (title == null || body == null) return
        
        val type = data["type"]
        val referenceId = data["referenceId"]
        
        val pendingIntent = createPendingIntent(type, referenceId)
        
        val channelId = getNotificationChannelId(type)
        
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setNumber(badgeCount)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels(notificationManager)
        }
        
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
    
    /**
     * Tạo các kênh thông báo cho Android O+
     */
    private fun createNotificationChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val eventChannel = NotificationChannel(
                Notification.CHANNEL_ID_EVENTS,
                Notification.CHANNEL_NAME_EVENTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Notification.CHANNEL_DESCRIPTION_EVENTS
                enableLights(true)
                enableVibration(true)
            }
            
            val commentChannel = NotificationChannel(
                Notification.CHANNEL_ID_COMMENTS,
                Notification.CHANNEL_NAME_COMMENTS,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = Notification.CHANNEL_DESCRIPTION_COMMENTS
                enableLights(true)
                enableVibration(true)
            }
            
            val systemChannel = NotificationChannel(
                Notification.CHANNEL_ID_SYSTEM,
                Notification.CHANNEL_NAME_SYSTEM,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = Notification.CHANNEL_DESCRIPTION_SYSTEM
                enableLights(true)
                enableVibration(false)
            }
            
            notificationManager.createNotificationChannels(listOf(eventChannel, commentChannel, systemChannel))
        }
    }
    
    /**
     * Tạo PendingIntent dựa trên loại thông báo
     */
    private fun createPendingIntent(type: String?, referenceId: String?): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            
            when (type) {
                Notification.TYPE_EVENT_REMINDER, Notification.TYPE_NEW_EVENT -> {
                    action = Notification.ACTION_OPEN_EVENT
                    putExtra("eventId", referenceId)
                }
                Notification.TYPE_TICKET_PURCHASED -> {
                    action = Notification.ACTION_OPEN_TICKET
                    putExtra("ticketId", referenceId)
                }
                Notification.TYPE_NEW_COMMENT, Notification.TYPE_NEW_RATING -> {
                    action = Notification.ACTION_OPEN_COMMENT
                    putExtra("eventId", referenceId)
                }
                else -> {
                    action = Notification.ACTION_OPEN_NOTIFICATION
                }
            }
        }
        
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Lấy ID kênh thông báo dựa trên loại thông báo
     */
    private fun getNotificationChannelId(type: String?): String {
        return when (type) {
            Notification.TYPE_EVENT_REMINDER, Notification.TYPE_NEW_EVENT, Notification.TYPE_TICKET_PURCHASED -> Notification.CHANNEL_ID_EVENTS
            Notification.TYPE_NEW_COMMENT, Notification.TYPE_NEW_RATING -> Notification.CHANNEL_ID_COMMENTS
            else -> Notification.CHANNEL_ID_SYSTEM
        }
    }
    
    /**
     * Tăng số lượng badge
     */
    private fun incrementBadgeCount() {
        badgeCount++
        try {
            ShortcutBadger.applyCount(applicationContext, badgeCount)
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật badge count")
        }
        
        scope.launch {
            try {
                notificationRepository.getUnreadNotificationCount().collect { result ->
                    if (result is Resource.Success) {
                        val unreadCount = result.data?.unreadCount ?: 0
                        badgeCount = unreadCount
                        ShortcutBadger.applyCount(applicationContext, badgeCount)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi lấy số lượng thông báo chưa đọc")
            }
        }
    }
    
    /**
     * Đăng ký token với server
     */
    private fun registerTokenWithServer(token: String) {
        scope.launch {
            try {
                notificationRepository.registerDeviceToken(token, "ANDROID").collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Timber.d("Đăng ký token thành công: ${result.data?.id}")
                        }
                        is Resource.Error -> {
                            Timber.e("Đăng ký token thất bại: ${result.message}")
                        }
                        is Resource.Loading -> {
                            // Do nothing
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi đăng ký token với server")
            }
        }
    }
} 