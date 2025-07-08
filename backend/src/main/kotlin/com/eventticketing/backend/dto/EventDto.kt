package com.eventticketing.backend.dto

import com.eventticketing.backend.entity.EventStatus
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class EventDto(
    val id: UUID?,
    val organizerId: UUID,
    val organizerName: String,
    val title: String,
    val description: String?,
    val location: String,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startDate: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val endDate: LocalDateTime,
    val imageUrl: String?,
    val status: EventStatus,
    val ticketTypes: List<TicketTypeDto> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class EventCreateDto(
    @field:NotBlank(message = "Title is required")
    @field:Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    val title: String,
    
    val description: String?,
    
    @field:NotBlank(message = "Location is required")
    val location: String,
    
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    
    @field:NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startDate: LocalDateTime,
    
    @field:NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val endDate: LocalDateTime,
    
    val imageUrl: String?,
    
    val status: EventStatus = EventStatus.DRAFT,
    
    val ticketTypes: List<TicketTypeCreateDto> = emptyList()
)

data class EventUpdateDto(
    val title: String?,
    val description: String?,
    val location: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startDate: LocalDateTime?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val endDate: LocalDateTime?,
    val imageUrl: String?,
    val status: EventStatus?
)

data class EventSearchRequestDto(
    val keyword: String? = null,
    val status: EventStatus? = null,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startDate: LocalDateTime? = null,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val endDate: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "startDate",
    val sortDirection: String = "asc"
) 