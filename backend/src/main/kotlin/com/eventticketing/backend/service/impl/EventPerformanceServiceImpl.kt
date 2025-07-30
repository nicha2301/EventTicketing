package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.analytics.EventPerformanceDto
import com.eventticketing.backend.repository.*
import com.eventticketing.backend.service.EventPerformanceService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

@Service
class EventPerformanceServiceImpl(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository,
    private val paymentRepository: PaymentRepository,
    private val eventCostRepository: EventCostRepository,
    private val ratingRepository: RatingRepository
) : EventPerformanceService {
    
    @PersistenceContext
    private lateinit var entityManager: EntityManager
    
    override fun getEventPerformance(eventId: UUID): EventPerformanceDto {
        val event = eventRepository.findById(eventId)
            .orElseThrow { RuntimeException("Event not found") }
        
        val ticketSalesRate = calculateTicketSalesRate(eventId)
        val attendanceRate = calculateAttendanceRate(eventId)
        val totalRevenue = calculateTotalRevenue(eventId)
        val totalCost = eventCostRepository.getTotalCostByEventId(eventId)
        val averageRating = calculateAverageRating(eventId)
        val roi = calculateROI(eventId)
        val ticketsSold = ticketRepository.countSoldTicketsByEventId(eventId).toInt()
        val costPerAttendee = calculateCostPerAttendee(eventId)
        val profitMargin = calculateProfitMargin(eventId)
        val npsScore = calculateNPSScore(eventId)
        
        return EventPerformanceDto(
            ticketSalesRate = ticketSalesRate,
            attendanceRate = attendanceRate,
            averageRating = averageRating,
            roi = roi,
            totalRevenue = totalRevenue,
            totalCost = totalCost,
            ticketsSold = ticketsSold,
            revenueTarget = event.revenueTarget,
            ticketsTarget = event.ticketCapacity,
            npsScore = npsScore,
            costPerAttendee = costPerAttendee,
            profitMargin = profitMargin
        )
    }
    
    override fun calculateROI(eventId: UUID): Double {
        val totalRevenue = calculateTotalRevenue(eventId)
        val totalCost = eventCostRepository.getTotalCostByEventId(eventId) ?: BigDecimal.ZERO
        
        if (totalCost == BigDecimal.ZERO) {
            return 0.0
        }
        
        val roi = ((totalRevenue - totalCost).divide(totalCost, 4, RoundingMode.HALF_UP))
            .multiply(BigDecimal(100))
        
        return roi.toDouble()
    }
    
    override fun calculateTicketSalesRate(eventId: UUID): Int {
        val event = eventRepository.findById(eventId)
            .orElseThrow { RuntimeException("Event not found") }
        
        val ticketsSold = ticketRepository.countSoldTicketsByEventId(eventId)
        val totalCapacity = event.ticketCapacity ?: return 0
        
        return ((ticketsSold.toDouble() / totalCapacity) * 100).toInt()
    }
    
    override fun calculateAttendanceRate(eventId: UUID): Int {
        val totalTickets = ticketRepository.countSoldTicketsByEventId(eventId)
        val checkedInTickets = ticketRepository.countCheckedInTicketsByEventId(eventId)
        
        if (totalTickets == 0L) return 0
        
        return ((checkedInTickets.toDouble() / totalTickets) * 100).toInt()
    }
    
    override fun calculateCostPerAttendee(eventId: UUID): BigDecimal? {
        val totalCost = eventCostRepository.getTotalCostByEventId(eventId) ?: return null
        val attendeeCount = eventCostRepository.getAttendeeCountByEventId(eventId)
        
        if (attendeeCount == 0L) return null
        
        return totalCost.divide(BigDecimal(attendeeCount), 2, RoundingMode.HALF_UP)
    }
    
    override fun calculateProfitMargin(eventId: UUID): Double {
        val totalRevenue = calculateTotalRevenue(eventId)
        val totalCost = eventCostRepository.getTotalCostByEventId(eventId) ?: BigDecimal.ZERO
        
        if (totalRevenue == BigDecimal.ZERO) return 0.0
        
        val profit = totalRevenue - totalCost
        val profitMargin = profit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
        
        return profitMargin.toDouble()
    }
    
    private fun calculateTotalRevenue(eventId: UUID): BigDecimal {
        val query = """
            SELECT COALESCE(SUM(p.amount), 0)
            FROM payments p
            JOIN tickets t ON p.ticket_id = t.id
            WHERE t.event_id = :eventId AND p.status = 'COMPLETED'
        """.trimIndent()
        
        val result = entityManager.createNativeQuery(query)
            .setParameter("eventId", eventId)
            .singleResult as BigDecimal
        
        return result
    }
    
    private fun calculateAverageRating(eventId: UUID): Double {
        return ratingRepository.calculateAverageRatingByEventId(eventId) ?: 0.0
    }
    
    private fun calculateNPSScore(eventId: UUID): Int {
        val totalRatings = ratingRepository.countByEventIdAndStatusApproved(eventId)
        if (totalRatings == 0L) return 0
        
        val promoters = (1..5).sumOf { score ->
            if (score >= 4) ratingRepository.countByEventIdAndScoreAndStatusApproved(eventId, score) else 0L
        }
        val detractors = (1..5).sumOf { score ->
            if (score <= 2) ratingRepository.countByEventIdAndScoreAndStatusApproved(eventId, score) else 0L
        }
        
        val promoterPercentage = (promoters.toDouble() / totalRatings) * 100
        val detractorPercentage = (detractors.toDouble() / totalRatings) * 100
        
        return (promoterPercentage - detractorPercentage).toInt()
    }
}
