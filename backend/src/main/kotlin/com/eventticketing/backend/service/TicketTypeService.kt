package com.eventticketing.backend.service

import com.eventticketing.backend.dto.TicketTypeDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface TicketTypeService {
    /**
     * Tạo loại vé mới cho một sự kiện
     */
    fun createTicketType(eventId: UUID, ticketTypeDto: TicketTypeDto): TicketTypeDto
    
    /**
     * Cập nhật thông tin loại vé
     */
    fun updateTicketType(id: UUID, ticketTypeDto: TicketTypeDto): TicketTypeDto
    
    /**
     * Lấy thông tin loại vé theo ID
     */
    fun getTicketTypeById(id: UUID): TicketTypeDto
    
    /**
     * Lấy tất cả loại vé của một sự kiện
     */
    fun getTicketTypesByEventId(eventId: UUID, pageable: Pageable): Page<TicketTypeDto>
    
    /**
     * Xóa loại vé
     */
    fun deleteTicketType(id: UUID): Boolean
    
    /**
     * Cập nhật số lượng vé còn lại
     */
    fun updateAvailableQuantity(id: UUID, purchasedQuantity: Int): TicketTypeDto
    
    /**
     * Kiểm tra tình trạng bán vé
     */
    fun checkTicketAvailability(id: UUID, quantity: Int): Boolean
} 