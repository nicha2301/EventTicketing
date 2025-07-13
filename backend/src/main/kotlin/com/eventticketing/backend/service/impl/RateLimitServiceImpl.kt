package com.eventticketing.backend.service.impl

import com.eventticketing.backend.service.RateLimitService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class RateLimitServiceImpl : RateLimitService {
    
    private val logger = LoggerFactory.getLogger(RateLimitServiceImpl::class.java)
    
    // Lưu trữ thông tin rate limit cho từng IP và endpoint
    private val rateLimitMap = ConcurrentHashMap<String, MutableList<LocalDateTime>>()
    
    // Cấu hình rate limit cho từng endpoint
    private val rateLimitConfig = mapOf(
        "/api/auth/login" to RateLimitConfig(5, 60), // 5 requests per minute
        "/api/auth/register" to RateLimitConfig(3, 60), // 3 requests per minute
        "/api/auth/password/forgot" to RateLimitConfig(3, 60), // 3 requests per minute
        "/api/auth/refresh-token" to RateLimitConfig(10, 60), // 10 requests per minute
        "default" to RateLimitConfig(100, 60) // 100 requests per minute
    )
    
    override fun isRateLimited(ipAddress: String, endpoint: String): Boolean {
        val config = rateLimitConfig[endpoint] ?: rateLimitConfig["default"]!!
        val key = "$ipAddress:$endpoint"
        val requests = rateLimitMap[key] ?: mutableListOf()
        
        // Xóa các request cũ hơn window time
        val now = LocalDateTime.now()
        requests.removeAll { it.isBefore(now.minusSeconds(config.windowSeconds.toLong())) }
        
        return requests.size >= config.maxRequests
    }
    
    override fun recordRequest(ipAddress: String, endpoint: String) {
        val key = "$ipAddress:$endpoint"
        val requests = rateLimitMap.getOrPut(key) { mutableListOf() }
        requests.add(LocalDateTime.now())
        
        // Giới hạn kích thước list để tránh memory leak
        if (requests.size > 100) {
            requests.removeAt(0)
        }
    }
    
    override fun getRemainingRequests(ipAddress: String, endpoint: String): Int {
        val config = rateLimitConfig[endpoint] ?: rateLimitConfig["default"]!!
        val key = "$ipAddress:$endpoint"
        val requests = rateLimitMap[key] ?: mutableListOf()
        
        // Xóa các request cũ hơn window time
        val now = LocalDateTime.now()
        requests.removeAll { it.isBefore(now.minusSeconds(config.windowSeconds.toLong())) }
        
        return maxOf(0, config.maxRequests - requests.size)
    }
    
    override fun resetRateLimit(ipAddress: String, endpoint: String) {
        val key = "$ipAddress:$endpoint"
        rateLimitMap.remove(key)
        logger.info("Reset rate limit for $ipAddress:$endpoint")
    }
    
    private data class RateLimitConfig(
        val maxRequests: Int,
        val windowSeconds: Int
    )
} 