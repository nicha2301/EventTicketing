package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "token_blacklist")
class TokenBlacklist(
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "token", nullable = false, unique = true, length = 1000)
    val token: String,

    @Column(name = "username", nullable = false)
    val username: String,

    @Column(name = "expiry_date", nullable = false)
    val expiryDate: LocalDateTime,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) 