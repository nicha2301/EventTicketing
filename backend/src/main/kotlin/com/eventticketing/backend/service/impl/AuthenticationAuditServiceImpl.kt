package com.eventticketing.backend.service.impl

import com.eventticketing.backend.service.AuthenticationAuditService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class AuthenticationAuditServiceImpl : AuthenticationAuditService {
    
    private val logger = LoggerFactory.getLogger(AuthenticationAuditServiceImpl::class.java)
    
    override fun logSuccessfulLogin(userId: UUID, email: String, ipAddress: String?, userAgent: String?) {
        logger.info("SUCCESSFUL_LOGIN - User: $email (ID: $userId) - IP: $ipAddress - UserAgent: $userAgent")
    }
    
    override fun logFailedLogin(email: String, ipAddress: String?, userAgent: String?, reason: String) {
        logger.warn("FAILED_LOGIN - Email: $email - IP: $ipAddress - UserAgent: $userAgent - Reason: $reason")
    }
    
    override fun logLogout(userId: UUID, email: String, ipAddress: String?) {
        logger.info("LOGOUT - User: $email (ID: $userId) - IP: $ipAddress")
    }
    
    override fun logPasswordChange(userId: UUID, email: String, ipAddress: String?) {
        logger.info("PASSWORD_CHANGE - User: $email (ID: $userId) - IP: $ipAddress")
    }
    
    override fun logPasswordResetRequest(email: String, ipAddress: String?) {
        logger.info("PASSWORD_RESET_REQUEST - Email: $email - IP: $ipAddress")
    }
    
    override fun logPasswordResetSuccess(userId: UUID, email: String, ipAddress: String?) {
        logger.info("PASSWORD_RESET_SUCCESS - User: $email (ID: $userId) - IP: $ipAddress")
    }
    
    override fun logAccountActivation(userId: UUID, email: String, ipAddress: String?) {
        logger.info("ACCOUNT_ACTIVATION - User: $email (ID: $userId) - IP: $ipAddress")
    }
    
    override fun logTokenRefresh(userId: UUID, email: String, ipAddress: String?) {
        logger.info("TOKEN_REFRESH - User: $email (ID: $userId) - IP: $ipAddress")
    }
    
    override fun logTokenRejection(token: String, ipAddress: String?, reason: String) {
        logger.warn("TOKEN_REJECTION - IP: $ipAddress - Reason: $reason - Token: ${token.take(20)}...")
    }
} 