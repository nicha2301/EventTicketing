package com.eventticketing.backend.service

import com.eventticketing.backend.dto.report.ReportDto
import com.eventticketing.backend.dto.report.ReportRequest
import com.eventticketing.backend.dto.report.ReportSummaryDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.io.OutputStream
import java.time.LocalDate
import java.util.UUID

interface ReportService {
    // Generate reports
    fun generateRevenueReport(userId: UUID, request: ReportRequest): ReportDto
    fun generateSalesReport(userId: UUID, request: ReportRequest): ReportDto
    fun generateAttendanceReport(userId: UUID, request: ReportRequest): ReportDto
    
    // Get existing reports
    fun getReportById(reportId: Long): ReportDto
    fun getReportsByUser(userId: UUID, pageable: Pageable): Page<ReportSummaryDto>
    fun getReportsByEvent(eventId: UUID, pageable: Pageable): Page<ReportSummaryDto>
    fun getReportsByType(type: String, pageable: Pageable): Page<ReportSummaryDto>
    
    // Export reports to different formats
    fun exportReportToPdf(reportId: Long, outputStream: OutputStream)
    fun exportReportToExcel(reportId: Long, outputStream: OutputStream)
    
    // Dashboard data for organizers
    fun getDailyRevenue(eventId: UUID?, startDate: LocalDate, endDate: LocalDate): Map<String, Any>
    fun getTicketSalesByType(eventId: UUID): Map<String, Any>
    fun getCheckInStatistics(eventId: UUID): Map<String, Any>
} 