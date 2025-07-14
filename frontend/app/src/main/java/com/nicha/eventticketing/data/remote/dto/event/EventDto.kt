package com.nicha.eventticketing.data.remote.dto.event

import java.util.Date

data class EventDto(
    val id: String,
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val status: String, // DRAFT, PUBLISHED, CANCELLED, COMPLETED
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val location: String?,
    val organizerId: String,
    val organizerName: String,
    val categoryId: String?,
    val categoryName: String?,
    val featuredImageUrl: String?,
    val imageUrl: String?,
    val minTicketPrice: Double?,
    val maxTicketPrice: Double?,
    val price: Double?,
    val ticketsSold: Int,
    val totalTickets: Int,
    val availableSeats: Int?,
    val totalSeats: Int?,
    val averageRating: Double,
    val ratingCount: Int,
    val createdAt: String,
    val updatedAt: String
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

data class PageDto<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
) 