package com.nicha.eventticketing.data.remote.dto.ticket

/**
 * DTO cho việc mua vé
 */
data class TicketPurchaseDto(
    val eventId: String,
    val buyerName: String,
    val buyerEmail: String,
    val buyerPhone: String,
    val paymentMethod: String,
    val tickets: List<TicketPurchaseItemDto>,
    val promoCode: String? = null
)

/**
 * DTO cho mỗi loại vé trong yêu cầu mua
 */
data class TicketPurchaseItemDto(
    val ticketTypeId: String,
    val quantity: Int
)

/**
 * DTO cho kết quả mua vé
 */
data class TicketPurchaseResponseDto(
    val orderId: String,
    val eventId: String,
    val eventTitle: String,
    val tickets: List<TicketDto>,
    val totalAmount: Double,
    val paymentId: String,
    val paymentStatus: String,
    val paymentUrl: String?,
    val purchaseDate: String,
    val buyerName: String,
    val buyerEmail: String,
    val buyerPhone: String,
    val discountAmount: Double?,
    val promoCode: String?
) 