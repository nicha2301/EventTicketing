package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.ticket.CheckInRequestDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseResponseDto
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository xử lý các chức năng liên quan đến vé
 */
interface TicketRepository {
    /**
     * Mua vé
     * @param purchaseDto Thông tin mua vé
     * @return Flow<Resource<TicketPurchaseResponseDto>> Flow chứa thông tin kết quả mua vé
     */
    fun purchaseTickets(purchaseDto: TicketPurchaseDto): Flow<Resource<TicketPurchaseResponseDto>>
    
    /**
     * Lấy thông tin vé theo ID
     * @param ticketId ID của vé
     * @return Flow<Resource<TicketDto>> Flow chứa thông tin vé
     */
    fun getTicketById(ticketId: String): Flow<Resource<TicketDto>>
    
    /**
     * Lấy thông tin vé theo số vé
     * @param ticketNumber Số vé
     * @return Flow<Resource<TicketDto>> Flow chứa thông tin vé
     */
    fun getTicketByNumber(ticketNumber: String): Flow<Resource<TicketDto>>
    
    /**
     * Lấy danh sách vé của người dùng hiện tại
     * @param status Trạng thái vé (tùy chọn)
     * @param page Số trang
     * @param size Số lượng mỗi trang
     * @return Flow<Resource<PageDto<TicketDto>>> Flow chứa danh sách vé theo trang
     */
    fun getMyTickets(status: String? = null, page: Int = 0, size: Int = 10): Flow<Resource<PageDto<TicketDto>>>
    
    /**
     * Check-in vé
     * @param request Thông tin check-in
     * @return Flow<Resource<TicketDto>> Flow chứa thông tin vé đã check-in
     */
    fun checkInTicket(request: CheckInRequestDto): Flow<Resource<TicketDto>>
    
    /**
     * Hủy vé
     * @param ticketId ID của vé
     * @return Flow<Resource<TicketDto>> Flow chứa thông tin vé đã hủy
     */
    fun cancelTicket(ticketId: String): Flow<Resource<TicketDto>>
    
    /**
     * Lấy danh sách vé của sự kiện
     * @param eventId ID của sự kiện
     * @param status Trạng thái vé (tùy chọn)
     * @param page Số trang
     * @param size Số lượng mỗi trang
     * @return Flow<Resource<PageDto<TicketDto>>> Flow chứa danh sách vé theo trang
     */
    fun getEventTickets(eventId: String, status: String? = null, page: Int = 0, size: Int = 20): Flow<Resource<PageDto<TicketDto>>>
    
    /**
     * Gửi lại email xác nhận vé
     * @param ticketId ID của vé
     * @return Flow<Resource<Boolean>> Flow chứa kết quả gửi email
     */
    fun resendTicketConfirmation(ticketId: String): Flow<Resource<Boolean>>
    
    /**
     * Xác thực vé
     * @param ticketId ID của vé
     * @param code Mã xác thực
     * @return Flow<Resource<TicketDto>> Flow chứa thông tin vé đã xác thực
     */
    fun validateTicket(ticketId: String, code: String): Flow<Resource<TicketDto>>
    
    /**
     * Chuyển nhượng vé
     * @param ticketId ID của vé
     * @param email Email của người nhận
     * @return Flow<Resource<TicketDto>> Flow chứa thông tin vé đã chuyển nhượng
     */
    fun transferTicket(ticketId: String, email: String): Flow<Resource<TicketDto>>
} 