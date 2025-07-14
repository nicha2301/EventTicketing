package com.nicha.eventticketing.data.remote.dto.ticket

/**
 * DTO cho Ticket từ API
 */
data class TicketDto(
    val id: String,
    val eventId: String,
    val eventTitle: String,
    val eventImageUrl: String?,
    val userId: String,
    val ticketCode: String,
    val ticketType: String, // VIP, STANDARD, EARLY_BIRD
    val price: Double,
    val purchaseDate: String,
    val isUsed: Boolean,
    val usedDate: String?,
    val expiryDate: String?,
    val status: String, // ACTIVE, USED, EXPIRED, CANCELLED
    val createdAt: String,
    val updatedAt: String
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

data class TicketPurchaseDto(
    val eventId: String,
    val quantity: Int,
    val paymentMethod: String // CREDIT_CARD, PAYPAL, etc.
)

data class TicketPurchaseResponseDto(
    val ticketIds: List<String>,
    val totalAmount: Double,
    val paymentId: String,
    val paymentStatus: String,
    val createdAt: String
)
