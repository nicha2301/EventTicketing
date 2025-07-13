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
    val id: UUID,
    val userId: UUID,
    val userName: String,
    val ticketId: UUID,
    val eventId: UUID,
    val eventTitle: String,
    val ticketTypeName: String,
    val amount: BigDecimal,
    val paymentMethod: String,
    val transactionId: String?,
    val status: PaymentStatus,
    val paymentUrl: String?,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val updatedAt: LocalDateTime,
    
    val refundedAmount: BigDecimal? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val refundedAt: LocalDateTime? = null,
    
    val metadata: Map<String, String>? = null
) 