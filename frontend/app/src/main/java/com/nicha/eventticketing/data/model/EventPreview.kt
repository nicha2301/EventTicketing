package com.nicha.eventticketing.data.model

import java.util.Date

data class EventPreview(
    val id: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val address: String,
    val featuredImageUrl: String,
    val minTicketPrice: Double,
    val maxTicketPrice: Double,
    val isFavorite: Boolean = false,
    val ticketsSold: Int = 0,
    val totalTickets: Int = 0,
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val createdAt: Date,
    val updatedAt: Date
) 