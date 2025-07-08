package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.Event
import com.eventticketing.backend.entity.EventStatus
import com.eventticketing.backend.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface EventRepository : JpaRepository<Event, UUID> {
    fun findByOrganizer(organizer: User, pageable: Pageable): Page<Event>
    
    fun findByStatus(status: EventStatus, pageable: Pageable): Page<Event>
    
    fun findByStartDateAfterAndStatus(startDate: LocalDateTime, status: EventStatus, pageable: Pageable): Page<Event>
    
    @Query("""
        SELECT e FROM Event e 
        WHERE (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) 
        AND (:status IS NULL OR e.status = :status)
        AND (:startDate IS NULL OR e.startDate >= :startDate)
        AND (:endDate IS NULL OR e.endDate <= :endDate)
    """)
    fun searchEvents(
        @Param("keyword") keyword: String?,
        @Param("status") status: EventStatus?,
        @Param("startDate") startDate: LocalDateTime?,
        @Param("endDate") endDate: LocalDateTime?,
        pageable: Pageable
    ): Page<Event>
} 