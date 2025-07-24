package com.nicha.eventticketing.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val ticketTypeId: String,
    val userId: String,
    val ticketNumber: String,
    val status: String,
    val purchaseDate: String?,
    val eventTitle: String?,
    val eventStartDate: String?,
    val eventEndDate: String?,
    val eventLocation: String?,
    val ticketTypeName: String?,
    val ticketTypePrice: Double?,
    val quantity: Int?,
    val totalPrice: Double?,
    val qrCode: String?
) 