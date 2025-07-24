package com.nicha.eventticketing.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nicha.eventticketing.data.local.converter.StringListConverter
import com.nicha.eventticketing.data.local.converter.TicketTypeListConverter
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto

@Entity(tableName = "events")
@TypeConverters(StringListConverter::class, TicketTypeListConverter::class)
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val organizerId: String?,
    val organizerName: String?,
    val startDate: String?,
    val endDate: String?,
    val location: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val category: String?,
    val categoryId: String?,
    val mainImageUrl: String?,
    val images: List<String>?, 
    val status: String?,
    val isFeatured: Boolean?,
    val ticketTypes: List<TicketTypeDto>?,
    val attendeeCount: Int?,
    val maxAttendees: Int?,
    val createdAt: String?,
    val updatedAt: String?,
    val minTicketPrice: Double?,
    val maxTicketPrice: Double?,
    val isFree: Boolean?
) 