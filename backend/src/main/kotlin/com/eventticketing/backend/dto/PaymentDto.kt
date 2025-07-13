package com.eventticketing.backend.dto

import com.eventticketing.backend.entity.PaymentStatus
import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class PaymentDto(
    val id: UUID?,
    val userId: UUID,
    val userName: String,
    val ticketId: UUID,
    val eventId: UUID,
    val eventTitle: String,
    val ticketTypeName: String,
    val amount: BigDecimal,
    val paymentMethod: String,
    val transactionId: String,
    val status: PaymentStatus,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val paymentDate: LocalDateTime
)

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