package com.nicha.eventticketing.config

/**
 * Cấu hình toàn cục cho ứng dụng
 */
object AppConfig {
    // Môi trường hiện tại
    private val ENV = Environment.DEVELOPMENT
    
    /**
     * Các môi trường của ứng dụng
     */
    enum class Environment {
        DEVELOPMENT, // Môi trường phát triển (localhost/emulator)
        STAGING,     // Môi trường kiểm thử
        PRODUCTION   // Môi trường sản phẩm
    }
    
    // API Configuration
    object Api {
        // Cấu hình theo môi trường
        private val API_DOMAIN = when (ENV) {
            // Cấu hình cho máy thật
            Environment.DEVELOPMENT -> "10.211.189.200" // IP thực tế của máy chủ
            // Cấu hình cũ cho Android Emulator
            // Environment.DEVELOPMENT -> "10.0.2.2" 
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
        
        // Tự động tạo URL dựa trên môi trường
        val API_BASE_URL = if (API_PORT.isEmpty()) {
            "$API_PROTOCOL://$API_DOMAIN/"
        } else {
            "$API_PROTOCOL://$API_DOMAIN:$API_PORT/"
        }
        
        // Thời gian timeout cho các request (đơn vị: giây)
        const val CONNECT_TIMEOUT = 15L  // Giảm từ 30 xuống 15
        const val READ_TIMEOUT = 15L     // Giảm từ 30 xuống 15
        const val WRITE_TIMEOUT = 15L    // Giảm từ 30 xuống 15
        
        // Số lần thử lại tối đa khi request thất bại
        const val MAX_RETRIES = 3
    }
    
    // Authentication Configuration
    object Auth {
        const val TOKEN_HEADER = "Authorization"
        const val TOKEN_PREFIX = "Bearer "
        const val UNAUTHORIZED_CODE = 401
        const val FORBIDDEN_CODE = 403
        const val REFRESH_TOKEN_PATH = "api/auth/refresh-token"
        
        // Thời gian hết hạn token (đơn vị: giây)
        const val TOKEN_EXPIRATION = 3600L
        
        // Cập nhật token khi còn lại bao nhiêu phần trăm thời gian sống
        const val TOKEN_REFRESH_THRESHOLD_PERCENT = 20 // 20%
    }
    
    // Database Configuration
    object Database {
        const val DATABASE_NAME = "event_ticketing_db"
        const val DATABASE_VERSION = 1
    }
    
    // Feature Flags
    object FeatureFlags {
        // Các tính năng được bật/tắt tùy theo môi trường
        val ENABLE_BIOMETRIC_AUTH = ENV != Environment.DEVELOPMENT
        val ENABLE_PUSH_NOTIFICATIONS = ENV != Environment.DEVELOPMENT
        val ENABLE_ANALYTICS = ENV != Environment.DEVELOPMENT
        val ENABLE_CRASH_REPORTING = ENV != Environment.DEVELOPMENT
        val ENABLE_DEBUG_LOGGING = ENV == Environment.DEVELOPMENT
        
        // Thêm flag bật/tắt tính năng lưu đăng nhập
        val ENABLE_REMEMBER_ME = true
    }
    
    // Error handling
    object ErrorCodes {
        // HTTP Status Codes
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val INTERNAL_SERVER_ERROR = 500
        
        // Application specific error codes
        const val NETWORK_ERROR = 1000
        const val TIMEOUT_ERROR = 1001
        const val PARSE_ERROR = 1002
        const val UNKNOWN_ERROR = 9999
    }
}
