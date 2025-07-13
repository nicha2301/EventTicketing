package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.service.AnalyticsService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('ADMIN')")
class AnalyticsController(private val analyticsService: AnalyticsService) {

    @GetMapping("/dashboard")
    fun getDashboardStats(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val stats = analyticsService.getDashboardStats(startDate, endDate)
        return ResponseEntity.ok(ApiResponse.success("Thống kê dashboard", stats))
    }

    @GetMapping("/revenue")
    fun getRevenueReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val report = analyticsService.getRevenueReport(startDate, endDate)
        return ResponseEntity.ok(ApiResponse.success("Báo cáo doanh thu", report))
    }

    @GetMapping("/events/popular")
    fun getPopularEventsReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<ApiResponse<List<Map<String, Any>>>> {
        val report = analyticsService.getPopularEventsReport(startDate, endDate, limit)
        return ResponseEntity.ok(ApiResponse.success("Báo cáo sự kiện phổ biến", report))
    }

    @GetMapping("/users/active")
    fun getActiveUsersReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val report = analyticsService.getActiveUsersReport(startDate, endDate)
        return ResponseEntity.ok(ApiResponse.success("Báo cáo người dùng hoạt động", report))
    }

    @GetMapping("/export/csv/{reportType}")
    fun exportReportToCsv(
        @PathVariable reportType: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<ByteArray> {
        val csvData = analyticsService.exportReportToCsv(reportType, startDate, endDate)
        
        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType("text/csv")
        headers.setContentDispositionFormData("attachment", "${reportType}_report.csv")
        
        return ResponseEntity(csvData, headers, HttpStatus.OK)
    }

    @GetMapping("/export/excel/{reportType}")
    fun exportReportToExcel(
        @PathVariable reportType: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<ByteArray> {
        val excelData = analyticsService.exportReportToExcel(reportType, startDate, endDate)
        
        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        headers.setContentDispositionFormData("attachment", "${reportType}_report.xlsx")
        
        return ResponseEntity(excelData, headers, HttpStatus.OK)
    }

    @GetMapping("/export/pdf/{reportType}")
    fun exportReportToPdf(
        @PathVariable reportType: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<ByteArray> {
        val pdfData = analyticsService.exportReportToPdf(reportType, startDate, endDate)
        
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.setContentDispositionFormData("attachment", "${reportType}_report.pdf")
        
        return ResponseEntity(pdfData, headers, HttpStatus.OK)
    }
} 