package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.Ticket
import com.eventticketing.backend.entity.TicketStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface TicketRepository : JpaRepository<Ticket, UUID> {

    /**
     * Tìm vé theo mã vé
     */
    fun findByTicketNumber(ticketNumber: String): Optional<Ticket>

    /**
     * Tìm tất cả vé của một người dùng
     */
    fun findByUserId(userId: UUID, pageable: Pageable): Page<Ticket>

    /**
     * Tìm tất cả vé của một sự kiện
     */
    fun findByEventId(eventId: UUID, pageable: Pageable): Page<Ticket>

    /**
     * Tìm tất cả vé của một loại vé
     */
    fun findByTicketTypeId(ticketTypeId: UUID, pageable: Pageable): Page<Ticket>

    /**
     * Tìm tất cả vé theo trạng thái
     */
    fun findByStatus(status: TicketStatus, pageable: Pageable): Page<Ticket>

    /**
     * Tìm tất cả vé của một người dùng theo trạng thái
     */
    fun findByUserIdAndStatus(userId: UUID, status: TicketStatus, pageable: Pageable): Page<Ticket>

    /**
     * Tìm tất cả vé của một sự kiện theo trạng thái
     */
    fun findByEventIdAndStatus(eventId: UUID, status: TicketStatus, pageable: Pageable): Page<Ticket>
    
    /**
     * Tìm tất cả vé của một sự kiện theo trạng thái (không phân trang)
     */
    fun findByEventIdAndStatus(eventId: UUID, status: TicketStatus): List<Ticket>
    
    /**
     * Tìm tất cả vé theo payment ID
     */
    fun findAllByPaymentId(paymentId: UUID): List<Ticket>

    /**
     * Đếm số lượng vé đã bán của một sự kiện
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status IN ('PAID', 'CHECKED_IN')")
    fun countSoldTicketsByEventId(@Param("eventId") eventId: UUID): Long

    /**
     * Đếm số lượng vé đã bán của một loại vé
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId AND t.status IN ('PAID', 'CHECKED_IN')")
    fun countSoldTicketsByTicketTypeId(@Param("ticketTypeId") ticketTypeId: UUID): Long

    /**
     * Tìm tất cả vé đã đặt chỗ quá thời hạn thanh toán
     */
    @Query("SELECT t FROM Ticket t WHERE t.status = 'RESERVED' AND t.createdAt < :expirationTime")
    fun findExpiredReservations(@Param("expirationTime") expirationTime: LocalDateTime): List<Ticket>
    
    // Methods for reporting
    /**
     * Tìm tất cả vé của một sự kiện (không phân trang) cho reporting
     */
    fun findByEventId(eventId: UUID): List<Ticket>
    
    /**
     * Tìm tất cả vé tạo ra trong khoảng thời gian cho reporting
     */
    fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Ticket>
    
    /**
     * Tìm tất cả vé của một sự kiện tạo ra trong khoảng thời gian cho reporting
     */
    fun findByEventIdAndCreatedAtBetween(eventId: UUID, startDate: LocalDateTime, endDate: LocalDateTime): List<Ticket>
    
    /**
     * Đếm số lượng vé đã check-in của một sự kiện
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status = 'CHECKED_IN'")
    fun countCheckedInTicketsByEventId(@Param("eventId") eventId: UUID): Long
    
    /**
     * Đếm số lượng vé theo trạng thái check-in và loại vé
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId AND t.status = :status")
    fun countTicketsByTicketTypeIdAndStatus(
        @Param("ticketTypeId") ticketTypeId: UUID, 
        @Param("status") status: TicketStatus
    ): Long
} 