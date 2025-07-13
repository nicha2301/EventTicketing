package com.eventticketing.backend.interceptor

import com.eventticketing.backend.annotation.RateLimited
import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.service.RateLimitService
import com.eventticketing.backend.util.RequestUtils
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RateLimitInterceptor(
    private val rateLimitService: RateLimitService,
    private val objectMapper: ObjectMapper
) : HandlerInterceptor {
    
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) {
            return true
        }
        
        val rateLimited = handler.getMethodAnnotation(RateLimited::class.java)
        if (rateLimited == null) {
            return true
        }
        
        val ipAddress = RequestUtils.getClientIpAddress(request) ?: "unknown"
        val endpoint = request.requestURI
        
        // Kiểm tra rate limit
        if (rateLimitService.isRateLimited(ipAddress, endpoint)) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            
            val errorResponse = ApiResponse<String>(
                success = false,
                message = "Rate limit exceeded. Please try again later.",
                data = null
            )
            
            objectMapper.writeValue(response.outputStream, errorResponse)
            return false
        }
        
        // Ghi nhận request
        rateLimitService.recordRequest(ipAddress, endpoint)
        
        // Thêm header với thông tin rate limit
        val remaining = rateLimitService.getRemainingRequests(ipAddress, endpoint)
        response.setHeader("X-RateLimit-Remaining", remaining.toString())
        response.setHeader("X-RateLimit-Reset", (System.currentTimeMillis() / 1000 + rateLimited.windowSeconds).toString())
        
        return true
    }
} 