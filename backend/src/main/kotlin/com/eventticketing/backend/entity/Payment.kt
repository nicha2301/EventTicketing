package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    var ticket: Ticket,
    
    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal,
    
    @Column(name = "payment_method", nullable = false)
    var paymentMethod: String,
    
    @Column(name = "transaction_id", nullable = false)
    var transactionId: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,
    
    @Column(name = "payment_date", nullable = false)
    val paymentDate: LocalDateTime = LocalDateTime.now()
)

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED
} 