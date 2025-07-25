package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.dto.TicketDto
import com.eventticketing.backend.dto.TicketPurchaseDto
import com.eventticketing.backend.dto.TicketPurchaseResponseDto
import com.eventticketing.backend.dto.TicketCheckInRequestDto
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
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response))
    }

    /**
     * Lấy thông tin vé theo ID
     */
    @GetMapping("/{ticketId}")
    fun getTicketById(@PathVariable ticketId: UUID): ResponseEntity<ApiResponse<TicketDto>> {
        val ticket = ticketService.getTicketById(ticketId)
        return ResponseEntity.ok(ApiResponse.success(ticket))
    }

    /**
     * Lấy thông tin vé theo mã vé
     */
    @GetMapping("/number/{ticketNumber}")
    fun getTicketByNumber(@PathVariable ticketNumber: String): ResponseEntity<ApiResponse<TicketDto>> {
        val ticket = ticketService.getTicketByNumber(ticketNumber)
        return ResponseEntity.ok(ApiResponse.success(ticket))
    }

    /**
     * Lấy danh sách vé chưa thanh toán của người dùng hiện tại
     */
    @GetMapping("/my-pending-tickets")
    fun getMyPendingTickets(): ResponseEntity<ApiResponse<List<TicketPurchaseResponseDto>>> {
        val currentUser = securityUtils.getCurrentUser()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Người dùng chưa đăng nhập"))

        val pendingOrders = ticketService.getPendingTickets()
        return ResponseEntity.ok(ApiResponse.success("Danh sách đơn hàng đang chờ thanh toán", pendingOrders))
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
        
        return ResponseEntity.ok(ApiResponse.success(tickets))
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
        return ResponseEntity.ok(ApiResponse.success(tickets))
    }
    
    /**
     * Check-in vé
     * Hỗ trợ check-in bằng ID hoặc mã vé
     */
    @PostMapping("/check-in")
    @PreAuthorize("hasRole('ORGANIZER')")
    fun checkInTicketWithRequest(@Valid @RequestBody request: TicketCheckInRequestDto): ResponseEntity<ApiResponse<TicketDto>> {
        val ticket = ticketService.checkInTicket(request)
        return ResponseEntity.ok(ApiResponse.success(
            "Đã check-in vé thành công",
            ticket
        ))
    }

    /**
     * Hủy vé
     */
    @PostMapping("/{ticketId}/cancel")
    fun cancelTicket(@PathVariable ticketId: UUID): ResponseEntity<ApiResponse<TicketDto>> {
        val ticket = ticketService.cancelTicket(ticketId)
        return ResponseEntity.ok(ApiResponse.success(ticket))
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
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Xử lý các vé đặt chỗ đã hết hạn (chỉ admin hoặc scheduled task)
     */
    @PostMapping("/process-expired")
    @PreAuthorize("hasRole('ADMIN')")
    fun processExpiredReservations(): ResponseEntity<ApiResponse<Map<String, Int>>> {
        val count = ticketService.processExpiredReservations()
        return ResponseEntity.ok(ApiResponse.success(mapOf("expiredCount" to count)))
    }
} 