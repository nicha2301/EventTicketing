package com.eventticketing.backend.entity

import com.fasterxml.jackson.databind.JsonNode
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @UuidGenerator
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
    
    @Column(name = "transaction_id")
    var transactionId: String? = null,
    
    @Column(name = "payment_url", columnDefinition = "TEXT")
    var paymentUrl: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "refunded_amount", precision = 10, scale = 2)
    var refundedAmount: BigDecimal? = null,
    
    @Column(name = "refunded_at")
    var refundedAt: LocalDateTime? = null,
    
    @Column(name = "refund_reason")
    var refundReason: String? = null,
    
    @Column(name = "description")
    var description: String? = null,
    
    @Column(name = "metadata")
    @JdbcTypeCode(SqlTypes.JSON)
    var metadata: String? = null
)

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED, PROCESSING
} 