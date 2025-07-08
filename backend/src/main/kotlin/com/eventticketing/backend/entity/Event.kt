package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "events")
class Event(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    var organizer: User,
    
    @Column(nullable = false)
    var title: String,
    
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    
    @Column(nullable = false)
    var location: String,
    
    @Column(precision = 10, scale = 7)
    var latitude: BigDecimal? = null,
    
    @Column(precision = 10, scale = 7)
    var longitude: BigDecimal? = null,
    
    @Column(name = "start_date", nullable = false)
    var startDate: LocalDateTime,
    
    @Column(name = "end_date", nullable = false)
    var endDate: LocalDateTime,
    
    @Column(name = "image_url")
    var imageUrl: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: EventStatus = EventStatus.DRAFT,
    
    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ticketTypes: MutableSet<TicketType> = mutableSetOf(),
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class EventStatus {
    DRAFT, PUBLISHED, CANCELLED, COMPLETED
} 