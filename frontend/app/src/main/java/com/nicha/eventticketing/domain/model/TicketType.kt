package com.nicha.eventticketing.domain.model

/**
 * Enum đại diện cho loại vé của sự kiện
 */
enum class TicketType {
    VIP,
    STANDARD,
    EARLY_BIRD,
    GENERAL,
    PREMIUM;
    
    companion object {
        fun fromString(value: String?): TicketType {
            return try {
                valueOf(value?.uppercase() ?: "STANDARD")
            } catch (e: Exception) {
                STANDARD
            }
        }
    }
}

/**
 * Đại diện cho thông tin chi tiết loại vé của sự kiện
 */
data class TicketTypeInfo(
    val id: String,
    val name: String,
    val price: Double,
    val description: String,
    val availableQuantity: Int
) 