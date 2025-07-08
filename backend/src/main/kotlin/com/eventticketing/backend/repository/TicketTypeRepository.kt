package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.TicketType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TicketTypeRepository : JpaRepository<TicketType, UUID> {
    /**
     * Tìm tất cả loại vé của một sự kiện
     */
    fun findByEventId(eventId: UUID, pageable: Pageable): Page<TicketType>
    
    /**
     * Tìm tất cả loại vé đang hoạt động của một sự kiện
     */
    fun findByEventIdAndIsActiveTrue(eventId: UUID, pageable: Pageable): Page<TicketType>
    
    /**
     * Kiểm tra xem loại vé có thuộc về sự kiện không
     */
    fun existsByIdAndEventId(id: UUID, eventId: UUID): Boolean
    
    /**
     * Đếm số lượng loại vé của một sự kiện
     */
    @Query("SELECT COUNT(tt) FROM TicketType tt WHERE tt.event.id = :eventId")
    fun countByEventId(@Param("eventId") eventId: UUID): Long
    
    /**
     * Tính tổng số vé đã bán của một sự kiện
     */
    @Query("SELECT SUM(tt.quantity - tt.availableQuantity) FROM TicketType tt WHERE tt.event.id = :eventId")
    fun countTotalSoldTicketsByEventId(@Param("eventId") eventId: UUID): Long
    
    /**
     * Tính tổng doanh thu từ các loại vé của một sự kiện
     */
    @Query("SELECT SUM(tt.price * (tt.quantity - tt.availableQuantity)) FROM TicketType tt WHERE tt.event.id = :eventId")
    fun calculateTotalRevenue(@Param("eventId") eventId: UUID): java.math.BigDecimal
    
    /**
     * Tìm các loại vé sắp hết (số lượng còn lại dưới ngưỡng)
     */
    @Query("SELECT tt FROM TicketType tt WHERE tt.availableQuantity <= :threshold AND tt.isActive = true")
    fun findLowStockTicketTypes(@Param("threshold") threshold: Int, pageable: Pageable): Page<TicketType>
} 