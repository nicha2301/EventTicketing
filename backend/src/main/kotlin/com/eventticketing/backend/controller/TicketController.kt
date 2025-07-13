package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.dto.TicketDto
import com.eventticketing.backend.dto.TicketPurchaseDto
import com.eventticketing.backend.dto.TicketPurchaseResponseDto
import com.eventticketing.backend.entity.TicketStatus
import com.eventticketing.backend.util.SecurityUtils
import com.eventticketing.backend.service.TicketService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/tickets")
class TicketController(
    private val ticketService: TicketService,
    private val securityUtils: SecurityUtils
) {

    /**
     * Mua vé cho sự kiện
     */
    @PostMapping("/purchase")
    fun purchaseTickets(@Valid @RequestBody ticketPurchaseDto: TicketPurchaseDto): ResponseEntity<ApiResponse<TicketPurchaseResponseDto>> {
        val response = ticketService.purchaseTickets(ticketPurchaseDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Đặt vé thành công", response))
    }

    /**
     * Lấy thông tin vé theo ID
     */
    @GetMapping("/{ticketId}")
    fun getTicketById(@PathVariable ticketId: UUID): ResponseEntity<ApiResponse<TicketDto>> {
        val ticket = ticketService.getTicketById(ticketId)
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin vé thành công", ticket))
    }

    /**
     * Lấy thông tin vé theo mã vé
     */
    @GetMapping("/number/{ticketNumber}")
    fun getTicketByNumber(@PathVariable ticketNumber: String): ResponseEntity<ApiResponse<TicketDto>> {
        val ticket = ticketService.getTicketByNumber(ticketNumber)
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin vé thành công", ticket))
    }

    /**
     * Lấy danh sách vé của người dùng hiện tại
     */
    @GetMapping("/my-tickets")
    fun getMyTickets(
        @PageableDefault(size = 10) pageable: Pageable,
        @RequestParam(required = false) status: TicketStatus?
    ): ResponseEntity<ApiResponse<Page<TicketDto>>> {
        val currentUser = securityUtils.getCurrentUser()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Người dùng chưa đăng nhập"))

        val tickets = if (status != null) {
            ticketService.getTicketsByUserIdAndStatus(currentUser.id!!, status, pageable)
        } else {
            ticketService.getTicketsByUserId(currentUser.id!!, pageable)
        }
        
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách vé thành công", tickets))
    }

    /**
     * Lấy danh sách vé của một sự kiện (chỉ admin)
     */
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getTicketsByEventId(
        @PathVariable eventId: UUID,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<TicketDto>>> {
        val tickets = ticketService.getTicketsByEventId(eventId, pageable)
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách vé của sự kiện thành công", tickets))
    }

    /**
     * Check-in vé (chỉ admin)
     */
    @PostMapping("/{ticketId}/check-in")
    @PreAuthorize("hasRole('ADMIN')")
    fun checkInTicket(@PathVariable ticketId: UUID): ResponseEntity<ApiResponse<TicketDto>> {
        val ticket = ticketService.checkInTicket(ticketId)
        return ResponseEntity.ok(ApiResponse.success("Check-in vé thành công", ticket))
    }

    /**
     * Check-in vé bằng mã vé (chỉ admin)
     */
    @PostMapping("/check-in/{ticketNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    fun checkInTicketByNumber(@PathVariable ticketNumber: String): ResponseEntity<ApiResponse<TicketDto>> {
        val ticket = ticketService.checkInTicketByNumber(ticketNumber)
        return ResponseEntity.ok(ApiResponse.success("Check-in vé thành công", ticket))
    }

    /**
     * Hủy vé
     */
    @PostMapping("/{ticketId}/cancel")
    fun cancelTicket(@PathVariable ticketId: UUID): ResponseEntity<ApiResponse<TicketDto>> {
        val ticket = ticketService.cancelTicket(ticketId)
        return ResponseEntity.ok(ApiResponse.success("Hủy vé thành công", ticket))
    }

    /**
     * Cập nhật trạng thái thanh toán của vé (webhook từ cổng thanh toán)
     */
    @PostMapping("/payment-webhook")
    fun updatePaymentStatus(
        @RequestParam orderId: UUID,
        @RequestParam paymentId: UUID,
        @RequestParam status: String
    ): ResponseEntity<ApiResponse<TicketPurchaseResponseDto>> {
        val response = ticketService.updatePaymentStatus(orderId, paymentId, status)
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thanh toán thành công", response))
    }

    /**
     * Xử lý các vé đặt chỗ đã hết hạn (chỉ admin hoặc scheduled task)
     */
    @PostMapping("/process-expired")
    @PreAuthorize("hasRole('ADMIN')")
    fun processExpiredReservations(): ResponseEntity<ApiResponse<Map<String, Int>>> {
        val count = ticketService.processExpiredReservations()
        return ResponseEntity.ok(ApiResponse.success("Đã xử lý vé hết hạn", mapOf("expiredCount" to count)))
    }
} 