package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "ticket_types",
    indexes = [
        Index(name = "idx_ticket_type_event", columnList = "event_id"),
        Index(name = "idx_ticket_type_name", columnList = "name"),
        Index(name = "idx_ticket_type_sale_dates", columnList = "sale_start_date, sale_end_date")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class TicketType(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    var name: String,

    @Column
    var description: String? = null,

    @Column(nullable = false)
    var price: BigDecimal,

    @Column(nullable = false)
    var quantity: Int,

    @Column(name = "quantity_sold", nullable = false)
    var quantitySold: Int = 0,

    @Column(name = "sale_start_date", nullable = false)
    var saleStartDate: LocalDateTime,

    @Column(name = "sale_end_date", nullable = false)
    var saleEndDate: LocalDateTime,

    @Column(name = "max_per_order")
    var maxPerOrder: Int? = null,

    @Column(name = "min_per_order")
    var minPerOrder: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Kiểm tra xem loại vé có còn chỗ không
     */
    fun hasAvailableTickets(): Boolean {
        return quantitySold < quantity
    }

    /**
     * Kiểm tra xem loại vé có đang trong thời gian bán không
     */
    fun isOnSale(): Boolean {
        val now = LocalDateTime.now()
        return now.isAfter(saleStartDate) && now.isBefore(saleEndDate)
    }

    /**
     * Kiểm tra xem có thể đặt số lượng vé này không
     */
    fun canReserve(requestedQuantity: Int): Boolean {
        if (requestedQuantity <= 0) {
            return false
        }

        // Sử dụng let để tránh smart cast
        maxPerOrder?.let { max ->
            if (requestedQuantity > max) {
                return false
            }
        }

        // Sử dụng let để tránh smart cast
        minPerOrder?.let { min ->
            if (requestedQuantity < min) {
                return false
            }
        }

        val remainingTickets = quantity - quantitySold
        return requestedQuantity <= remainingTickets
    }

    /**
     * Đặt vé
     */
    fun reserveTickets(requestedQuantity: Int): Boolean {
        if (!canReserve(requestedQuantity)) {
            return false
        }

        quantitySold += requestedQuantity
        return true
    }

    /**
     * Hoàn trả vé (khi hủy đặt chỗ)
     */
    fun releaseTickets(releasedQuantity: Int): Boolean {
        if (releasedQuantity <= 0 || releasedQuantity > quantitySold) {
            return false
        }

        quantitySold -= releasedQuantity
        return true
    }
} 