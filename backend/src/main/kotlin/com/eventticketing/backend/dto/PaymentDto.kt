package com.eventticketing.backend.dto

import com.eventticketing.backend.entity.PaymentStatus
import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * DTO cho yêu cầu thanh toán
 */
data class PaymentRequestDto(
    val userId: UUID,
    val amount: BigDecimal,
    val currency: String = "VND",
    val description: String,
    val paymentMethod: String,
    val ticketIds: List<UUID>,
    val eventId: UUID,
    val returnUrl: String? = null
)

/**
 * DTO cho phản hồi thanh toán
 */
data class PaymentResponseDto(
    val success: Boolean,
    val redirectUrl: String? = null,
    val transactionId: String? = null,
    val message: String
)

/**
 * DTO cho thông tin thanh toán
 */
data class PaymentDto(
    val id: UUID? = null,
    val userId: UUID,
    val amount: BigDecimal,
    val paymentMethod: String,
    val transactionId: String? = null,
    val status: PaymentStatus,
    val createdAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val ticketIds: List<UUID>? = null
)

/**
 * Enum cho trạng thái thanh toán
 */
enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}

data class PaymentCreateDto(
    val ticketId: UUID,
    val amount: BigDecimal,
    val paymentMethod: String,
    val transactionId: String
)

data class PaymentUpdateDto(
    val status: PaymentStatus
)

data class PaymentSummaryDto(
    val totalAmount: BigDecimal,
    val completedPayments: Int,
    val pendingPayments: Int,
    val failedPayments: Int,
    val refundedPayments: Int
) 