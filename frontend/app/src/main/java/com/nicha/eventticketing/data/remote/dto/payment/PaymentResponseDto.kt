package com.nicha.eventticketing.data.remote.dto.payment

/**
 * DTO cho phản hồi thanh toán
 */
data class PaymentResponseDto(
    val id: String,
    val userId: String,
    val userName: String,
    val ticketId: String,
    val eventId: String,
    val eventTitle: String,
    val ticketTypeName: String,
    val amount: Double,
    val paymentMethod: String,
    val transactionId: String?,
    val status: String,
    val paymentUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    val refundedAmount: Double? = null,
    val refundedAt: String? = null,
    val metadata: Map<String, String>? = null
) 