package com.eventticketing.backend.dto.payment

import java.math.BigDecimal
import java.util.UUID

/**
 * DTO cho yêu cầu thanh toán gửi đến payment gateway
 */
data class PaymentRequestDto(
    val amount: BigDecimal,
    val description: String,
    val paymentMethod: String,
    val returnUrl: String,
    val metadata: Map<String, String> = emptyMap()
) 