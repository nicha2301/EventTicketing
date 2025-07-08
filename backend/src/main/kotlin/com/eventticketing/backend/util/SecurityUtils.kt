package com.eventticketing.backend.util

import com.eventticketing.backend.entity.User
import com.eventticketing.backend.entity.UserRole
import com.eventticketing.backend.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*

@Component
class SecurityUtils(
    private val userRepository: UserRepository
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
     * Lưu ý: Yêu cầu ID người dùng phải được lưu trong authentication
     */
    fun getCurrentUserId(): UUID? {
        val authentication = SecurityContextHolder.getContext().authentication
        
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }

        // Lấy ID từ claims trong token
        // Trong thực tế, bạn cần trích xuất từ JWT token
        val claims = authentication.details as? Map<*, *>
        return claims?.get("user_id") as? UUID
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