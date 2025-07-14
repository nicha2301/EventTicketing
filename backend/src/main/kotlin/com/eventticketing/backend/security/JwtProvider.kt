package com.eventticketing.backend.security

import com.eventticketing.backend.entity.User
import com.eventticketing.backend.util.Constants.JWT
import com.eventticketing.backend.util.Constants.TimeConstants
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey
import java.util.UUID
import java.time.LocalDateTime

@Component
class JwtProvider {

    private val logger = LoggerFactory.getLogger(JwtProvider::class.java)

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.expiration}")
    private var jwtExpiration: Int = 0

    @Value("\${app.jwt.refresh-expiration:604800}")
    private var refreshExpiration: Int = TimeConstants.SECONDS_IN_WEEK

    /**
     * Tạo key từ chuỗi bí mật
     */
    private fun getSigningKey(): SecretKey {
        val keyBytes = jwtSecret.toByteArray(StandardCharsets.UTF_8)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    /**
     * Tạo JWT token từ thông tin xác thực
     */
    fun generateJwtToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal

        // Kiểm tra xem principal có phải là UserDetails không
        if (userPrincipal is UserDetails) {
            return generateToken(userPrincipal.username, null, null, jwtExpiration, JWT.TOKEN_TYPE_ACCESS)
        }
        
        // Trường hợp principal là User entity của chúng ta
        if (userPrincipal is User) {
            return generateToken(userPrincipal.email, userPrincipal.id, userPrincipal.role.name, jwtExpiration, JWT.TOKEN_TYPE_ACCESS)
        }
        
        // Fallback nếu principal không phải loại nào trong số đó
        throw IllegalArgumentException("Loại principal không được hỗ trợ: ${userPrincipal?.javaClass}")
    }

    /**
     * Tạo JWT token từ User
     */
    fun generateJwtToken(user: User): String {
        return generateToken(user.email, user.id, user.role.name, jwtExpiration, JWT.TOKEN_TYPE_ACCESS)
    }

    /**
     * Tạo refresh token từ User
     */
    fun generateRefreshToken(user: User): String {
        return generateToken(user.email, user.id, user.role.name, refreshExpiration, JWT.TOKEN_TYPE_REFRESH)
    }
    
    /**
     * Tạo token với các thông tin chung
     */
    private fun generateToken(subject: String, userId: UUID?, role: String?, expiration: Int, type: String): String {
        val builder = Jwts.builder()
            .subject(subject)
            .issuedAt(Date())
            .expiration(Date(Date().time + expiration * TimeConstants.MILLISECONDS_IN_SECOND))
            .claim("type", type)
        
        if (userId != null) {
            builder.claim("userId", userId)
        }
        
        if (role != null) {
            builder.claim("role", role)
        }
        
        return builder.signWith(getSigningKey()).compact()
    }

    /**
     * Tạo cả access token và refresh token
     */
    fun generateTokenPair(user: User): TokenPair {
        val accessToken = generateJwtToken(user)
        val refreshToken = generateRefreshToken(user)
        return TokenPair(accessToken, refreshToken)
    }

    /**
     * Lấy username từ JWT token
     */
    fun getUsernameFromJwtToken(token: String): String {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
    }

    /**
     * Lấy tất cả claims từ JWT token
     */
    fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .payload
    }

    /**
     * Lấy user ID từ JWT token
     */
    fun getUserIdFromJwtToken(token: String): UUID? {
        val claims = getAllClaimsFromToken(token)
        return claims["userId"] as? UUID
    }

    /**
     * Lấy role từ JWT token
     */
    fun getRoleFromJwtToken(token: String): String? {
        val claims = getAllClaimsFromToken(token)
        return claims["role"] as? String
    }

    /**
     * Lấy token type từ JWT token
     */
    fun getTokenTypeFromJwtToken(token: String): String? {
        val claims = getAllClaimsFromToken(token)
        return claims["type"] as? String
    }

    /**
     * Lấy ngày hết hạn từ JWT token
     */
    fun getExpirationDateFromJwtToken(token: String): LocalDateTime {
        val date = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
            .expiration
        
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
    }

    /**
     * Kiểm tra JWT token có hợp lệ không
     */
    fun validateJwtToken(authToken: String): Boolean {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(authToken)
            return true
        } catch (e: SignatureException) {
            logger.error("Invalid JWT signature: {}", e.message)
        } catch (e: MalformedJwtException) {
            logger.error("Invalid JWT token: {}", e.message)
        } catch (e: ExpiredJwtException) {
            logger.error("JWT token is expired: {}", e.message)
        } catch (e: UnsupportedJwtException) {
            logger.error("JWT token is unsupported: {}", e.message)
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string is empty: {}", e.message)
        }

        return false
    }

    /**
     * Kiểm tra xem token có phải là refresh token không
     */
    fun isRefreshToken(token: String): Boolean {
        return try {
            val tokenType = getTokenTypeFromJwtToken(token)
            tokenType == JWT.TOKEN_TYPE_REFRESH
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Data class để chứa cặp access token và refresh token
 */
data class TokenPair(
    val accessToken: String,
    val refreshToken: String
) 