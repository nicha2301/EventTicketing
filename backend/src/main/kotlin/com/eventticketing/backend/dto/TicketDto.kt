package com.eventticketing.backend.dto

import com.eventticketing.backend.entity.TicketStatus
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class TicketDto(
    val id: UUID?,
    val ticketTypeId: UUID,
    val eventId: UUID,
    val eventTitle: String,
    val ticketTypeName: String,
    val userId: UUID,
    val userName: String,
    val price: BigDecimal,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val purchaseDate: LocalDateTime,
    val status: TicketStatus,
    val qrCode: String,
    val checkedIn: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val checkedInAt: LocalDateTime?
)

data class TicketPurchaseDto(
    @field:NotNull(message = "Ticket type ID is required")
    val ticketTypeId: UUID,
    
    @field:NotNull(message = "Payment method is required")
    val paymentMethod: String
)

data class TicketCheckInDto(
    val qrCode: String
)

data class TicketCheckInResponseDto(
    val ticketId: UUID,
    val eventId: UUID,
    val eventTitle: String,
    val userName: String,
    val ticketTypeName: String,
    val status: TicketStatus,
    val checkedIn: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val checkedInAt: LocalDateTime?
) 