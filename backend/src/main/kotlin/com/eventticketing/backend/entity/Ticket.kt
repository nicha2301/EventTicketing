package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "tickets")
data class Ticket(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "ticket_number", unique = true)
    var ticketNumber: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    var ticketType: TicketType,

    @Column(nullable = false)
    var price: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TicketStatus = TicketStatus.RESERVED,

    @Column(name = "qr_code")
    var qrCode: String? = null,

    @Column(name = "purchase_date")
    var purchaseDate: LocalDateTime? = null,

    @Column(name = "checked_in_at")
    var checkedInAt: LocalDateTime? = null,

    @Column(name = "cancelled_at")
    var cancelledAt: LocalDateTime? = null,

    @Column(name = "payment_id")
    var paymentId: UUID? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Đánh dấu vé đã check-in
     */
    fun checkIn(): Boolean {
        if (status != TicketStatus.PAID) {
            return false
        }
        status = TicketStatus.CHECKED_IN
        checkedInAt = LocalDateTime.now()
        return true
    }

    /**
     * Đánh dấu vé đã thanh toán
     */
    fun markAsPaid(paymentId: UUID): Boolean {
        if (status != TicketStatus.RESERVED) {
            return false
        }
        status = TicketStatus.PAID
        this.paymentId = paymentId
        purchaseDate = LocalDateTime.now()
        return true
    }

    /**
     * Đánh dấu vé đã hủy
     */
    fun cancel(): Boolean {
        if (status == TicketStatus.CHECKED_IN || status == TicketStatus.CANCELLED) {
            return false
        }
        status = TicketStatus.CANCELLED
        cancelledAt = LocalDateTime.now()
        return true
    }

    /**
     * Đánh dấu vé đã hết hạn
     */
    fun expire(): Boolean {
        if (status != TicketStatus.RESERVED) {
            return false
        }
        status = TicketStatus.EXPIRED
        return true
    }

    /**
     * Tạo mã vé ngẫu nhiên
     */
    fun generateTicketNumber() {
        if (ticketNumber == null) {
            val random = Random()
            val randomNumber = 100000 + random.nextInt(900000) // 6 chữ số
            ticketNumber = "TK${System.currentTimeMillis()}$randomNumber"
        }
    }
}
