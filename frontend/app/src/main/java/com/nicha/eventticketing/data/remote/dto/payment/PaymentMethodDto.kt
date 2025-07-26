package com.nicha.eventticketing.data.remote.dto.payment

/**
 * DTO cho cập nhật trạng thái Payment
 */
data class PaymentStatusUpdateDto(
    val status: String,
    val transactionId: String? = null
)

/**
 * DTO cho thông tin phương thức thanh toán
 */
data class PaymentMethodDto(
    val id: String,
    val name: String,
    val code: String,
    val icon: String? = null,
    val isActive: Boolean = true
)
