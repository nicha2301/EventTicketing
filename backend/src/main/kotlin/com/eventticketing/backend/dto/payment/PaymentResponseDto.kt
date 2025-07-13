package com.eventticketing.backend.dto.payment

import com.eventticketing.backend.entity.PaymentStatus
import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * DTO for payment response
 */
data class PaymentResponseDto(
    val id: UUID? = null,
    val userId: UUID? = null,
    val userName: String? = null,
    val ticketId: UUID? = null,
    val eventId: UUID? = null,
    val eventTitle: String? = null,
    val ticketTypeName: String? = null,
    val amount: BigDecimal? = null,
    val paymentMethod: String? = null,
    val transactionId: String? = null,
    val status: PaymentStatus? = null,
    val paymentUrl: String? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val updatedAt: LocalDateTime? = null,
    
    val refundedAmount: BigDecimal? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val refundedAt: LocalDateTime? = null,
    
    val metadata: Map<String, String>? = null,
    
    // Các trường bổ sung cho payment gateway
    val success: Boolean? = null,
    val redirectUrl: String? = null,
    val message: String? = null
) {
    // Constructor thứ hai cho payment gateway
    constructor(
        success: Boolean,
        redirectUrl: String,
        transactionId: String?,
        message: String
    ) : this(
        transactionId = transactionId,
        paymentUrl = redirectUrl,
        success = success,
        redirectUrl = redirectUrl,
        message = message
    )
} 