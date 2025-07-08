package com.eventticketing.backend.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class TicketPurchaseResponseDto(
    val orderId: UUID,
    
    val eventId: UUID,
    
    val eventTitle: String,
    
    val tickets: List<TicketDto>,
    
    val totalAmount: BigDecimal,
    
    val paymentId: UUID? = null,
    
    val paymentStatus: String,
    
    val paymentUrl: String? = null,
    
    val purchaseDate: LocalDateTime,
    
    val buyerName: String,
    
    val buyerEmail: String,
    
    val buyerPhone: String? = null,
    
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    
    val promoCode: String? = null
) 