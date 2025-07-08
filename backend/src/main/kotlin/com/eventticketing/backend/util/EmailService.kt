package com.eventticketing.backend.util

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender? = null
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
        val content = """
            Xin chào $name,
            
            Cảm ơn bạn đã đăng ký tài khoản tại Event Ticketing.
            Vui lòng nhấn vào liên kết bên dưới để kích hoạt tài khoản của bạn:
            
            $activationLink
            
            Liên kết sẽ hết hạn sau 24 giờ.
            
            Trân trọng,
            Event Ticketing Team
        """.trimIndent()

        sendEmail(to, subject, content)
    }

    /**
     * Gửi email đặt lại mật khẩu
     */
    fun sendPasswordResetEmail(to: String, name: String, token: String) {
        val subject = "Đặt lại mật khẩu Event Ticketing"
        val resetLink = "$appUrl/reset-password?token=$token"
        val content = """
            Xin chào $name,
            
            Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu của bạn.
            Vui lòng nhấn vào liên kết bên dưới để đặt lại mật khẩu:
            
            $resetLink
            
            Liên kết sẽ hết hạn sau 1 giờ.
            Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
            
            Trân trọng,
            Event Ticketing Team
        """.trimIndent()

        sendEmail(to, subject, content)
    }

    /**
     * Gửi email xác nhận mua vé
     */
    fun sendTicketConfirmationEmail(to: String, name: String, eventName: String, ticketType: String, qrCode: String) {
        val subject = "Xác nhận vé sự kiện: $eventName"
        val content = """
            Xin chào $name,
            
            Cảm ơn bạn đã mua vé tham gia sự kiện: $eventName
            
            Chi tiết vé:
            - Loại vé: $ticketType
            - Mã QR: $qrCode
            
            Vui lòng xuất trình mã QR khi tham gia sự kiện.
            Bạn cũng có thể xem vé đã mua trong ứng dụng Event Ticketing.
            
            Trân trọng,
            Event Ticketing Team
        """.trimIndent()

        sendEmail(to, subject, content)
    }

    /**
     * Gửi email thông báo sự kiện sắp diễn ra
     */
    fun sendEventReminderEmail(to: String, name: String, eventName: String, eventDate: String, eventLocation: String) {
        val subject = "Nhắc nhở: Sự kiện $eventName sắp diễn ra"
        val content = """
            Xin chào $name,
            
            Chúng tôi xin nhắc nhở bạn rằng sự kiện $eventName mà bạn đã đăng ký sẽ diễn ra vào:
            
            Thời gian: $eventDate
            Địa điểm: $eventLocation
            
            Hãy nhớ mang theo vé (mã QR) khi tham gia sự kiện.
            
            Trân trọng,
            Event Ticketing Team
        """.trimIndent()

        sendEmail(to, subject, content)
    }

    /**
     * Gửi email thông báo cho ban tổ chức khi có người mua vé
     */
    fun sendOrganizerNotificationEmail(to: String, organizerName: String, eventName: String, ticketType: String) {
        val subject = "Thông báo: Vé mới được mua cho sự kiện $eventName"
        val content = """
            Xin chào $organizerName,
            
            Có người vừa mua vé tham gia sự kiện $eventName do bạn tổ chức.
            
            Chi tiết:
            - Loại vé: $ticketType
            
            Vui lòng đăng nhập vào hệ thống để xem thông tin chi tiết.
            
            Trân trọng,
            Event Ticketing Team
        """.trimIndent()

        sendEmail(to, subject, content)
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