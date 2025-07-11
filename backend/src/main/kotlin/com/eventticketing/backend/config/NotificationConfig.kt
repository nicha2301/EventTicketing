package com.eventticketing.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.notification")
class NotificationConfig {
    // Email notification settings
    var email = EmailNotificationConfig()
    
    // Push notification settings
    var push = PushNotificationConfig()
    
    class EmailNotificationConfig {
        var enabled: Boolean = true
        var fromEmail: String = "noreply@eventticketing.com"
        var fromName: String = "Event Ticketing"
        var templates = mutableMapOf<String, String>()
    }
    
    class PushNotificationConfig {
        var enabled: Boolean = false
        var firebaseConfigPath: String = "firebase-service-account.json"
    }
} 