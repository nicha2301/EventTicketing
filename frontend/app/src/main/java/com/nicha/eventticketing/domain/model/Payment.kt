package com.nicha.eventticketing.domain.model

import java.util.Date

/**
 * Domain model cho Payment, được sử dụng trong business logic
 */
data class Payment(
    val id: String,
    val userId: String,
    val eventId: String,
    val ticketId: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus,
    val transactionId: String?,
    val transactionDate: Date,
    val refundStatus: RefundStatus?
)

enum class PaymentMethod {
    CREDIT_CARD, BANK_TRANSFER, PAYPAL, MOMO, ZALOPAY, CASH, OTHER, UNKNOWN;
    
    companion object {
        fun fromString(method: String): PaymentMethod {
            return try {
                valueOf(method.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
}

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED, UNKNOWN;
    
    companion object {
        fun fromString(status: String): PaymentStatus {
            return try {
                valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
}

enum class RefundStatus {
    REQUESTED, APPROVED, REJECTED, COMPLETED, UNKNOWN;
    
    companion object {
        fun fromString(status: String): RefundStatus {
            return try {
                valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
} 