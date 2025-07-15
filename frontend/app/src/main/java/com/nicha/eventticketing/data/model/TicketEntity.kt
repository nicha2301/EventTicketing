package com.nicha.eventticketing.data.model

import java.util.Date

enum class TicketStatus {
    ACTIVE, USED, EXPIRED, CANCELLED
}

data class TicketEntity(
    val id: String,
    val ticketNumber: String,
    val eventId: String,
    val eventTitle: String,
    val eventImageUrl: String,
    val eventDate: String,
    val eventTime: String,
    val eventLocation: String,
    val ticketTypeId: String,
    val ticketType: String,
    val price: Double,
    val purchaseDate: String = "",
    val status: TicketStatus = TicketStatus.ACTIVE,
    val qrCode: String = "",
    val userId: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) 