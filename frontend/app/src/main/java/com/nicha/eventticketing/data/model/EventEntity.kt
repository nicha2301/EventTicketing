package com.nicha.eventticketing.data.model

import java.time.LocalDateTime
import java.util.Date

enum class EventStatus {
    UPCOMING, ONGOING, COMPLETED, CANCELLED
}

data class EventEntity(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val organizerName: String,
    val startDate: String,
    val endDate: String,
    val startTime: String = "",
    val endTime: String = "",
    val type: EventType = EventType.OTHER,
    val price: Double = 0.0,
    val availableTickets: Int = 0,
    val isFeatured: Boolean = false,
    val featuredImageUrl: String = "",
    val bannerImageUrl: String = "",
    val tags: List<String> = emptyList(),
    val status: EventStatus = EventStatus.UPCOMING,
    val address: String = "",
    val organizerId: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    
    // Các trường cũ để tương thích với code hiện tại
    val imageUrl: String = featuredImageUrl,
    val startDateTime: LocalDateTime = LocalDateTime.now(),
    val endDateTime: LocalDateTime = LocalDateTime.now(),
    val eventType: EventType = type
) 