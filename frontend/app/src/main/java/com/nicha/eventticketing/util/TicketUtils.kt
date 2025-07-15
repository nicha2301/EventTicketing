package com.nicha.eventticketing.util

import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto

/**
 * Lớp tiện ích cho việc xử lý vé
 */
object TicketUtils {
    
    /**
     * Enum class đại diện cho các loại vé
     */
    enum class TicketType {
        REGULAR,
        VIP
    }
    
    /**
     * Xác định loại vé từ TicketTypeDto
     * @param ticketType DTO của loại vé
     * @return TicketType enum đại diện cho loại vé
     */
    fun determineTicketType(ticketType: TicketTypeDto): TicketType {
        return if (isVipTicket(ticketType)) TicketType.VIP else TicketType.REGULAR
    }
    
    /**
     * Kiểm tra xem một loại vé có phải là vé VIP không
     * @param ticketType DTO của loại vé
     * @return true nếu là vé VIP, false nếu không phải
     */
    fun isVipTicket(ticketType: TicketTypeDto): Boolean {
        return ticketType.name.contains("VIP", ignoreCase = true)
    }
    
    /**
     * Lọc danh sách vé để chỉ giữ lại vé thường và vé VIP
     * @param ticketTypes Danh sách các loại vé
     * @return Danh sách đã lọc chỉ chứa vé thường và vé VIP
     */
    fun filterTicketTypes(ticketTypes: List<TicketTypeDto>?): List<TicketTypeDto> {
        if (ticketTypes == null) return emptyList()
        
        // Tìm vé VIP và vé thường
        val vipTicket = ticketTypes.find { isVipTicket(it) }
        
        // Nếu có vé VIP, tìm vé thường (vé không phải VIP có giá thấp nhất)
        val regularTicket = if (vipTicket != null) {
            ticketTypes.filter { !isVipTicket(it) }
                .minByOrNull { it.price }
        } else {
            // Nếu không có vé VIP, lấy vé có giá thấp nhất làm vé thường
            ticketTypes.minByOrNull { it.price }
        }
        
        // Trả về danh sách chỉ chứa vé thường và vé VIP (nếu có)
        return listOfNotNull(regularTicket, vipTicket)
    }
    
    /**
     * Tạo tên hiển thị cho loại vé
     * @param ticketType DTO của loại vé
     * @return Tên hiển thị của loại vé
     */
    fun getDisplayName(ticketType: TicketTypeDto): String {
        return if (isVipTicket(ticketType)) "Vé VIP" else "Vé Thường"
    }
} 