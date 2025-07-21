package com.eventticketing.backend.service.impl

import com.eventticketing.backend.service.NotificationService
import com.eventticketing.backend.util.EmailService
import com.eventticketing.backend.util.PushNotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class NotificationServiceImpl(
    private val emailService: EmailService,
    private val pushNotificationService: PushNotificationService
) : NotificationService {
    
    private val logger = LoggerFactory.getLogger(NotificationServiceImpl::class.java)
    
    override fun sendAccountActivation(userId: UUID, email: String, name: String, token: String) {
        try {
            // Gửi email kích hoạt
            emailService.sendActivationEmail(email, name, token)
            
            logger.info("Account activation notification sent to user: $userId")
        } catch (e: Exception) {
            logger.error("Failed to send account activation notification to user $userId: ${e.message}")
        }
    }
    
    override fun sendPasswordReset(userId: UUID, email: String, name: String, token: String) {
        try {
            // Gửi email đặt lại mật khẩu
            emailService.sendPasswordResetEmail(email, name, token)
            
            logger.info("Password reset notification sent to user: $userId")
        } catch (e: Exception) {
            logger.error("Failed to send password reset notification to user $userId: ${e.message}")
        }
    }
    
    override fun sendTicketConfirmation(
        userId: UUID, email: String, name: String, eventId: UUID, eventName: String,
        eventDate: String, eventLocation: String, ticketId: UUID, ticketType: String,
        ticketPrice: String, qrCodeData: String
    ) {
        try {
            // Gửi email xác nhận vé
            emailService.sendTicketConfirmationEmail(
                email, name, eventName, eventDate, eventLocation, 
                ticketType, ticketPrice, qrCodeData, ticketId.toString()
            )
            
            // Gửi thông báo đẩy
            pushNotificationService.sendNotification(
                userId.toString(),
                "Xác nhận vé",
                "Vé của bạn cho sự kiện $eventName đã được xác nhận.",
                mapOf(
                    "eventId" to eventId.toString(),
                    "ticketId" to ticketId.toString(),
                    "type" to "TICKET_CONFIRMATION"
                )
            )
            
            logger.info("Ticket confirmation notification sent to user: $userId for ticket: $ticketId")
        } catch (e: Exception) {
            logger.error("Failed to send ticket confirmation notification to user $userId: ${e.message}")
        }
    }
    
    override fun sendEventReminder(
        userId: UUID, email: String, name: String, eventId: UUID, eventName: String,
        eventDate: String, eventLocation: String, ticketId: UUID, hoursLeft: Int
    ) {
        try {
            // Gửi email nhắc nhở sự kiện
            emailService.sendEventReminderEmail(
                email, name, eventName, eventDate, eventLocation, 
                eventId.toString(), ticketId.toString()
            )
            
            // Gửi thông báo đẩy
            pushNotificationService.sendEventReminderNotification(
                userId.toString(), eventName, eventId.toString(), hoursLeft
            )
            
            logger.info("Event reminder notification sent to user: $userId for event: $eventId")
        } catch (e: Exception) {
            logger.error("Failed to send event reminder notification to user $userId: ${e.message}")
        }
    }
    
    override fun sendOrganizerTicketPurchaseNotification(
        organizerId: UUID, email: String, organizerName: String, eventId: UUID, 
        eventName: String, ticketType: String, buyerName: String, purchaseDate: String
    ) {
        try {
            // Gửi email thông báo cho ban tổ chức
            emailService.sendOrganizerNotificationEmail(
                email, organizerName, eventName, ticketType, 
                buyerName, purchaseDate, eventId.toString()
            )
            
            // Gửi thông báo đẩy
            pushNotificationService.sendNewTicketPurchaseNotification(
                organizerId.toString(), eventName, buyerName, eventId.toString()
            )
            
            logger.info("Organizer ticket purchase notification sent to organizer: $organizerId for event: $eventId")
        } catch (e: Exception) {
            logger.error("Failed to send organizer notification to organizer $organizerId: ${e.message}")
        }
    }
    
    override fun sendNewCommentNotification(
        userId: UUID, email: String, name: String, eventId: UUID,
        eventName: String, commenterName: String, commentContent: String
    ) {
        try {
            // Gửi email thông báo bình luận mới
            emailService.sendNewCommentNotificationEmail(
                email, name, eventName, commenterName, commentContent, eventId.toString()
            )
            
            // Gửi thông báo đẩy
            pushNotificationService.sendNewCommentNotification(
                userId.toString(), eventName, commenterName, eventId.toString()
            )
            
            logger.info("New comment notification sent to user: $userId for event: $eventId")
        } catch (e: Exception) {
            logger.error("Failed to send comment notification to user $userId: ${e.message}")
        }
    }
    
    override fun sendNewRatingNotification(
        userId: UUID, email: String, name: String, eventId: UUID,
        eventName: String, raterName: String, rating: Int, review: String?
    ) {
        try {
            // Gửi email thông báo đánh giá mới
            emailService.sendNewRatingNotificationEmail(
                email, name, eventName, raterName, rating, review, eventId.toString()
            )
            
            // Gửi thông báo đẩy
            pushNotificationService.sendNewRatingNotification(
                userId.toString(), eventName, raterName, rating, eventId.toString()
            )
            
            logger.info("New rating notification sent to user: $userId for event: $eventId")
        } catch (e: Exception) {
            logger.error("Failed to send rating notification to user $userId: ${e.message}")
        }
    }
    
    override fun sendSystemNotification(
        userId: UUID, email: String, name: String, 
        subject: String, message: String, 
        referenceId: UUID?, referenceType: String?,
        buttonUrl: String?, buttonText: String?
    ) {
        try {
            // Gửi email thông báo hệ thống
            emailService.sendSystemNotificationEmail(
                to = email,
                name = name,
                subject = subject,
                message = message,
                buttonUrl = buttonUrl,
                buttonText = buttonText
            )
            
            // Gửi thông báo đẩy
            pushNotificationService.sendNotification(
                userId.toString(),
                subject,
                message,
                mapOf(
                    "type" to "SYSTEM_NOTIFICATION",
                    "referenceId" to (referenceId?.toString() ?: ""),
                    "referenceType" to (referenceType ?: "")
                )
            )
            
            logger.info("System notification sent to user: $userId")
        } catch (e: Exception) {
            logger.error("Failed to send system notification to user $userId: ${e.message}")
        }
    }
} 