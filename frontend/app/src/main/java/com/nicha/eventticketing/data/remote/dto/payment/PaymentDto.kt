package com.nicha.eventticketing.data.remote.dto.payment

/**
 * DTO cho Payment từ API
 */
data class PaymentDto(
    val id: String,
    val userId: String,
    val eventId: String,
    val ticketId: String,
    val amount: Double,
    val paymentMethod: String, // CREDIT_CARD, BANK_TRANSFER, PAYPAL, MOMO, ZALOPAY, CASH
    val status: String, // PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED
    val transactionId: String?,
    val transactionDate: String,
    val refundStatus: String?, // REQUESTED, APPROVED, REJECTED, COMPLETED
    val createdAt: String,
    val updatedAt: String
)

/**
 * DTO cho việc tạo Payment mới
 */
data class PaymentCreateDto(
    val userId: String,
    val eventId: String,
    val ticketId: String,
    val amount: Double,
    val paymentMethod: String
)

/**
 * DTO cho việc cập nhật trạng thái Payment
 */
data class PaymentStatusUpdateDto(
    val status: String,
    val transactionId: String?
)

/**
 * DTO cho việc yêu cầu hoàn tiền
 */
data class RefundRequestDto(
    val paymentId: String,
    val reason: String
) 