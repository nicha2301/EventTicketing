package com.eventticketing.backend.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.Properties

@Configuration
class MailConfig {
    private val logger = LoggerFactory.getLogger(MailConfig::class.java)
    
    @Value("\${spring.mail.host:#{null}}")
    private val host: String? = null
    
    @Value("\${spring.mail.port:0}")
    private val port: Int = 0
    
    @Value("\${spring.mail.username:#{null}}")
    private val username: String? = null
    
    @Value("\${spring.mail.password:#{null}}")
    private val password: String? = null
    
    @Value("\${spring.mail.properties.mail.smtp.auth:false}")
    private val auth: Boolean = false
    
    @Value("\${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private val starttlsEnable: Boolean = false
    
    @Value("\${app.notification.email.enabled:true}")
    private val emailEnabled: Boolean = true
    
    @Bean
    fun javaMailSender(): JavaMailSender? {
        if (!emailEnabled) {
            return null
        }
        
        if (host.isNullOrBlank() || port == 0 || username.isNullOrBlank() || password.isNullOrBlank()) {
            logger.warn("Email configuration is incomplete")
            return null
        }
        
        try {
            val mailSender = JavaMailSenderImpl()
            mailSender.host = host!!
            mailSender.port = port
            mailSender.username = username!!
            mailSender.password = password!!
            
            val props: Properties = mailSender.javaMailProperties
            props.put("mail.transport.protocol", "smtp")
            props.put("mail.smtp.auth", auth.toString())
            props.put("mail.smtp.starttls.enable", starttlsEnable.toString())
            props.put("mail.debug", "false")
            
            return mailSender
        } catch (e: Exception) {
            logger.error("Error configuring JavaMailSender: ${e.message}", e)
            return null
        }
    }
} 