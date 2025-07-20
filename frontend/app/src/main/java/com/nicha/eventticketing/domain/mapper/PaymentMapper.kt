package com.nicha.eventticketing.domain.mapper

import com.nicha.eventticketing.data.remote.dto.payment.PaymentDto
import com.nicha.eventticketing.domain.model.Payment
import com.nicha.eventticketing.domain.model.PaymentMethod
import com.nicha.eventticketing.domain.model.PaymentStatus
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

    private fun parseDate(dateString: String): Date {
        return try {
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
} 