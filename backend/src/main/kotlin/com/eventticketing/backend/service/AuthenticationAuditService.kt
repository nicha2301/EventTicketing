package com.eventticketing.backend.service

import java.time.LocalDateTime
import java.util.*

interface AuthenticationAuditService {
    
    /**
     * Ghi log đăng nhập thành công
     */
    fun logSuccessfulLogin(userId: UUID, email: String, ipAddress: String?, userAgent: String?)
    
    /**
     * Ghi log đăng nhập thất bại
     */
    fun logFailedLogin(email: String, ipAddress: String?, userAgent: String?, reason: String)
    
    /**
     * Ghi log đăng xuất
     */
    fun logLogout(userId: UUID, email: String, ipAddress: String?)
    
    /**
     * Ghi log đổi mật khẩu
     */
    fun logPasswordChange(userId: UUID, email: String, ipAddress: String?)
    
    /**
     * Ghi log yêu cầu đặt lại mật khẩu
     */
    fun logPasswordResetRequest(email: String, ipAddress: String?)
    
    /**
     * Ghi log đặt lại mật khẩu thành công
     */
    fun logPasswordResetSuccess(userId: UUID, email: String, ipAddress: String?)
    
    /**
     * Ghi log kích hoạt tài khoản
     */
    fun logAccountActivation(userId: UUID, email: String, ipAddress: String?)
    
    /**
     * Ghi log refresh token
     */
    fun logTokenRefresh(userId: UUID, email: String, ipAddress: String?)
    
    /**
     * Ghi log token bị từ chối
     */
    fun logTokenRejection(token: String, ipAddress: String?, reason: String)
} 