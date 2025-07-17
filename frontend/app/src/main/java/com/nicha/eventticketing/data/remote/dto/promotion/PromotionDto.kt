package com.nicha.eventticketing.data.remote.dto.promotion

/**
 * DTO cho thông tin khuyến mãi
 */
data class PromotionDto(
    val id: String,
    val code: String,
    val name: String,
    val description: String?,
    val discountType: String, // PERCENTAGE, FIXED_AMOUNT
    val discountValue: Double,
    val minPurchaseAmount: Double?,
    val maxDiscountAmount: Double?,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean,
    val usageLimit: Int?,
    val usageCount: Int,
    val eventId: String?,
    val createdAt: String,
    val updatedAt: String
)

/**
 * DTO cho việc tạo Promotion mới
 */
data class PromotionCreateDto(
    val code: String,
    val name: String,
    val description: String?,
    val discountType: String,
    val discountValue: Double,
    val minPurchaseAmount: Double?,
    val maxDiscountAmount: Double?,
    val startDate: String,
    val endDate: String,
    val usageLimit: Int?,
    val eventId: String?
)

/**
 * DTO cho việc cập nhật Promotion
 */
data class PromotionUpdateDto(
    val name: String?,
    val description: String?,
    val discountType: String?,
    val discountValue: Double?,
    val minPurchaseAmount: Double?,
    val maxDiscountAmount: Double?,
    val startDate: String?,
    val endDate: String?,
    val isActive: Boolean?,
    val usageLimit: Int?,
    val eventId: String?
)

/**
 * DTO cho việc xác thực mã khuyến mãi
 */
data class PromotionValidationDto(
    val code: String,
    val eventId: String?,
    val amount: Double
) 