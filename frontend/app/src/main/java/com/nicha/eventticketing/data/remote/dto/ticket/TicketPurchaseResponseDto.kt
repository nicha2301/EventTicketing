package com.nicha.eventticketing.data.remote.dto.ticket

/**
 * DTO cho mua vé
 */
data class TicketPurchaseResponseDto(
    val orderId: String,
    val eventId: String,
    val eventTitle: String,
    val tickets: List<TicketPurchaseDto>,
    val totalAmount: Double,
    val paymentId: String?,
    val paymentStatus: String,
    val paymentUrl: String?,
    val purchaseDate: String,
    val buyerName: String,
    val buyerEmail: String,
    val buyerPhone: String,
    val discountAmount: Double = 0.0,
    val promoCode: String?
)

data class TicketPurchaseDto(
    val id: String,
    val ticketNumber: String?,
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
    val checkedInAt: String?,
    val cancelledAt: String?,
    val paymentId: String?,
    val paymentStatus: String,
    val eventStartDate: String,
    val eventEndDate: String,
    val eventLocation: String,
    val eventAddress: String,
    val eventImageUrl: String?
)
