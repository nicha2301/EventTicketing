package com.eventticketing.backend.service

import com.eventticketing.backend.dto.analytics.AttendeeAnalyticsDto
import java.util.*

interface AttendeeAnalyticsService {
    fun getAttendeeAnalytics(eventId: UUID): AttendeeAnalyticsDto
    fun getAgeDistribution(eventId: UUID): Map<String, Int>
    fun getGenderDistribution(eventId: UUID): Map<String, Int>
    fun getLocationDistribution(eventId: UUID): Map<String, Int>
    fun getRegistrationTimeline(eventId: UUID): Map<String, Int>
}
