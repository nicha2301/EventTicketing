package com.eventticketing.backend.dto.event

import com.eventticketing.backend.dto.user.UserSummaryDto
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EventSummaryDto(
    val id: Long,
    val title: String,
    val description: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val location: String?,
    val organizerId: UUID,
    val organizerName: String?,
    val coverImageUrl: String?,
    val ticketsSold: Int?,
    val ticketsAvailable: Int?
) 