package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.analytics.EventPerformanceDto
import com.eventticketing.backend.security.CurrentUser
import com.eventticketing.backend.security.UserPrincipal
import com.eventticketing.backend.service.EventPerformanceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/analytics/performance")
class EventPerformanceController(
    private val eventPerformanceService: EventPerformanceService
) {
    
    @GetMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getEventPerformance(
        @PathVariable eventId: UUID,
        @CurrentUser userPrincipal: UserPrincipal?
    ): ResponseEntity<EventPerformanceDto> {
        val performance = eventPerformanceService.getEventPerformance(eventId)
        return ResponseEntity(performance, HttpStatus.OK)
    }
    
    @GetMapping("/{eventId}/roi")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getROIAnalysis(
        @PathVariable eventId: UUID,
        @CurrentUser userPrincipal: UserPrincipal?
    ): ResponseEntity<Map<String, Any>> {
        val roi = eventPerformanceService.calculateROI(eventId)
        val profitMargin = eventPerformanceService.calculateProfitMargin(eventId)
        val costPerAttendee = eventPerformanceService.calculateCostPerAttendee(eventId)
        
        val roiAnalysis = mapOf(
            "roi" to roi,
            "profitMargin" to profitMargin,
            "costPerAttendee" to (costPerAttendee ?: 0.0)
        )
        
        return ResponseEntity(roiAnalysis, HttpStatus.OK)
    }
    
    @GetMapping("/{eventId}/kpi")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getKPIDashboard(
        @PathVariable eventId: UUID,
        @CurrentUser userPrincipal: UserPrincipal?
    ): ResponseEntity<Map<String, Any>> {
        val ticketSalesRate = eventPerformanceService.calculateTicketSalesRate(eventId)
        val attendanceRate = eventPerformanceService.calculateAttendanceRate(eventId)
        
        val kpiData = mapOf(
            "ticketSalesRate" to ticketSalesRate,
            "attendanceRate" to attendanceRate
        )
        
        return ResponseEntity(kpiData, HttpStatus.OK)
    }
}
