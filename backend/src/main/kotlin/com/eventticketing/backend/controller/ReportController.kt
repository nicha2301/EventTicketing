package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.report.ReportDto
import com.eventticketing.backend.dto.report.ReportRequest
import com.eventticketing.backend.dto.report.ReportSummaryDto
import com.eventticketing.backend.security.CurrentUser
import com.eventticketing.backend.service.ReportService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/reports")
class ReportController(private val reportService: ReportService) {

    @PostMapping("/revenue")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun generateRevenueReport(
        @CurrentUser userId: UUID,
        @RequestBody request: ReportRequest
    ): ResponseEntity<ReportDto> {
        val report = reportService.generateRevenueReport(userId, request)
        return ResponseEntity(report, HttpStatus.CREATED)
    }

    @PostMapping("/sales")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun generateSalesReport(
        @CurrentUser userId: UUID,
        @RequestBody request: ReportRequest
    ): ResponseEntity<ReportDto> {
        val report = reportService.generateSalesReport(userId, request)
        return ResponseEntity(report, HttpStatus.CREATED)
    }

    @PostMapping("/attendance")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun generateAttendanceReport(
        @CurrentUser userId: UUID,
        @RequestBody request: ReportRequest
    ): ResponseEntity<ReportDto> {
        val report = reportService.generateAttendanceReport(userId, request)
        return ResponseEntity(report, HttpStatus.CREATED)
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getReportById(@PathVariable reportId: Long): ResponseEntity<ReportDto> {
        val report = reportService.getReportById(reportId)
        return ResponseEntity(report, HttpStatus.OK)
    }

    @GetMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getReportsByCurrentUser(
        @CurrentUser userId: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<ReportSummaryDto>> {
        val reports = reportService.getReportsByUser(userId, pageable)
        return ResponseEntity(reports, HttpStatus.OK)
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getReportsByEvent(
        @PathVariable eventId: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<ReportSummaryDto>> {
        val reports = reportService.getReportsByEvent(eventId, pageable)
        return ResponseEntity(reports, HttpStatus.OK)
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getReportsByType(
        @PathVariable type: String,
        pageable: Pageable
    ): ResponseEntity<Page<ReportSummaryDto>> {
        val reports = reportService.getReportsByType(type, pageable)
        return ResponseEntity(reports, HttpStatus.OK)
    }

    @GetMapping("/{reportId}/export/pdf")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun exportReportToPdf(
        @PathVariable reportId: Long,
        response: HttpServletResponse
    ) {
        response.contentType = "application/pdf"
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-$reportId.pdf")
        
        reportService.exportReportToPdf(reportId, response.outputStream)
        response.outputStream.flush()
    }

    @GetMapping("/{reportId}/export/excel")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun exportReportToExcel(
        @PathVariable reportId: Long,
        response: HttpServletResponse
    ) {
        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-$reportId.xlsx")
        
        reportService.exportReportToExcel(reportId, response.outputStream)
        response.outputStream.flush()
    }

    @GetMapping("/dashboard/daily-revenue")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getDailyRevenue(
        @RequestParam(required = false) eventId: UUID?,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<Map<String, Any>> {
        val data = reportService.getDailyRevenue(eventId, startDate, endDate)
        return ResponseEntity(data, HttpStatus.OK)
    }

    @GetMapping("/dashboard/ticket-sales/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getTicketSalesByType(
        @PathVariable eventId: UUID
    ): ResponseEntity<Map<String, Any>> {
        val data = reportService.getTicketSalesByType(eventId)
        return ResponseEntity(data, HttpStatus.OK)
    }

    @GetMapping("/dashboard/check-in-statistics/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getCheckInStatistics(
        @PathVariable eventId: UUID
    ): ResponseEntity<Map<String, Any>> {
        val data = reportService.getCheckInStatistics(eventId)
        return ResponseEntity(data, HttpStatus.OK)
    }
}
 