package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "ticket_types")
data class TicketType(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(length = 500)
    var description: String? = null,

    @Column(nullable = false)
    var price: BigDecimal,

    @Column(nullable = false)
    var quantity: Int,

    @Column(name = "available_quantity", nullable = false)
    var availableQuantity: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event,

    @Column(name = "sales_start_date")
    var salesStartDate: LocalDateTime? = null,

    @Column(name = "sales_end_date")
    var salesEndDate: LocalDateTime? = null,

    @Column(name = "max_tickets_per_customer")
    var maxTicketsPerCustomer: Int? = null,

    @Column(name = "min_tickets_per_order", nullable = false)
    var minTicketsPerOrder: Int = 1,

    @Column(name = "is_early_bird")
    var isEarlyBird: Boolean = false,

    @Column(name = "is_vip")
    var isVIP: Boolean = false,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "ticketType", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val tickets: MutableList<Ticket> = mutableListOf()
) {
    @get:Transient
    val quantitySold: Int
        get() = quantity - availableQuantity

    // Hàm kiểm tra có vé còn trống không
    fun hasAvailableTickets(requestedQuantity: Int): Boolean {
        return isActive && availableQuantity >= requestedQuantity && 
               (salesStartDate == null || LocalDateTime.now().isAfter(salesStartDate)) &&
               (salesEndDate == null || LocalDateTime.now().isBefore(salesEndDate))
    }

    // Hàm cập nhật số lượng vé còn trống
    fun updateAvailableQuantity(purchasedQuantity: Int): Boolean {
        if (!hasAvailableTickets(purchasedQuantity)) {
            return false
        }
        availableQuantity -= purchasedQuantity
        return true
    }
} 