package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.Ticket
import com.eventticketing.backend.entity.TicketType
import com.eventticketing.backend.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TicketRepository : JpaRepository<Ticket, UUID> {
    fun findByUser(user: User, pageable: Pageable): Page<Ticket>
    
    fun findByTicketType(ticketType: TicketType): List<Ticket>
    
    fun findByQrCode(qrCode: String): Optional<Ticket>
    
    @Query("""
        SELECT t FROM Ticket t 
        JOIN t.ticketType tt 
        JOIN tt.event e 
        WHERE e.id = :eventId
    """)
    fun findByEventId(@Param("eventId") eventId: UUID, pageable: Pageable): Page<Ticket>
    
    @Query("""
        SELECT COUNT(t) FROM Ticket t 
        JOIN t.ticketType tt 
        JOIN tt.event e 
        WHERE e.id = :eventId AND t.checkedIn = true
    """)
    fun countCheckedInTicketsByEventId(@Param("eventId") eventId: UUID): Long
} 