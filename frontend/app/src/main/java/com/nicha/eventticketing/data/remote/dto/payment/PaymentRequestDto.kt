package com.nicha.eventticketing.data.remote.dto.payment

/**
 * DTO cho yêu cầu thanh toán
 */
data class PaymentRequestDto(
    val ticketId: String,
    val amount: Double,
    val paymentMethod: String,
    val returnUrl: String,
    val description: String? = null,
    val metadata: Map<String, String>? = null
) 