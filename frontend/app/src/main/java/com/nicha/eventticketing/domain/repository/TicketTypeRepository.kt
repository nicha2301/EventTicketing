package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypePageResponse
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository xử lý các chức năng liên quan đến loại vé
 */
interface TicketTypeRepository {
    /**
     * Lấy danh sách loại vé của sự kiện
     * @param eventId ID của sự kiện
     * @param page Số trang
     * @param size Số lượng mỗi trang
     * @return Flow<Resource<TicketTypePageResponse>> Flow chứa danh sách loại vé theo trang
     */
    fun getTicketTypes(eventId: String, page: Int = 0, size: Int = 20): Flow<Resource<TicketTypePageResponse>>
    
    /**
     * Lấy thông tin chi tiết của một loại vé
     * @param ticketTypeId ID của loại vé
     * @return Flow<Resource<TicketTypeDto>> Flow chứa thông tin loại vé
     */
    fun getTicketTypeById(ticketTypeId: String): Flow<Resource<TicketTypeDto>>
    
    /**
     * Tạo loại vé mới cho sự kiện
     * @param eventId ID của sự kiện
     * @param ticketType Thông tin loại vé cần tạo
     * @return Flow<Resource<TicketTypeDto>> Flow chứa thông tin loại vé đã tạo
     */
    fun createTicketType(eventId: String, ticketType: TicketTypeDto): Flow<Resource<TicketTypeDto>>
    
    /**
     * Cập nhật thông tin loại vé
     * @param ticketTypeId ID của loại vé
     * @param ticketType Thông tin loại vé cần cập nhật
     * @return Flow<Resource<TicketTypeDto>> Flow chứa thông tin loại vé đã cập nhật
     */
    fun updateTicketType(ticketTypeId: String, ticketType: TicketTypeDto): Flow<Resource<TicketTypeDto>>
    
    /**
     * Xóa loại vé
     * @param ticketTypeId ID của loại vé
     * @return Flow<Resource<Boolean>> Flow chứa kết quả xóa
     */
    fun deleteTicketType(ticketTypeId: String): Flow<Resource<Boolean>>
    
    /**
     * Kiểm tra tình trạng còn vé của loại vé
     * @param ticketTypeId ID của loại vé
     * @return Flow<Resource<Boolean>> Flow chứa kết quả kiểm tra
     */
    fun checkTicketTypeAvailability(ticketTypeId: String): Flow<Resource<Boolean>>
    
    /**
     * Cập nhật số lượng vé còn lại
     * @param ticketTypeId ID của loại vé
     * @param quantity Số lượng vé còn lại
     * @return Flow<Resource<TicketTypeDto>> Flow chứa thông tin loại vé đã cập nhật
     */
    fun updateTicketTypeQuantity(ticketTypeId: String, quantity: Int): Flow<Resource<TicketTypeDto>>
} 