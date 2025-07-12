package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "notifications")
data class Notification(
    @Id
    @UuidGenerator
    val id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(nullable = false)
    val title: String,
    
    @Column(nullable = false)
    val content: String,
    
    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val notificationType: NotificationType,
    
    @Column(name = "reference_id")
    val referenceId: UUID? = null,
    
    @Column(name = "reference_type")
    val referenceType: String? = null,
    
    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,
    
    @Column(name = "read_at")
    var readAt: LocalDateTime? = null,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class NotificationType {
    ACCOUNT_ACTIVATION,
    PASSWORD_RESET,
    TICKET_CONFIRMATION,
    EVENT_REMINDER,
    NEW_COMMENT,
    NEW_RATING,
    TICKET_PURCHASE,
    SYSTEM
} 