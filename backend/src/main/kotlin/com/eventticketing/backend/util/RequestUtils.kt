package com.eventticketing.backend.util

import com.eventticketing.backend.util.Constants.Headers
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
object RequestUtils {
    
    /**
     * Lấy IP address từ request
     */
    fun getClientIpAddress(request: HttpServletRequest): String? {
        return request.getHeader(Headers.X_FORWARDED_FOR)?.split(",")?.firstOrNull()?.trim()
            ?: request.getHeader(Headers.X_REAL_IP)
            ?: request.getHeader(Headers.X_CLUSTER_CLIENT_IP)
            ?: request.remoteAddr
    }
    
    /**
     * Lấy User Agent từ request
     */
    fun getUserAgent(request: HttpServletRequest): String? {
        return request.getHeader(Headers.USER_AGENT)
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
            "referer" to (request.getHeader(Headers.REFERER) ?: "unknown")
        )
    }
}

/**
 * Extension functions cho HttpServletRequest
 */
fun HttpServletRequest.getClientIp(): String? = RequestUtils.getClientIpAddress(this)
fun HttpServletRequest.getUserAgent(): String? = RequestUtils.getUserAgent(this)
fun HttpServletRequest.isLocalhost(): Boolean = RequestUtils.isLocalhost(this)
fun HttpServletRequest.getRequestInfo(): Map<String, String> = RequestUtils.getRequestInfo(this) 