package com.eventticketing.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * Cấu hình JWT
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Validated
class JwtConfig {
    companion object {
        // Các constant liên quan đến JWT
        const val TOKEN_PREFIX = "Bearer "
        const val HEADER_STRING = "Authorization"
        const val AUTHORITIES_KEY = "authorities"
        const val USER_ID_KEY = "userId"
        const val EMAIL_KEY = "email"
        
        // Thời gian mặc định
        const val DEFAULT_EXPIRATION_SECONDS = 86400L // 24 giờ
        const val DEFAULT_REFRESH_EXPIRATION_SECONDS = 604800L // 7 ngày
    }
    
    @NotBlank(message = "JWT secret key không được để trống")
    lateinit var secret: String
    
    @Positive(message = "JWT expiration phải là số dương")
    var expiration: Long = DEFAULT_EXPIRATION_SECONDS
    
    @Positive(message = "JWT refresh expiration phải là số dương")
    var refreshExpiration: Long = DEFAULT_REFRESH_EXPIRATION_SECONDS
    
    /**
     * Lấy thời gian hết hạn của access token dưới dạng Duration
     */
    fun getAccessTokenDuration(): Duration {
        return Duration.of(expiration, ChronoUnit.SECONDS)
    }
    
    /**
     * Lấy thời gian hết hạn của refresh token dưới dạng Duration
     */
    fun getRefreshTokenDuration(): Duration {
        return Duration.of(refreshExpiration, ChronoUnit.SECONDS)
    }
    
    /**
     * Kiểm tra xem secret key có đủ mạnh không
     */
    fun isSecretKeyStrong(): Boolean {
        return secret.length >= 32
    }
} 