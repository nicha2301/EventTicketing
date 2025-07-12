package com.eventticketing.backend.dto.report

import com.eventticketing.backend.dto.event.EventSummaryDto
import com.eventticketing.backend.dto.user.UserSummaryDto
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ReportDto(
    val id: Long?,
    val name: String,
    val type: String,
    val description: String?,
    val dateGenerated: LocalDateTime,
    val parameters: Map<String, Any>?,
    val resultData: Any?,
    val filePath: String?,
    val generatedBy: UserSummaryDto,
    val event: EventSummaryDto?
)

data class ReportRequest(
    val name: String,
    val type: String, // REVENUE, ATTENDANCE, SALES
    val description: String?,
    val parameters: Map<String, Any>?,
    val eventId: UUID?
)

data class ReportSummaryDto(
    val id: Long?,
    val name: String,
    val type: String,
    val dateGenerated: LocalDateTime,
    val filePath: String?
) 