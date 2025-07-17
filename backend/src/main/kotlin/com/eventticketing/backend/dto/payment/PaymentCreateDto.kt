package com.eventticketing.backend.dto.payment

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

/**
 * DTO for creating a new payment
 */
data class PaymentCreateDto(
    @field:NotNull(message = "Ticket ID is required")
    val ticketId: UUID,
    
    @field:NotNull(message = "Amount is required")
    @field:Positive(message = "Amount must be positive")
    val amount: BigDecimal,
    
    val paymentMethod: String = "momo",
    
    val returnUrl: String = "",
    
    // Optional fields
    val description: String? = null,
    val metadata: Map<String, String>? = null
) 