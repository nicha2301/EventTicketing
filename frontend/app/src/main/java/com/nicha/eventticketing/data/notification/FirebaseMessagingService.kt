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
import com.nicha.eventticketing.config.AppConfig
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
    
    companion object {
        private val CHANNEL_ID_EVENTS = AppConfig.Notification.CHANNEL_ID_EVENTS
        private val CHANNEL_NAME_EVENTS = AppConfig.Notification.CHANNEL_NAME_EVENTS
        private val CHANNEL_DESCRIPTION_EVENTS = AppConfig.Notification.CHANNEL_DESCRIPTION_EVENTS
        
        private val CHANNEL_ID_COMMENTS = AppConfig.Notification.CHANNEL_ID_COMMENTS
        private val CHANNEL_NAME_COMMENTS = AppConfig.Notification.CHANNEL_NAME_COMMENTS
        private val CHANNEL_DESCRIPTION_COMMENTS = AppConfig.Notification.CHANNEL_DESCRIPTION_COMMENTS
        
        private val CHANNEL_ID_SYSTEM = AppConfig.Notification.CHANNEL_ID_SYSTEM
        private val CHANNEL_NAME_SYSTEM = AppConfig.Notification.CHANNEL_NAME_SYSTEM
        private val CHANNEL_DESCRIPTION_SYSTEM = AppConfig.Notification.CHANNEL_DESCRIPTION_SYSTEM
        
        val ACTION_OPEN_EVENT = AppConfig.Notification.ACTION_OPEN_EVENT
        val ACTION_OPEN_TICKET = AppConfig.Notification.ACTION_OPEN_TICKET
        val ACTION_OPEN_COMMENT = AppConfig.Notification.ACTION_OPEN_COMMENT
        val ACTION_OPEN_NOTIFICATION = AppConfig.Notification.ACTION_OPEN_NOTIFICATION
        
        private val TYPE_EVENT_REMINDER = AppConfig.Notification.TYPE_EVENT_REMINDER
        private val TYPE_NEW_EVENT = AppConfig.Notification.TYPE_NEW_EVENT
        private val TYPE_TICKET_PURCHASED = AppConfig.Notification.TYPE_TICKET_PURCHASED
        private val TYPE_NEW_COMMENT = AppConfig.Notification.TYPE_NEW_COMMENT
        private val TYPE_NEW_RATING = AppConfig.Notification.TYPE_NEW_RATING
        private val TYPE_SYSTEM = AppConfig.Notification.TYPE_SYSTEM
        private val TYPE_TEST = AppConfig.Notification.TYPE_TEST
        
        private var badgeCount = 0
    }
    
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
            TYPE_EVENT_REMINDER -> {
                // Xử lý thông báo nhắc nhở sự kiện
            }
            TYPE_NEW_COMMENT -> {
                // Xử lý thông báo bình luận mới
            }
            TYPE_TICKET_PURCHASED -> {
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
     * Tạo các kênh thông báo cho Android
     */
    private fun createNotificationChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val eventChannel = NotificationChannel(
                CHANNEL_ID_EVENTS,
                CHANNEL_NAME_EVENTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION_EVENTS
                enableLights(true)
                enableVibration(true)
            }
            
            val commentChannel = NotificationChannel(
                CHANNEL_ID_COMMENTS,
                CHANNEL_NAME_COMMENTS,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION_COMMENTS
                enableLights(true)
                enableVibration(true)
            }
            
            val systemChannel = NotificationChannel(
                CHANNEL_ID_SYSTEM,
                CHANNEL_NAME_SYSTEM,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION_SYSTEM
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
                TYPE_EVENT_REMINDER, TYPE_NEW_EVENT -> {
                    action = ACTION_OPEN_EVENT
                    putExtra("eventId", referenceId)
                }
                TYPE_TICKET_PURCHASED -> {
                    action = ACTION_OPEN_TICKET
                    putExtra("ticketId", referenceId)
                }
                TYPE_NEW_COMMENT, TYPE_NEW_RATING -> {
                    action = ACTION_OPEN_COMMENT
                    putExtra("eventId", referenceId)
                }
                else -> {
                    action = ACTION_OPEN_NOTIFICATION
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
            TYPE_EVENT_REMINDER, TYPE_NEW_EVENT, TYPE_TICKET_PURCHASED -> CHANNEL_ID_EVENTS
            TYPE_NEW_COMMENT, TYPE_NEW_RATING -> CHANNEL_ID_COMMENTS
            else -> CHANNEL_ID_SYSTEM
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