package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_user_email", columnList = "email", unique = true),
        Index(name = "idx_user_role", columnList = "role"),
        Index(name = "idx_user_enabled", columnList = "enabled")
    ]
)
class User(
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null,
    
    @Column(nullable = false, unique = true)
    var email: String,
    
    @Column(nullable = false)
    var password: String,
    
    @Column(name = "full_name", nullable = false)
    var fullName: String,
    
    @Column(name = "phone_number")
    var phoneNumber: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole,
    
    @Column(nullable = false)
    var enabled: Boolean = true,
    
    @Column(name = "profile_picture_url")
    var profilePictureUrl: String? = null,
    
    @Column(name = "notification_preferences", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    var notificationPreferences: Map<String, Any> = emptyMap(),
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val deviceTokens: MutableList<DeviceToken> = mutableListOf(),
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val notifications: MutableList<Notification> = mutableListOf(),
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) 