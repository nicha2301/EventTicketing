package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.Event
import com.eventticketing.backend.entity.TicketType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface TicketTypeRepository : JpaRepository<TicketType, UUID> {
    fun findByEvent(event: Event): List<TicketType>
    
    fun findByEventAndSaleEndDateAfter(event: Event, currentDate: LocalDateTime): List<TicketType>
    
    fun countByEvent(event: Event): Long
} 