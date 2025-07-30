package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.EventCost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*

@Repository
interface EventCostRepository : JpaRepository<EventCost, UUID> {
    
    fun findByEventId(eventId: UUID): List<EventCost>
    
    @Query("SELECT SUM(ec.amount) FROM EventCost ec WHERE ec.event.id = :eventId")
    fun getTotalCostByEventId(@Param("eventId") eventId: UUID): BigDecimal?
    
    @Query("SELECT ec.costType, SUM(ec.amount) FROM EventCost ec WHERE ec.event.id = :eventId GROUP BY ec.costType")
    fun getCostBredownByEventId(@Param("eventId") eventId: UUID): List<Array<Any>>
    
    @Query("SELECT COUNT(DISTINCT t.user.id) FROM Ticket t WHERE t.event.id = :eventId")
    fun getAttendeeCountByEventId(@Param("eventId") eventId: UUID): Long
}
