package com.eventticketing.backend.security

import com.eventticketing.backend.service.AuthenticationAuditService
import com.eventticketing.backend.service.TokenBlacklistService
import com.eventticketing.backend.util.RequestUtils
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val customUserDetailsService: CustomUserDetailsService,
    private val tokenBlacklistService: TokenBlacklistService,
    private val authenticationAuditService: AuthenticationAuditService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)
            
            if (!jwt.isNullOrBlank()) {
                if (!jwtProvider.validateJwtToken(jwt)) {
                    // Token không hợp lệ
                    val ipAddress = RequestUtils.getClientIpAddress(request)
                    authenticationAuditService.logTokenRejection(jwt, ipAddress, "Invalid JWT token")
                    filterChain.doFilter(request, response)
                    return
                }
                
                // Kiểm tra token có trong blacklist không
                if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                    // Token đã bị blacklist
                    val ipAddress = RequestUtils.getClientIpAddress(request)
                    authenticationAuditService.logTokenRejection(jwt, ipAddress, "Token is blacklisted")
                    logger.debug("Token đã bị blacklist, bỏ qua xác thực")
                    filterChain.doFilter(request, response)
                    return
                }
                
                val username = jwtProvider.getUsernameFromJwtToken(jwt)
                val userDetails = customUserDetailsService.loadUserByUsername(username)
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            val jwt = getJwtFromRequest(request)
            val ipAddress = RequestUtils.getClientIpAddress(request)
            if (!jwt.isNullOrBlank()) {
                authenticationAuditService.logTokenRejection(jwt, ipAddress, "Exception during authentication: ${e.message}")
            }
            logger.error("Không thể thiết lập xác thực người dùng: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")

        return if (!bearerToken.isNullOrBlank() && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
} 