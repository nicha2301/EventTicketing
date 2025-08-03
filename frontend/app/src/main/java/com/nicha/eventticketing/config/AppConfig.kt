package com.nicha.eventticketing.config

import android.Manifest

/**
 * Cấu hình toàn cục cho ứng dụng
 */
object AppConfig {
    private val ENV = Environment.DEVELOPMENT
    
    /**
     * Các môi trường của ứng dụng
     */
    enum class Environment {
        DEVELOPMENT, 
        STAGING,    
        PRODUCTION   
    }
    
    // API Configuration
    object Api {
        private val API_DOMAIN = when (ENV) {
            Environment.DEVELOPMENT -> "10.120.235.200"
            Environment.STAGING -> "api-staging.eventticketing.com"
            Environment.PRODUCTION -> "api.eventticketing.com"
        }
        
        private val API_PORT = when (ENV) {
            Environment.DEVELOPMENT -> "8080"
            else -> ""
        }
        
        private val API_PROTOCOL = when (ENV) {
            Environment.DEVELOPMENT -> "http"
            else -> "https"
        }
        
        val API_BASE_URL = if (API_PORT.isEmpty()) {
            "$API_PROTOCOL://$API_DOMAIN/"
        } else {
            "$API_PROTOCOL://$API_DOMAIN:$API_PORT/"
        }
        
        const val CONNECT_TIMEOUT = 15L  
        const val READ_TIMEOUT = 15L     
        const val WRITE_TIMEOUT = 15L   
        
        const val MAX_RETRIES = 3
    }
    
    // Authentication Configuration
    object Auth {
        const val TOKEN_HEADER = "Authorization"
        const val TOKEN_PREFIX = "Bearer "
        const val UNAUTHORIZED_CODE = 401
        const val FORBIDDEN_CODE = 403
        const val REFRESH_TOKEN_PATH = "api/auth/refresh-token"
        
        const val TOKEN_EXPIRATION = 3600L
        
        const val TOKEN_REFRESH_THRESHOLD_PERCENT = 20 
    }
    
    // Database Configuration
    object Database {
        const val DATABASE_NAME = "event_ticketing_db"
        const val DATABASE_VERSION = 1
    }
    
    // Feature Flags
    object FeatureFlags {
        val ENABLE_BIOMETRIC_AUTH = ENV != Environment.DEVELOPMENT
        val ENABLE_PUSH_NOTIFICATIONS = ENV != Environment.DEVELOPMENT
        val ENABLE_ANALYTICS = ENV != Environment.DEVELOPMENT
        val ENABLE_CRASH_REPORTING = ENV != Environment.DEVELOPMENT
        val ENABLE_DEBUG_LOGGING = ENV == Environment.DEVELOPMENT
        
        val ENABLE_REMEMBER_ME = true
    }
    
    // Error handling
    object ErrorCodes {
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val INTERNAL_SERVER_ERROR = 500
        
        const val NETWORK_ERROR = 1000
        const val TIMEOUT_ERROR = 1001
        const val PARSE_ERROR = 1002
        const val UNKNOWN_ERROR = 9999
    }
    
    // Push notification configuration
    object Notification {
        // Notification channel IDs
        const val CHANNEL_ID_EVENTS = "event_ticketing_events"
        const val CHANNEL_NAME_EVENTS = "Sự kiện"
        const val CHANNEL_DESCRIPTION_EVENTS = "Thông báo về sự kiện và vé"
        
        const val CHANNEL_ID_COMMENTS = "event_ticketing_comments"
        const val CHANNEL_NAME_COMMENTS = "Bình luận"
        const val CHANNEL_DESCRIPTION_COMMENTS = "Thông báo về bình luận và đánh giá"
        
        const val CHANNEL_ID_SYSTEM = "event_ticketing_system"
        const val CHANNEL_NAME_SYSTEM = "Hệ thống"
        const val CHANNEL_DESCRIPTION_SYSTEM = "Thông báo hệ thống"
        
        // Deep linking actions
        const val ACTION_OPEN_EVENT = "com.nicha.eventticketing.OPEN_EVENT"
        const val ACTION_OPEN_TICKET = "com.nicha.eventticketing.OPEN_TICKET"
        const val ACTION_OPEN_COMMENT = "com.nicha.eventticketing.OPEN_COMMENT" 
        const val ACTION_OPEN_NOTIFICATION = "com.nicha.eventticketing.OPEN_NOTIFICATION"
        
        // Notification type constants
        const val TYPE_EVENT_REMINDER = "EVENT_REMINDER"
        const val TYPE_NEW_EVENT = "NEW_EVENT"
        const val TYPE_TICKET_PURCHASED = "TICKET_PURCHASED"
        const val TYPE_NEW_COMMENT = "NEW_COMMENT"
        const val TYPE_NEW_RATING = "NEW_RATING"
        const val TYPE_SYSTEM = "SYSTEM"
        const val TYPE_TEST = "TEST_NOTIFICATION"
        
        // FCM topic constants
        const val TOPIC_ALL_USERS = "all_users"
        const val TOPIC_ORGANIZERS = "organizers"
        const val TOPIC_EVENTS = "events"
    }
    
    // Permission configuration
    object Permission {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        const val STORAGE_READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
        const val MEDIA_IMAGES_PERMISSION = Manifest.permission.READ_MEDIA_IMAGES
        const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
        
        const val REQUEST_CAMERA_PERMISSION = 100
        const val REQUEST_STORAGE_PERMISSION = 101
        const val REQUEST_NOTIFICATION_PERMISSION = 102
    }
}
