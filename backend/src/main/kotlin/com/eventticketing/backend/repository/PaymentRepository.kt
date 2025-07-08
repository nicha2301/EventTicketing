package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.Payment
import com.eventticketing.backend.entity.PaymentStatus
import com.eventticketing.backend.entity.Ticket
import com.eventticketing.backend.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface PaymentRepository : JpaRepository<Payment, UUID> {
    fun findByUser(user: User, pageable: Pageable): Page<Payment>
    
    fun findByTicket(ticket: Ticket): Optional<Payment>
    
    fun findByStatus(status: PaymentStatus, pageable: Pageable): Page<Payment>
    
    fun findByTransactionId(transactionId: String): Optional<Payment>
    
    @Query("""
        SELECT p FROM Payment p
        JOIN p.ticket t
        JOIN t.ticketType tt
        JOIN tt.event e
        WHERE e.id = :eventId
    """)
    fun findByEventId(@Param("eventId") UUID: UUID, pageable: Pageable): Page<Payment>
    
    @Query("""
        SELECT SUM(p.amount) FROM Payment p
        WHERE p.status = :status
        AND p.paymentDate BETWEEN :startDate AND :endDate
    """)
    fun sumAmountByStatusAndDateRange(
        @Param("status") status: PaymentStatus,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Optional<Double>
} 