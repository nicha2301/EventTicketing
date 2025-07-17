package com.nicha.eventticketing.data.model

import java.util.Date
import java.util.UUID

data class PaymentDto(
    val id: UUID,
    val orderId: UUID,
    val amount: Double,
    val paymentMethod: String,
    val status: String, // PENDING, COMPLETED, FAILED, CANCELLED
    val transactionId: String?,
    val createdAt: Date,
    val updatedAt: Date
)

data class PaymentCreateDto(
    val orderId: UUID,
    val amount: Double,
    val paymentMethod: String, // VNPAY, STRIPE, etc.
    val returnUrl: String?
)

data class PaymentResponseDto(
    val paymentId: UUID,
    val redirectUrl: String?,
    val status: String
) 