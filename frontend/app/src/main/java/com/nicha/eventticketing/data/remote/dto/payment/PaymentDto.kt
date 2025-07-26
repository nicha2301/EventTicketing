package com.nicha.eventticketing.data.remote.dto.payment

/**
 * DTO cho thông tin thanh toán
 */
data class PaymentDto(
    val id: String,
    val userId: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val paymentMethod: String,
    val transactionId: String?,
    val orderId: String,
    val createdAt: String,
    val updatedAt: String
)
