package com.eventticketing.backend.dto

import com.eventticketing.backend.entity.TicketStatus
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class TicketDto(
    val id: UUID? = null,
    
    val ticketNumber: String? = null,
    
    val userId: UUID,
    
    val userName: String,
    
    val eventId: UUID,
    
    val eventTitle: String,
    
    val ticketTypeId: UUID,
    
    val ticketTypeName: String,
    
    val price: BigDecimal,
    
    val status: TicketStatus,
    
    val qrCodeUrl: String? = null,
    
    val purchaseDate: LocalDateTime? = null,
    
    val checkedInAt: LocalDateTime? = null,
    
    val cancelledAt: LocalDateTime? = null,
    
    val paymentId: UUID? = null,
    
    val paymentStatus: String? = null,
    
    val eventStartDate: LocalDateTime,
    
    val eventEndDate: LocalDateTime,
    
    val eventLocation: String,
    
    val eventAddress: String,
    
    val eventImageUrl: String? = null
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

data class TicketCheckInRequestDto(
    val ticketId: UUID? = null,
    val ticketNumber: String? = null,
    @field:NotNull(message = "EventId không được để trống")
    val eventId: UUID,
    val userId: UUID? = null
) 