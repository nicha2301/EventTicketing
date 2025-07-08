package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "locations")
data class Location(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false)
    var address: String,

    @Column(nullable = false, length = 100)
    var city: String,

    @Column(length = 100)
    var state: String? = null,

    @Column(nullable = false, length = 100)
    var country: String,

    @Column(name = "postal_code", length = 20)
    var postalCode: String? = null,

    @Column(nullable = false)
    var latitude: Double,

    @Column(nullable = false)
    var longitude: Double,

    @Column
    var capacity: Int? = null,

    @Column(length = 1000)
    var description: String? = null,

    @Column(length = 255)
    var website: String? = null,

    @Column(name = "phone_number", length = 20)
    var phoneNumber: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    val events: MutableList<Event> = mutableListOf()
) 