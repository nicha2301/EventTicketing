package com.nicha.eventticketing.domain.mapper

import com.nicha.eventticketing.data.remote.dto.payment.PaymentDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentRequestDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentResponseDto
import com.nicha.eventticketing.domain.model.PaymentMethod
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mapper class for Payment related objects
 */
class PaymentMapper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    fun mapToDomainModel(dto: PaymentDto): com.nicha.eventticketing.domain.model.Payment {
        return com.nicha.eventticketing.domain.model.Payment(
            id = dto.id,
            userId = dto.userId,
            eventId = "", 
            ticketId = "", 
            amount = dto.amount,
            paymentMethod = PaymentMethod.fromCode(dto.paymentMethod) ?: PaymentMethod.MOMO,
            status = com.nicha.eventticketing.domain.model.PaymentStatus.fromString(dto.status),
            transactionId = dto.transactionId,
            transactionDate = parseDate(dto.createdAt),
            refundStatus = null
        )
    }

    /**
     * Convert PaymentResponseDto to domain model Payment
     */
    fun mapToDomainModel(dto: PaymentResponseDto): com.nicha.eventticketing.domain.model.Payment {
        return com.nicha.eventticketing.domain.model.Payment(
            id = dto.id ?: dto.paymentId ?: "",  
            userId = dto.userId ?: "",
            eventId = dto.eventId ?: "",
            ticketId = dto.ticketId ?: "",
            amount = dto.amount,
            paymentMethod = PaymentMethod.fromCode(dto.paymentMethod) ?: PaymentMethod.MOMO,
            status = com.nicha.eventticketing.domain.model.PaymentStatus.fromString(dto.status),
            transactionId = dto.transactionId,
            transactionDate = parseDate(dto.createdAt),
            refundStatus = dto.refundStatus?.let { 
                com.nicha.eventticketing.domain.model.RefundStatus.fromString(it) 
            }
        )
    }

    /**
     * Convert domain model Payment to PaymentRequestDto
     */
    fun mapToRequestDto(
        ticketId: String,
        amount: Double,
        paymentMethod: String,
        description: String? = null,
        returnUrl: String = "",
        metadata: Map<String, Any>? = null
    ): PaymentRequestDto {
        return PaymentRequestDto(
            ticketId = ticketId,
            amount = amount,
            paymentMethod = paymentMethod,
            description = description,
            returnUrl = returnUrl,
            metadata = metadata
        )
    }

    private fun parseDate(dateString: String): Date {
        return try {
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}