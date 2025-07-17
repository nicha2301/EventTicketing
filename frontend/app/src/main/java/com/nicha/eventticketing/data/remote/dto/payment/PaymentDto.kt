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

/**
 * DTO cho việc tạo Payment mới
 */
data class PaymentCreateDto(
    val amount: Double,
    val currency: String = "VND",
    val paymentMethod: String,
    val orderId: String
)

/**
 * DTO cho việc cập nhật trạng thái Payment
 */
data class PaymentStatusUpdateDto(
    val status: String,
    val transactionId: String?
)

/**
 * DTO cho thông tin phương thức thanh toán
 */
data class PaymentMethodDto(
    val id: String,
    val name: String,
    val code: String,
    val icon: String?,
    val isActive: Boolean
) 