package com.nicha.eventticketing.domain.model

import java.util.Date

/**
 * Domain model cho Ticket, được sử dụng trong business logic
 */
data class Ticket(
    val id: String,
    val eventId: String,
    val eventTitle: String,
    val eventImageUrl: String?,
    val userId: String,
    val ticketCode: String,
    val ticketType: TicketType,
    val price: Double,
    val purchaseDate: Date,
    val isUsed: Boolean,
    val usedDate: Date?,
    val expiryDate: Date?,
    val status: TicketStatus
)

enum class TicketStatus {
    ACTIVE, USED, EXPIRED, CANCELLED, UNKNOWN;
    
    companion object {
        fun fromString(status: String): TicketStatus {
            return try {
                valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
} 