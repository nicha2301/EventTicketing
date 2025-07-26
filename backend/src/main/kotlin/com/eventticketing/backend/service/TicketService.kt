package com.eventticketing.backend.service

import com.eventticketing.backend.dto.TicketDto
import com.eventticketing.backend.dto.TicketPurchaseDto
import com.eventticketing.backend.dto.TicketPurchaseResponseDto
import com.eventticketing.backend.dto.TicketCheckInRequestDto
import com.eventticketing.backend.entity.TicketStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface TicketService {

    /**
     * Mua vé cho một sự kiện
     */
    fun purchaseTickets(ticketPurchaseDto: TicketPurchaseDto): TicketPurchaseResponseDto

    /**
     * Lấy danh sách vé chưa thanh toán của người dùng hiện tại
     */
    fun getPendingTickets(): List<TicketPurchaseResponseDto>

    /**
     * Lấy thông tin vé theo ID
     */
    fun getTicketById(ticketId: UUID): TicketDto

    /**
     * Lấy thông tin vé theo mã vé
     */
    fun getTicketByNumber(ticketNumber: String): TicketDto

    /**
     * Lấy danh sách vé của một người dùng
     */
    fun getTicketsByUserId(userId: UUID, pageable: Pageable): Page<TicketDto>

    /**
     * Lấy danh sách vé của một sự kiện
     */
    fun getTicketsByEventId(eventId: UUID, pageable: Pageable): Page<TicketDto>

    /**
     * Lấy danh sách vé của một người dùng theo trạng thái
     */
    fun getTicketsByUserIdAndStatus(userId: UUID, status: TicketStatus, pageable: Pageable): Page<TicketDto>
    
    /**
     * Check-in vé bằng mã vé và id vé
     */
    fun checkInTicket(request: TicketCheckInRequestDto): TicketDto

    /**
     * Hủy vé
     */
    fun cancelTicket(ticketId: UUID): TicketDto

    /**
     * Cập nhật trạng thái thanh toán của vé
     */
    fun updatePaymentStatus(orderId: UUID, paymentId: UUID, status: String): TicketPurchaseResponseDto

    /**
     * Xử lý các vé đặt chỗ đã hết hạn
     */
    fun processExpiredReservations(): Int
} 