package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.dto.TicketTypeDto
import com.eventticketing.backend.service.TicketTypeService
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
@RequestMapping("/api")
class TicketTypeController(
    private val ticketTypeService: TicketTypeService
) {

    @PostMapping("/events/{eventId}/ticket-types")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun createTicketType(
        @PathVariable eventId: UUID,
        @Valid @RequestBody ticketTypeDto: TicketTypeDto
    ): ResponseEntity<ApiResponse<TicketTypeDto>> {
        val createdTicketType = ticketTypeService.createTicketType(eventId, ticketTypeDto)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                "Đã tạo loại vé thành công",
                createdTicketType
            )
        )
    }

    @PutMapping("/ticket-types/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun updateTicketType(
        @PathVariable id: UUID,
        @Valid @RequestBody ticketTypeDto: TicketTypeDto
    ): ResponseEntity<ApiResponse<TicketTypeDto>> {
        val updatedTicketType = ticketTypeService.updateTicketType(id, ticketTypeDto)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã cập nhật loại vé thành công",
                updatedTicketType
            )
        )
    }

    @GetMapping("/ticket-types/{id}")
    fun getTicketTypeById(@PathVariable id: UUID): ResponseEntity<ApiResponse<TicketTypeDto>> {
        val ticketType = ticketTypeService.getTicketTypeById(id)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy thông tin loại vé thành công",
                ticketType
            )
        )
    }

    @GetMapping("/events/{eventId}/ticket-types")
    fun getTicketTypesByEventId(
        @PathVariable eventId: UUID,
        @PageableDefault(size = 20, sort = ["price"]) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<TicketTypeDto>>> {
        val ticketTypes = ticketTypeService.getTicketTypesByEventId(eventId, pageable)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách loại vé thành công",
                ticketTypes
            )
        )
    }

    @DeleteMapping("/ticket-types/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun deleteTicketType(@PathVariable id: UUID): ResponseEntity<ApiResponse<Boolean>> {
        val result = ticketTypeService.deleteTicketType(id)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã xóa loại vé thành công",
                result
            )
        )
    }

    @GetMapping("/ticket-types/{id}/availability")
    fun checkTicketAvailability(
        @PathVariable id: UUID,
        @RequestParam(defaultValue = "1") quantity: Int
    ): ResponseEntity<ApiResponse<Boolean>> {
        val isAvailable = ticketTypeService.checkTicketAvailability(id, quantity)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                if (isAvailable) "Loại vé có sẵn" else "Loại vé không có sẵn",
                isAvailable
            )
        )
    }
} 