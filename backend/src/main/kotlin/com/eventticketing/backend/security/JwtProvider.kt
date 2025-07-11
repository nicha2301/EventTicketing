package com.eventticketing.backend.security

import com.eventticketing.backend.entity.User
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

@Component
class JwtProvider {

    private val logger = LoggerFactory.getLogger(JwtProvider::class.java)

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.expiration}")
    private var jwtExpiration: Int = 0

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
            return Jwts.builder()
                .subject(userPrincipal.username)
                .issuedAt(Date())
                .expiration(Date(Date().time + jwtExpiration * 1000))
                // Không thêm userId và role claim vì chúng ta không có thông tin này từ UserDetails
                .signWith(getSigningKey())
                .compact()
        }
        
        // Trường hợp principal là User entity của chúng ta (hiếm gặp, nhưng cứ giữ để tương thích ngược)
        if (userPrincipal is User) {
            return Jwts.builder()
                .subject(userPrincipal.email)
                .issuedAt(Date())
                .expiration(Date(Date().time + jwtExpiration * 1000))
                .claim("userId", userPrincipal.id)
                .claim("role", userPrincipal.role.name)
                .signWith(getSigningKey())
                .compact()
        }
        
        // Fallback nếu principal không phải loại nào trong số đó
        throw IllegalArgumentException("Loại principal không được hỗ trợ: ${userPrincipal?.javaClass}")
    }

    /**
     * Tạo JWT token từ User
     */
    fun generateJwtToken(user: User): String {
        return Jwts.builder()
                .subject(user.email)
                .issuedAt(Date())
                .expiration(Date(Date().time + jwtExpiration * 1000))
                .claim("userId", user.id)
                .claim("role", user.role.name)
                .signWith(getSigningKey())
                .compact()
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
} 