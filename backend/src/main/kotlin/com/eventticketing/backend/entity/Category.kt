package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "categories")
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true, length = 50)
    var name: String,

    @Column(length = 255)
    var description: String? = null,

    @Column(name = "icon_url")
    var iconUrl: String? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val events: MutableList<Event> = mutableListOf()
) 