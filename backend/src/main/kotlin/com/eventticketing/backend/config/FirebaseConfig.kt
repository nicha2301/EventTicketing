package com.eventticketing.backend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import java.io.IOException
import java.io.InputStream

@Configuration
class FirebaseConfig {

    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @Value("\${app.notification.push.enabled:false}")
    private val pushEnabled: Boolean = false

    @Value("\${app.notification.push.firebase-config-path:firebase-service-account.json}")
    private val firebaseConfigPath: String = ""

    @Value("\${app.notification.push.use-classpath-resource:true}")
    private val useClasspathResource: Boolean = true

    @PostConstruct
    fun initialize() {
        if (!pushEnabled) {
            logger.info("Push notifications are disabled. Skipping Firebase initialization.")
            return
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                val serviceAccount: InputStream = getServiceAccountResource().inputStream
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()

                FirebaseApp.initializeApp(options)
                logger.info("Firebase has been initialized successfully.")
            } else {
                logger.info("Firebase already initialized.")
            }
        } catch (e: IOException) {
            logger.error("Failed to initialize Firebase: ${e.message}", e)
        } catch (e: Exception) {
            logger.error("Unexpected error during Firebase initialization: ${e.message}", e)
        }
    }

    private fun getServiceAccountResource(): Resource {
        return if (useClasspathResource) {
            try {
                ClassPathResource(firebaseConfigPath)
            } catch (e: Exception) {
                logger.warn("Could not load Firebase config from classpath, trying file system")
                FileSystemResource(firebaseConfigPath)
            }
        } else {
            FileSystemResource(firebaseConfigPath)
        }
    }
} 