package com.nicha.eventticketing

import android.app.Application
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.nicha.eventticketing.config.AppConfig
import com.nicha.eventticketing.util.CloudinaryService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltAndroidApp
class EventTicketingApp : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    companion object {
        lateinit var instance: EventTicketingApp
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        instance = this
        
        // Khởi tạo Timber cho logging
        if (AppConfig.FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        
        Timber.d("EventTicketing App khởi động")
        
        // Khởi tạo Firebase
        initializeFirebase()
        
        // Khởi tạo Cloudinary
        initializeCloudinary()
        
        // Bắt các exception không bị xử lý
        setupUncaughtExceptionHandler()
    }
    
    /**
     * Khởi tạo Firebase và FCM
     */
    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            Timber.d("Firebase đã được khởi tạo thành công")
            
            FirebaseMessaging.getInstance().subscribeToTopic(AppConfig.Notification.TOPIC_ALL_USERS)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("Đăng ký nhận thông báo từ topic '${AppConfig.Notification.TOPIC_ALL_USERS}' thành công")
                    } else {
                        Timber.e("Đăng ký nhận thông báo từ topic '${AppConfig.Notification.TOPIC_ALL_USERS}' thất bại: ${task.exception}")
                    }
                }
            
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Timber.d("FCM Token: $token")
                    } else {
                        Timber.e("Không thể lấy FCM token: ${task.exception}")
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi khởi tạo Firebase")
        }
    }
    
    /**
     * Khởi tạo Cloudinary Service
     */
    private fun initializeCloudinary() {
        try {
            CloudinaryService.getInstance().initialize(this)
            Timber.d("Cloudinary đã được khởi tạo thành công")
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi khởi tạo Cloudinary")
        }
    }
    
    private fun setupUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Lỗi không xử lý được trong thread: ${thread.name}")
            
            // Gọi default handler để hiển thị crash dialog
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * Custom Timber tree cho môi trường production
     */
    private inner class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority < Log.INFO) {
                return 
            }
            
            if (priority >= Log.ERROR) {
                logToFile(priority, tag, message, t)
            }
        }
        
        private fun logToFile(priority: Int, tag: String?, message: String, t: Throwable?) {
            try {
                val logDir = File(filesDir, "logs")
                if (!logDir.exists()) {
                    logDir.mkdirs()
                }
                
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val logFile = File(logDir, "app-log-$date.txt")
                
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                val priorityChar = when (priority) {
                    Log.VERBOSE -> 'V'
                    Log.DEBUG -> 'D'
                    Log.INFO -> 'I'
                    Log.WARN -> 'W'
                    Log.ERROR -> 'E'
                    Log.ASSERT -> 'A'
                    else -> '?'
                }
                
                val stackTrace = t?.let { throwable ->
                    throwable.stackTraceToString()
                } ?: ""
                
                val logEntry = "$timestamp $priorityChar/${tag ?: "App"}: $message\n$stackTrace\n\n"
                
                logFile.appendText(logEntry)
            } catch (e: Exception) {
                Log.e("EventTicketingApp", "Không thể ghi log vào file: ${e.message}")
            }
        }
    }
} 