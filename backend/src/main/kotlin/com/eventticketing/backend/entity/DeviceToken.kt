package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "device_tokens")
data class DeviceToken(
    @Id
    @UuidGenerator
    val id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(nullable = false, unique = true)
    val token: String,
    
    @Column(name = "device_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val deviceType: DeviceType,
    
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class DeviceType {
    ANDROID,
    IOS,
    WEB
} 