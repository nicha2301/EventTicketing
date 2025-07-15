package com.nicha.eventticketing.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val startDate: Date,
    val endDate: Date,
    val status: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val organizerId: String,
    val organizerName: String,
    val featuredImageUrl: String?,
    val minTicketPrice: Double?,
    val maxTicketPrice: Double?,
    val ticketsSold: Int,
    val totalTickets: Int,
    val averageRating: Double,
    val ratingCount: Int,
    val createdAt: Date,
    val updatedAt: Date,
    val isFavorite: Boolean = false,
    val lastUpdatedLocally: Date = Date()
) 