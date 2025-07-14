package com.eventticketing.backend.util

/**
 * Các hằng số sử dụng trong ứng dụng
 */
object Constants {
    
    /**
     * Các hằng số liên quan đến JWT
     */
    object JWT {
        const val TOKEN_PREFIX = "Bearer "
        const val HEADER_STRING = "Authorization"
        const val TOKEN_TYPE_ACCESS = "access"
        const val TOKEN_TYPE_REFRESH = "refresh"
    }
    
    /**
     * Các hằng số liên quan đến HTTP Headers
     */
    object Headers {
        const val X_FORWARDED_FOR = "X-Forwarded-For"
        const val X_REAL_IP = "X-Real-IP"
        const val X_CLUSTER_CLIENT_IP = "X-Cluster-Client-IP"
        const val USER_AGENT = "User-Agent"
        const val REFERER = "Referer"
        const val X_TOTAL_COUNT = "X-Total-Count"
        const val X_RATE_LIMIT_LIMIT = "X-RateLimit-Limit"
        const val X_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining"
        const val X_RATE_LIMIT_RESET = "X-RateLimit-Reset"
    }
    
    /**
     * Các hằng số liên quan đến API Paths
     */
    object ApiPaths {
        const val AUTH_BASE = "/api/auth"
        const val USERS_BASE = "/api/users"
        const val EVENTS_BASE = "/api/events"
        const val TICKETS_BASE = "/api/tickets"
        const val ADMIN_BASE = "/api/admin"
        const val ORGANIZER_BASE = "/api/organizer"
    }
    
    /**
     * Các hằng số liên quan đến thời gian
     */
    object TimeConstants {
        const val SECONDS_IN_MINUTE = 60
        const val SECONDS_IN_HOUR = 3600
        const val SECONDS_IN_DAY = 86400
        const val SECONDS_IN_WEEK = 604800
        const val MILLISECONDS_IN_SECOND = 1000
    }
} 