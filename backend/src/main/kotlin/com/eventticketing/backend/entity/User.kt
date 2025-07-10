package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "users")
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
    
    @Column(name = "notification_preferences", columnDefinition = "jsonb")
    var notificationPreferences: String = "{}",
    
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