package com.eventticketing.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.rate-limit")
class RateLimitProperties {
    val endpoints: MutableMap<String, RateLimitConfig> = mutableMapOf(
        "default" to RateLimitConfig(100, 60),
        "/api/auth/login" to RateLimitConfig(5, 60),
        "/api/auth/register" to RateLimitConfig(3, 60),
        "/api/auth/password/forgot" to RateLimitConfig(3, 60),
        "/api/auth/refresh-token" to RateLimitConfig(10, 60),
        "/api/auth/google" to RateLimitConfig(5, 60)
    )
}

data class RateLimitConfig(
    var maxRequests: Int = 5,
    var windowSeconds: Int = 60
) 