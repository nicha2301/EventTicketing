package com.nicha.eventticketing.data.remote.dto.ticket

/**
 * DTO cho thông tin vé
 */
data class TicketDto(
    val id: String,
    val ticketNumber: String,
    val userId: String,
    val userName: String,
    val eventId: String,
    val eventTitle: String,
    val ticketTypeId: String,
    val ticketTypeName: String,
    val price: Double,
    val status: String,
    val qrCodeUrl: String?,
    val purchaseDate: String?,
    val eventStartDate: String,
    val eventEndDate: String,
    val eventLocation: String,
    val eventAddress: String,
    val eventImageUrl: String?
)

/**
 * DTO cho việc tạo Ticket mới
 */
data class TicketCreateDto(
    val eventId: String,
    val userId: String,
    val ticketType: String,
    val price: Double
)

/**
 * DTO cho việc cập nhật trạng thái Ticket
 */
data class TicketStatusUpdateDto(
    val isUsed: Boolean,
    val status: String
)
