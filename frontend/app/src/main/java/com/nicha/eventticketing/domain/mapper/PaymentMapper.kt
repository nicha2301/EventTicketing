package com.nicha.eventticketing.domain.mapper

import com.nicha.eventticketing.data.local.entity.PaymentEntity
import com.nicha.eventticketing.data.remote.dto.payment.PaymentDto
import com.nicha.eventticketing.domain.model.Payment
import com.nicha.eventticketing.domain.model.PaymentMethod
import com.nicha.eventticketing.domain.model.PaymentStatus
import com.nicha.eventticketing.domain.model.RefundStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper để chuyển đổi giữa PaymentDto và Payment domain model
 */
@Singleton
class PaymentMapper @Inject constructor() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    fun mapToDomainModel(dto: PaymentDto): Payment {
        return Payment(
            id = dto.id,
            userId = dto.userId,
            eventId = "", // Không có trong PaymentDto mới
            ticketId = "", // Không có trong PaymentDto mới
            amount = dto.amount,
            paymentMethod = PaymentMethod.fromString(dto.paymentMethod),
            status = PaymentStatus.fromString(dto.status),
            transactionId = dto.transactionId,
            transactionDate = parseDate(dto.createdAt), // Sử dụng createdAt thay cho transactionDate
            refundStatus = null // Không có trong PaymentDto mới
        )
    }

    fun mapToEntity(dto: PaymentDto): PaymentEntity {
        return PaymentEntity(
            id = dto.id,
            userId = dto.userId,
            amount = dto.amount,
            currency = dto.currency,
            paymentMethod = dto.paymentMethod,
            status = dto.status,
            transactionCode = dto.transactionId,
            orderCode = dto.orderId,
            createdAt = parseDate(dto.createdAt),
            updatedAt = parseDate(dto.updatedAt)
        )
    }

    fun mapToDomainModel(entity: PaymentEntity): Payment {
        return Payment(
            id = entity.id,
            userId = entity.userId,
            eventId = "", // Not available in entity
            ticketId = "", // Not available in entity
            amount = entity.amount,
            paymentMethod = PaymentMethod.fromString(entity.paymentMethod),
            status = PaymentStatus.fromString(entity.status),
            transactionId = entity.transactionCode,
            transactionDate = entity.createdAt, // Using createdAt as transaction date
            refundStatus = null // No refund status in entity
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