package com.eventticketing.backend.service

import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

interface RateLimitService {
    
    /**
     * Kiểm tra xem IP có bị rate limit không
     */
    fun isRateLimited(ipAddress: String, endpoint: String): Boolean
    
    /**
     * Ghi nhận một request từ IP
     */
    fun recordRequest(ipAddress: String, endpoint: String)
    
    /**
     * Lấy số lần request còn lại cho IP
     */
    fun getRemainingRequests(ipAddress: String, endpoint: String): Int
    
    /**
     * Reset rate limit cho IP
     */
    fun resetRateLimit(ipAddress: String, endpoint: String)
} 