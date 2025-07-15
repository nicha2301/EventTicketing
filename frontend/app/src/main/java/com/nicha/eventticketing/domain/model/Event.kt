package com.nicha.eventticketing.domain.model

import java.util.Date

/**
 * Domain model cho Event, được sử dụng trong business logic
 * Khác với DTO, model này đã được xử lý và chuyển đổi để sử dụng trong ứng dụng
 */
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val startDate: Date,
    val endDate: Date,
    val status: EventStatus,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val location: String?,
    val organizerId: String,
    val organizerName: String,
    val category: Category?,
    val featuredImageUrl: String?,
    val imageUrl: String?,
    val pricing: EventPricing,
    val ticketInfo: TicketInfo,
    val rating: Rating,
    val createdAt: Date,
    val updatedAt: Date,
    val isFavorite: Boolean = false
)

data class EventPricing(
    val minPrice: Double?,
    val maxPrice: Double?,
    val basePrice: Double?
)

data class TicketInfo(
    val ticketsSold: Int,
    val totalTickets: Int,
    val availableSeats: Int?,
    val totalSeats: Int?
)

data class Rating(
    val average: Double,
    val count: Int
)

enum class EventStatus {
    DRAFT, PUBLISHED, CANCELLED, COMPLETED, UNKNOWN;
    
    companion object {
        fun fromString(status: String): EventStatus {
            return try {
                valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
} 