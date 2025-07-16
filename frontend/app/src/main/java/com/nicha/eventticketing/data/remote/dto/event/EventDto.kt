package com.nicha.eventticketing.data.remote.dto.event

import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto

/**
 * DTO cho thông tin sự kiện
 */
data class EventDto(
    val id: String,
    val title: String,
    val description: String,
    val shortDescription: String?,
    val organizerId: String,
    val organizerName: String,
    val categoryId: String,
    val categoryName: String,
    val locationId: String,
    val locationName: String,
    val address: String,
    val city: String,
    val latitude: Double?,
    val longitude: Double?,
    val status: String,
    val maxAttendees: Int,
    val currentAttendees: Int,
    val featuredImageUrl: String?,
    val imageUrls: List<String>,
    val minTicketPrice: Double?,
    val maxTicketPrice: Double?,
    val startDate: String,
    val endDate: String,
    val createdAt: String,
    val updatedAt: String,
    val isPrivate: Boolean,
    val isFeatured: Boolean,
    val isFree: Boolean,
    val ticketTypes: List<TicketTypeDto>?
)

data class EventCreateDto(
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val location: String,
    val imageUrl: String?,
    val categoryId: String?,
    val price: Double,
    val totalSeats: Int
)

data class EventUpdateDto(
    val title: String?,
    val description: String?,
    val startDate: String?,
    val endDate: String?,
    val location: String?,
    val imageUrl: String?,
    val categoryId: String?,
    val price: Double?,
    val totalSeats: Int?,
    val status: String?
) 