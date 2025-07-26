package com.eventticketing.backend.util

import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.*
import java.util.Base64
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class EmailService(
    private val mailSender: JavaMailSender? = null,
    private val templateEngine: TemplateEngine? = null
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    @Value("\${spring.mail.username:noreply@eventticketing.com}")
    private val fromEmail: String? = null
    
    @Value("\${app.url:http://localhost:8080}")
    private val appUrl: String? = null

    /**
     * Gửi email kích hoạt tài khoản
     */
    fun sendActivationEmail(to: String, name: String, token: String) {
        val subject = "Kích hoạt tài khoản Event Ticketing"
        val activationLink = "$appUrl/api/auth/activate?token=$token"
        
        val variables = mapOf(
            "name" to name,
            "activationLink" to activationLink,
            "expiryHours" to "24"
        )
        
        sendHtmlEmail(to, subject, "activation", variables)
    }

    /**
     * Gửi email đặt lại mật khẩu
     */
    fun sendPasswordResetEmail(to: String, name: String, token: String) {
        val subject = "Đặt lại mật khẩu Event Ticketing"
        val resetLink = "$appUrl/reset-password?token=$token"
        
        val variables = mapOf(
            "name" to name,
            "resetLink" to resetLink,
            "expiryHours" to "1"
        )
        
        sendHtmlEmail(to, subject, "password-reset", variables)
    }

    /**
     * Gửi email xác nhận mua vé
     */
    fun sendTicketConfirmationEmail(to: String, name: String, eventName: String, eventDate: String, 
                                   eventLocation: String, ticketType: String, ticketPrice: String, 
                                   qrCodeData: String, ticketId: String) {
        val subject = "Xác nhận vé sự kiện: $eventName"
        
        val variables = mapOf(
            "name" to name,
            "eventName" to eventName,
            "eventDate" to eventDate,
            "eventLocation" to eventLocation,
            "ticketType" to ticketType,
            "ticketPrice" to ticketPrice,
            "qrCodeData" to qrCodeData,
            "ticketId" to ticketId,
            "viewTicketUrl" to "$appUrl/tickets/$ticketId"
        )
        
        sendHtmlEmail(to, subject, "ticket-confirmation", variables)
    }
    
    /**
     * Kiểm tra tính hợp lệ của base64 string
     */
    private fun isValidBase64(data: String): Boolean {
        return try {
            Base64.getDecoder().decode(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gửi email thông báo sự kiện sắp diễn ra
     */
    fun sendEventReminderEmail(to: String, name: String, eventName: String, eventDate: String, 
                              eventLocation: String, eventId: String, ticketId: String) {
        val subject = "Nhắc nhở: Sự kiện $eventName sắp diễn ra"
        
        val variables = mapOf(
            "name" to name,
            "eventName" to eventName,
            "eventDate" to eventDate,
            "eventLocation" to eventLocation,
            "eventDetailsUrl" to "$appUrl/events/$eventId",
            "ticketUrl" to "$appUrl/tickets/$ticketId"
        )
        
        sendHtmlEmail(to, subject, "event-reminder", variables)
    }

    /**
     * Gửi email thông báo cho ban tổ chức khi có người mua vé
     */
    fun sendOrganizerNotificationEmail(to: String, organizerName: String, eventName: String, 
                                      ticketType: String, buyerName: String, purchaseDate: String, 
                                      eventId: String) {
        val subject = "Thông báo: Vé mới được mua cho sự kiện $eventName"
        
        val variables = mapOf(
            "organizerName" to organizerName,
            "eventName" to eventName,
            "ticketType" to ticketType,
            "buyerName" to buyerName,
            "purchaseDate" to purchaseDate,
            "dashboardUrl" to "$appUrl/organizer/events/$eventId"
        )
        
        sendHtmlEmail(to, subject, "organizer-notification", variables)
    }
    
    /**
     * Gửi email thông báo bình luận mới
     */
    fun sendNewCommentNotificationEmail(to: String, name: String, eventName: String, 
                                       commenterName: String, commentContent: String, eventId: String) {
        val subject = "Thông báo: Bình luận mới cho sự kiện $eventName"
        
        val variables = mapOf(
            "name" to name,
            "eventName" to eventName,
            "commenterName" to commenterName,
            "commentContent" to commentContent,
            "eventUrl" to "$appUrl/events/$eventId"
        )
        
        sendHtmlEmail(to, subject, "comment-notification", variables)
    }
    
    /**
     * Gửi email thông báo đánh giá mới
     */
    fun sendNewRatingNotificationEmail(to: String, name: String, eventName: String, 
                                      raterName: String, rating: Int, review: String?, eventId: String) {
        val subject = "Thông báo: Đánh giá mới cho sự kiện $eventName"
        
        val variables = mapOf(
            "name" to name,
            "eventName" to eventName,
            "raterName" to raterName,
            "rating" to rating.toString(),
            "review" to (review ?: "Không có nhận xét"),
            "eventUrl" to "$appUrl/events/$eventId"
        )
        
        sendHtmlEmail(to, subject, "rating-notification", variables)
    }
    
    /**
     * Gửi email thông báo
     */
    fun sendSystemNotificationEmail(
        to: String, 
        name: String, 
        subject: String, 
        message: String,
        title: String? = null,
        details: String? = null,
        buttonUrl: String? = null,
        buttonText: String? = null,
        additionalInfo: String? = null
    ) {
        val variables = mutableMapOf(
            "name" to name,
            "message" to message,
            "showInfoBox" to (title != null || details != null).toString()
        )
        
        if (title != null) variables["title"] = title
        if (details != null) variables["details"] = details
        if (buttonUrl != null) variables["buttonUrl"] = buttonUrl
        if (buttonText != null) variables["buttonText"] = buttonText
        if (additionalInfo != null) variables["additionalInfo"] = additionalInfo
        
        sendHtmlEmail(to, subject, "system-notification", variables)
    }

    /**
     * Tạo link QR code động thay vì base64
     */
    fun generateQRCodeLink(data: String, width: Int = 200, height: Int = 200): String {
        try {
            if (data.isBlank()) {
                return ""
            }
            
            val encodedData = URLEncoder.encode(data, StandardCharsets.UTF_8.toString())
            return "https://api.qrserver.com/v1/create-qr-code/?size=${width}x${height}&data=${encodedData}"
        } catch (e: Exception) {
            return ""
        }
    }
    
    /**
     * Tạo link QR code từ thông tin vé (tạo link nội bộ)
     */
    fun generateTicketQRLink(ticketId: String, eventId: String, userId: String): String {
        val qrData = "TICKET:${ticketId}:${eventId}:${userId}"
        return generateQRCodeLink(qrData)
    }

    /**
     * Phương thức gửi email HTML với template
     */
    private fun sendHtmlEmail(to: String, subject: String, templateName: String, variables: Map<String, String>) {
        try {
            if (mailSender == null || templateEngine == null) {
                // Log email nếu không có mail sender hoặc template engine (development)
                logger.info("Would send HTML email to: $to")
                logger.info("Subject: $subject")
                logger.info("Template: $templateName")
                logger.info("Variables: $variables")
                return
            }

            val context = Context(Locale("vi"))
            context.setVariables(variables)
            
            val htmlContent = templateEngine.process("email/$templateName", context)
            
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, StandardCharsets.UTF_8.name())
            
            helper.setFrom(fromEmail ?: "noreply@eventticketing.com")
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(htmlContent, true)
            
            // Thêm logo vào email
            try {
                val logoResource = ClassPathResource("static/images/logo.png")
                if (logoResource.exists()) {
                    helper.addInline("logo", logoResource)
                }
            } catch (e: Exception) {
                logger.warn("Could not add logo to email: ${e.message}")
            }
            
            mailSender.send(message)
            logger.info("Email sent successfully to: $to")
        } catch (e: Exception) {
            logger.error("Failed to send email: ${e.message}", e)
            throw e
        }
    }

    /**
     * Phương thức gửi email cơ bản
     */
    private fun sendEmail(to: String, subject: String, content: String) {
        try {
            if (mailSender == null) {
                // Log email nếu không có mail sender (development)
                logger.info("Would send email to: $to")
                logger.info("Subject: $subject")
                logger.info("Content: $content")
                return
            }

            val message = SimpleMailMessage().apply {
                from = fromEmail ?: "noreply@eventticketing.com"
                setTo(to)
                setSubject(subject)
                text = content
            }

            mailSender.send(message)
            logger.info("Email sent successfully to: $to")
        } catch (e: Exception) {
            logger.error("Failed to send email to $to: ${e.message}")
        }
    }
} 