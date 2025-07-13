package com.eventticketing.backend.util

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
object RequestUtils {
    
    /**
     * Lấy IP address từ request
     */
    fun getClientIpAddress(request: HttpServletRequest): String? {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }
        
        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }
        
        val xClusterClientIp = request.getHeader("X-Cluster-Client-IP")
        if (!xClusterClientIp.isNullOrBlank()) {
            return xClusterClientIp
        }
        
        return request.remoteAddr
    }
    
    /**
     * Lấy User Agent từ request
     */
    fun getUserAgent(request: HttpServletRequest): String? {
        return request.getHeader("User-Agent")
    }
    
    /**
     * Kiểm tra xem request có phải từ localhost không
     */
    fun isLocalhost(request: HttpServletRequest): Boolean {
        val ip = getClientIpAddress(request)
        return ip == "127.0.0.1" || ip == "localhost" || ip == "::1"
    }
    
    /**
     * Lấy thông tin chi tiết về request
     */
    fun getRequestInfo(request: HttpServletRequest): Map<String, String> {
        return mapOf(
            "ip" to (getClientIpAddress(request) ?: "unknown"),
            "userAgent" to (getUserAgent(request) ?: "unknown"),
            "method" to request.method,
            "uri" to request.requestURI,
            "referer" to (request.getHeader("Referer") ?: "unknown")
        )
    }
} 