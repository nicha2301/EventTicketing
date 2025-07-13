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
import java.nio.charset.StandardCharsets
import java.util.*

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
     * Gửi email với template tùy chỉnh
     * Được sử dụng bởi EmailConsumer
     */
    fun sendTemplateEmail(to: String, subject: String, template: String, model: Map<String, Any>) {
        try {
            val context = Context(Locale("vi"))
            context.setVariables(model)
            
            // Chuyển đổi model từ Map<String, Any> sang Map<String, String> nếu cần
            val stringModel = model.mapValues { it.value.toString() }
            
            sendHtmlEmail(to, subject, template, stringModel)
        } catch (e: Exception) {
            logger.error("Failed to send template email to $to: ${e.message}")
        }
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
            helper.addInline("logo", ClassPathResource("static/images/logo.png"))
            
            mailSender.send(message)
            logger.info("HTML email sent successfully to: $to")
        } catch (e: Exception) {
            logger.error("Failed to send HTML email to $to: ${e.message}")
            // Fallback to plain text email
            val plainContent = "Vui lòng xem email này ở định dạng HTML. " +
                               "Nếu bạn không thể xem được, vui lòng liên hệ support@eventticketing.com."
            sendEmail(to, subject, plainContent)
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