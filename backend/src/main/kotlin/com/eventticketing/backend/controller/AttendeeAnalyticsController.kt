package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.analytics.AttendeeAnalyticsDto
import com.eventticketing.backend.security.CurrentUser
import com.eventticketing.backend.security.UserPrincipal
import com.eventticketing.backend.service.AttendeeAnalyticsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/analytics/attendee")
class AttendeeAnalyticsController(
    private val attendeeAnalyticsService: AttendeeAnalyticsService
) {
    
    @GetMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getAttendeeAnalytics(
        @PathVariable eventId: UUID,
        @CurrentUser userPrincipal: UserPrincipal?
    ): ResponseEntity<AttendeeAnalyticsDto> {
        val analytics = attendeeAnalyticsService.getAttendeeAnalytics(eventId)
        return ResponseEntity(analytics, HttpStatus.OK)
    }
    
    @GetMapping("/{eventId}/demographics")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getDetailedDemographics(
        @PathVariable eventId: UUID,
        @CurrentUser userPrincipal: UserPrincipal?
    ): ResponseEntity<Map<String, Any>> {
        val ageDistribution = attendeeAnalyticsService.getAgeDistribution(eventId)
        val genderDistribution = attendeeAnalyticsService.getGenderDistribution(eventId)
        val locationDistribution = attendeeAnalyticsService.getLocationDistribution(eventId)
        
        val demographics = mapOf(
            "ageDistribution" to ageDistribution,
            "genderDistribution" to genderDistribution,
            "locationDistribution" to locationDistribution
        )
        
        return ResponseEntity(demographics, HttpStatus.OK)
    }
    
    @GetMapping("/{eventId}/timeline")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getRegistrationTimeline(
        @PathVariable eventId: UUID,
        @CurrentUser userPrincipal: UserPrincipal?
    ): ResponseEntity<Map<String, Int>> {
        val timeline = attendeeAnalyticsService.getRegistrationTimeline(eventId)
        return ResponseEntity(timeline, HttpStatus.OK)
    }
}
