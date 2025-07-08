package com.eventticketing.backend.dto.payment

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

/**
 * DTO for refund request
 */
data class RefundRequestDto(
    @field:NotNull(message = "Amount is required")
    @field:Positive(message = "Amount must be positive")
    val amount: BigDecimal,
    
    val reason: String,
    
    val metadata: Map<String, String>? = null
) 