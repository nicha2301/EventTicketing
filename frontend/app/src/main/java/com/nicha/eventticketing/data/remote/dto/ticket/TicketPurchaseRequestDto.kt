package com.nicha.eventticketing.data.remote.dto.ticket

/**
 * DTO cho mua vé
 */
data class TicketPurchaseRequestDto(
    val eventId: String,
    val tickets: List<TicketPurchaseItemDto>,
    val buyerName: String,
    val buyerEmail: String,
    val buyerPhone: String,
    val paymentMethod: String,
    val promoCode: String? = null
)
