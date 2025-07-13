package com.eventticketing.backend.service

import java.util.*

interface SessionService {
    
    /**
     * Tạo session mới cho user
     */
    fun createSession(userId: UUID, token: String, ipAddress: String?, userAgent: String?): String
    
    /**
     * Xóa session
     */
    fun removeSession(sessionId: String): Boolean
    
    /**
     * Xóa tất cả session của user
     */
    fun removeAllSessions(userId: UUID): Int
    
    /**
     * Kiểm tra session có hợp lệ không
     */
    fun isValidSession(sessionId: String): Boolean
    
    /**
     * Lấy thông tin session
     */
    fun getSessionInfo(sessionId: String): SessionInfo?
    
    /**
     * Lấy danh sách session của user
     */
    fun getUserSessions(userId: UUID): List<SessionInfo>
    
    /**
     * Cập nhật thời gian hoạt động của session
     */
    fun updateSessionActivity(sessionId: String)
    
    /**
     * Xóa các session đã hết hạn
     */
    fun cleanupExpiredSessions(): Int
}

data class SessionInfo(
    val sessionId: String,
    val userId: UUID,
    val token: String,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: java.time.LocalDateTime,
    val lastActivity: java.time.LocalDateTime,
    val isActive: Boolean
) 