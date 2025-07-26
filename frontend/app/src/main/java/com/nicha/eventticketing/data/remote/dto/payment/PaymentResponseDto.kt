package com.nicha.eventticketing.data.remote.dto.payment

/**
 * DTO cho phản hồi thanh toán
 */
data class PaymentResponseDto(
    val id: String?,  
    val paymentId: String? = null,
    val userId: String? = null,
    val eventId: String? = null,
    val ticketId: String? = null,
    val amount: Double,
    val currency: String = "VND",
    val paymentMethod: String,
    val status: String,
    val transactionId: String? = null,
    val paymentUrl: String? = null,
    val orderId: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val refundStatus: String? = null,
    val metadata: Map<String, Any>? = null
)
