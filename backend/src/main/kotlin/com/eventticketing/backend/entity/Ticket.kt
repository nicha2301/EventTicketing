package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "tickets")
class Ticket(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    var ticketType: TicketType,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,
    
    @Column(name = "purchase_date", nullable = false)
    val purchaseDate: LocalDateTime = LocalDateTime.now(),
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TicketStatus = TicketStatus.PURCHASED,
    
    @Column(name = "qr_code", nullable = false)
    var qrCode: String,
    
    @Column(name = "checked_in", nullable = false)
    var checkedIn: Boolean = false,
    
    @Column(name = "checked_in_at")
    var checkedInAt: LocalDateTime? = null,
    
    @OneToOne(mappedBy = "ticket", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var payment: Payment? = null
)

enum class TicketStatus {
    PURCHASED, CANCELLED, REFUNDED, EXPIRED
} 