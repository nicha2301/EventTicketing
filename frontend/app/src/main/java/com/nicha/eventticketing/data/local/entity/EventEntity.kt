package com.nicha.eventticketing.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nicha.eventticketing.data.local.converter.StringListConverter
import com.nicha.eventticketing.data.local.converter.TicketTypeListConverter

@Entity(tableName = "events")
@TypeConverters(StringListConverter::class, TicketTypeListConverter::class)
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String,
    val location: String,
    val address: String?,
    val organizerId: String,
    val organizerName: String?,
    val featuredImageUrl: String?,
    val bannerImageUrl: String?,
    val status: String,
    val type: String?,
    val tags: List<String>?,
    val createdAt: String?,
    val updatedAt: String?,
    val ticketTypes: List<com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto>?,
    val imageUrls: List<String>?
) 