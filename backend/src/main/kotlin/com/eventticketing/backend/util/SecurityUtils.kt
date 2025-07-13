package com.eventticketing.backend.util

import com.eventticketing.backend.entity.User
import com.eventticketing.backend.entity.UserRole
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.security.JwtProvider
import com.eventticketing.backend.security.UserPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.util.*

@Component
class SecurityUtils(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider
) {

    /**
     * Lấy username (email) của người dùng hiện tại đang đăng nhập
     */
    fun getCurrentUsername(): String? {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }

        return when (val principal = authentication.principal) {
            is UserDetails -> principal.username
            is String -> principal
            else -> null
        }
    }

    /**
     * Lấy ID của người dùng hiện tại
     */
    fun getCurrentUserId(): UUID? {
        // Lấy token từ SecurityContextHolder
        val authentication = SecurityContextHolder.getContext().authentication
        
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }

        // Nếu principal là UserPrincipal, lấy id từ đó
        val principal = authentication.principal
        if (principal is UserPrincipal) {
            return principal.id
        }
        
        // Nếu không, lấy từ username và truy vấn database
        val username = getCurrentUsername() ?: return null
        val user = userRepository.findByEmail(username).orElse(null) ?: return null
        return user.id
    }

    /**
     * Lấy thông tin người dùng hiện tại đang đăng nhập
     */
    fun getCurrentUser(): User? {
        val username = getCurrentUsername() ?: return null
        return userRepository.findByEmail(username).orElse(null)
    }

    /**
     * Kiểm tra xem người dùng hiện tại có phải admin không
     */
    fun isAdmin(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        
        if (authentication == null || !authentication.isAuthenticated) {
            return false
        }
        
        return authentication.authorities.any { 
            it.authority == "ROLE_${UserRole.ADMIN}" 
        }
    }

    /**
     * Kiểm tra xem người dùng hiện tại có phải organizer không
     */
    fun isOrganizer(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        
        if (authentication == null || !authentication.isAuthenticated) {
            return false
        }
        
        return authentication.authorities.any { 
            it.authority == "ROLE_${UserRole.ORGANIZER}" 
        }
    }

    /**
     * Kiểm tra xem người dùng đang đăng nhập có phải là người dùng với id cụ thể không
     */
    fun isCurrentUser(userId: UUID): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        return currentUserId == userId
    }

    /**
     * Kiểm tra xem người dùng đang đăng nhập có phải là admin hoặc chính người dùng đó không
     */
    fun isCurrentUserOrAdmin(userId: UUID): Boolean {
        return isAdmin() || isCurrentUser(userId)
    }
} 