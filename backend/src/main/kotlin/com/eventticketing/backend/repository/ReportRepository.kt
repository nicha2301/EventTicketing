package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.Report
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ReportRepository : JpaRepository<Report, Long> {
    fun findByGeneratedByIdOrderByDateGeneratedDesc(userId: UUID, pageable: Pageable): Page<Report>
    
    fun findByEventIdOrderByDateGeneratedDesc(eventId: UUID, pageable: Pageable): Page<Report>
    
    fun findByTypeOrderByDateGeneratedDesc(type: String, pageable: Pageable): Page<Report>
    
    fun findByTypeAndEventIdOrderByDateGeneratedDesc(type: String, eventId: UUID, pageable: Pageable): Page<Report>
} 