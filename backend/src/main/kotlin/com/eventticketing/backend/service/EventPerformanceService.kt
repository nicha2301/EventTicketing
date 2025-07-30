package com.eventticketing.backend.service

import com.eventticketing.backend.dto.analytics.EventPerformanceDto
import java.math.BigDecimal
import java.util.*

interface EventPerformanceService {
    fun getEventPerformance(eventId: UUID): EventPerformanceDto
    fun calculateROI(eventId: UUID): Double
    fun calculateTicketSalesRate(eventId: UUID): Int
    fun calculateAttendanceRate(eventId: UUID): Int
    fun calculateCostPerAttendee(eventId: UUID): BigDecimal?
    fun calculateProfitMargin(eventId: UUID): Double
}
