package com.eventticketing.backend.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.util.concurrent.RateLimiter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Cấu hình giới hạn tốc độ truy cập API
 */
@Configuration
@ConditionalOnProperty(name = ["app.rate-limit.enabled"], havingValue = "true", matchIfMissing = true)
class RateLimitConfig(
    private val rateLimitProperties: RateLimitProperties,
    private val clientIpResolver: ClientIpResolver,
    private val rateLimiterService: RateLimiterService
) : WebMvcConfigurer {
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(RateLimitInterceptor(clientIpResolver, rateLimiterService))
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/auth/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/actuator/**"
            )
    }
}

/**
 * Properties cho cấu hình rate limit
 */
@Component
@ConditionalOnProperty(name = ["app.rate-limit.enabled"], havingValue = "true", matchIfMissing = true)
class RateLimitProperties(
    @Value("\${app.rate-limit.requests-per-second:10.0}")
    val requestsPerSecond: Double = 10.0,
    
    @Value("\${app.rate-limit.burst:20}")
    val burst: Int = 20,
    
    @Value("\${app.rate-limit.cache-expiry-hours:1}")
    val cacheExpiryHours: Long = 1
)

/**
 * Service quản lý rate limiter cho mỗi client
 */
@Component
@ConditionalOnProperty(name = ["app.rate-limit.enabled"], havingValue = "true", matchIfMissing = true)
class RateLimiterService(private val properties: RateLimitProperties) {
    
    private val rateLimiterCache: LoadingCache<String, RateLimiter> = CacheBuilder.newBuilder()
        .expireAfterAccess(properties.cacheExpiryHours, TimeUnit.HOURS)
        .build(object : CacheLoader<String, RateLimiter>() {
            override fun load(key: String): RateLimiter {
                return RateLimiter.create(properties.requestsPerSecond)
            }
        })
    
    /**
     * Kiểm tra xem client có vượt quá giới hạn request không
     */
    fun allowRequest(clientIp: String): Boolean {
        return rateLimiterCache.get(clientIp).tryAcquire(1, 0, TimeUnit.SECONDS)
    }
}

/**
 * Resolver để lấy IP của client từ request
 */
@Component
@ConditionalOnProperty(name = ["app.rate-limit.enabled"], havingValue = "true", matchIfMissing = true)
class ClientIpResolver {
    
    private val ipHeaders = listOf(
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
    )
    
    /**
     * Lấy IP của client từ request
     */
    fun getClientIp(request: HttpServletRequest): String {
        return ipHeaders
            .asSequence()
            .mapNotNull { header -> request.getHeader(header) }
            .filterNot { ip -> ip.isBlank() || ip.equals("unknown", ignoreCase = true) }
            .firstOrNull()
            ?.let { ip ->
                // Nếu có nhiều IP (qua proxy), lấy IP đầu tiên
                if (ip.contains(",")) ip.split(",")[0].trim() else ip
            } ?: request.remoteAddr ?: "unknown"
    }
}

/**
 * Interceptor để kiểm tra giới hạn request
 */
class RateLimitInterceptor(
    private val clientIpResolver: ClientIpResolver,
    private val rateLimiterService: RateLimiterService
) : HandlerInterceptor {
    
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val clientIp = clientIpResolver.getClientIp(request)
        
        if (!rateLimiterService.allowRequest(clientIp)) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write("""{"status":"error","message":"Quá nhiều yêu cầu, vui lòng thử lại sau","code":429}""")
            return false
        }
        
        return true
    }
} 