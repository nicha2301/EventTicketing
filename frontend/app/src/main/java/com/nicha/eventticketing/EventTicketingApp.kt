package com.nicha.eventticketing

import android.app.Application
import android.util.Log
import com.nicha.eventticketing.config.AppConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@HiltAndroidApp
class EventTicketingApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Khởi tạo Timber cho logging
        if (AppConfig.FeatureFlags.ENABLE_DEBUG_LOGGING) {
            // Sử dụng DebugTree với nhiều thông tin hơn trong quá trình phát triển
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    // Format: (FileName.kt:line)
                    val fileName = element.fileName?.substringBefore(".") ?: "Unknown"
                    return "(${fileName}:${element.lineNumber})"
                }
            })
            Timber.d("Khởi tạo ứng dụng trong chế độ DEBUG")
        } else {
            // Sử dụng ReleaseTree với ghi log lỗi trong môi trường production
            Timber.plant(ReleaseTree())
            Timber.i("Khởi tạo ứng dụng trong chế độ RELEASE")
        }

        // Bắt các exception không bị xử lý
        setupUncaughtExceptionHandler()
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
                return // Bỏ qua các log VERBOSE và DEBUG trong môi trường production
            }
            
            // Ghi log lỗi quan trọng vào file hoặc gửi lên server
            if (priority >= Log.ERROR) {
                // Trong ứng dụng thực tế, có thể gửi lỗi lên Firebase Crashlytics hoặc dịch vụ giám sát khác
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
                // Nếu không thể ghi log vào file, sử dụng Android Log API
                Log.e("EventTicketingApp", "Không thể ghi log vào file: ${e.message}")
            }
        }
    }
} 