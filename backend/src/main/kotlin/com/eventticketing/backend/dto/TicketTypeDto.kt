package com.eventticketing.backend.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class TicketTypeDto(
    val id: UUID?,
    val eventId: UUID,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val quantity: Int,
    val quantitySold: Int,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val saleStartDate: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val saleEndDate: LocalDateTime,
    val available: Int
)

data class TicketTypeCreateDto(
    @field:NotBlank(message = "Ticket name is required")
    val name: String,
    
    val description: String?,
    
    @field:NotNull(message = "Price is required")
    val price: BigDecimal,
    
    @field:NotNull(message = "Quantity is required")
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int,
    
    @field:NotNull(message = "Sale start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val saleStartDate: LocalDateTime,
    
    @field:NotNull(message = "Sale end date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val saleEndDate: LocalDateTime
)

data class TicketTypeUpdateDto(
    val name: String?,
    val description: String?,
    val price: BigDecimal?,
    val quantity: Int?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val saleStartDate: LocalDateTime?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val saleEndDate: LocalDateTime?
) 