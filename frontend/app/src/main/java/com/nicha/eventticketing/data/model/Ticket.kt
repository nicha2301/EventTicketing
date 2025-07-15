package com.nicha.eventticketing.data.model

import java.time.LocalDateTime

data class Ticket(
    val id: String,
    val eventId: String,
    val eventName: String,
    val eventImageUrl: String,
    val eventType: EventType,
    val eventDateTime: LocalDateTime,
    val location: String,
    val ticketType: TicketType,
    val price: Double,
    val purchaseDate: LocalDateTime,
    val qrCodeData: String,
    val isUsed: Boolean = false
) 