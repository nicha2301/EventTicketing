package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.analytics.AttendeeAnalyticsDto
import com.eventticketing.backend.repository.TicketRepository
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.service.AttendeeAnalyticsService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

@Service
class AttendeeAnalyticsServiceImpl(
    private val ticketRepository: TicketRepository,
    private val userRepository: UserRepository
) : AttendeeAnalyticsService {
    
    @PersistenceContext
    private lateinit var entityManager: EntityManager
    
    override fun getAttendeeAnalytics(eventId: UUID): AttendeeAnalyticsDto {
        val totalRegistered = getTotalRegistered(eventId)
        val totalCheckedIn = getTotalCheckedIn(eventId)
        val ageDistribution = getAgeDistribution(eventId)
        val genderDistribution = getGenderDistribution(eventId)
        val locationDistribution = getLocationDistribution(eventId)
        val registrationTimeline = getRegistrationTimeline(eventId)
        
        return AttendeeAnalyticsDto(
            totalRegistered = totalRegistered,
            totalCheckedIn = totalCheckedIn,
            ageDistribution = ageDistribution,
            genderDistribution = genderDistribution,
            locationDistribution = locationDistribution,
            registrationTimeline = registrationTimeline
        )
    }
    
    override fun getAgeDistribution(eventId: UUID): Map<String, Int> {
        val results = userRepository.getAgeDistributionForEvent(eventId)
        return results.associate { result ->
            val ageGroup = result[0] as String
            val count = (result[1] as Number).toInt()
            ageGroup to count
        }
    }
    
    override fun getGenderDistribution(eventId: UUID): Map<String, Int> {
        val results = userRepository.getGenderDistributionForEvent(eventId)
        return results.associate { result ->
            val gender = result[0] as String? ?: "Không xác định"
            val count = (result[1] as Number).toInt()
            gender to count
        }
    }
    
    override fun getLocationDistribution(eventId: UUID): Map<String, Int> {
        val results = userRepository.getLocationDistributionForEvent(eventId)
        return results.associate { result ->
            val location = result[0] as String? ?: "Không xác định"
            val count = (result[1] as Number).toInt()
            location to count
        }
    }
    
    override fun getRegistrationTimeline(eventId: UUID): Map<String, Int> {
        val query = """
            SELECT 
                DATE(t.created_at) as registration_date,
                COUNT(*) as count
            FROM tickets t
            WHERE t.event_id = :eventId
            GROUP BY DATE(t.created_at)
            ORDER BY registration_date DESC
            LIMIT 30
        """.trimIndent()
        
        val results = entityManager.createNativeQuery(query)
            .setParameter("eventId", eventId)
            .resultList as List<Array<Any>>
        
        return results.associate { 
            (it[0] as java.sql.Date).toString() to (it[1] as Number).toInt() 
        }
    }
    
    private fun getTotalRegistered(eventId: UUID): Int {
        return ticketRepository.countSoldTicketsByEventId(eventId).toInt()
    }
    
    private fun getTotalCheckedIn(eventId: UUID): Int {
        return ticketRepository.countCheckedInTicketsByEventId(eventId).toInt()
    }
}
