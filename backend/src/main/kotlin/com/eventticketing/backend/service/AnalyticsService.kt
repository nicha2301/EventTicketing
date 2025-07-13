package com.eventticketing.backend.service

import com.eventticketing.backend.dto.AnalyticsMessageDto
import java.time.LocalDateTime
import java.util.*

/**
 * Service interface cho analytics
 */
interface AnalyticsService {
    /**
     * Theo dõi sự kiện người dùng
     */
    fun trackUserEvent(userId: UUID, eventType: String, data: Map<String, Any>)
    
    /**
     * Theo dõi sự kiện hệ thống
     */
    fun trackSystemEvent(eventType: String, data: Map<String, Any>)
    
    /**
     * Theo dõi sự kiện liên quan đến event
     */
    fun trackEventActivity(eventId: UUID, userId: UUID?, eventType: String, data: Map<String, Any>)
    
    /**
     * Lấy số liệu thống kê cho dashboard
     */
    fun getDashboardStats(startDate: LocalDateTime, endDate: LocalDateTime): Map<String, Any>
    
    /**
     * Lấy báo cáo doanh thu
     */
    fun getRevenueReport(startDate: LocalDateTime, endDate: LocalDateTime): Map<String, Any>
    
    /**
     * Lấy báo cáo sự kiện phổ biến
     */
    fun getPopularEventsReport(startDate: LocalDateTime, endDate: LocalDateTime, limit: Int): List<Map<String, Any>>
    
    /**
     * Lấy báo cáo người dùng hoạt động
     */
    fun getActiveUsersReport(startDate: LocalDateTime, endDate: LocalDateTime): Map<String, Any>
    
    /**
     * Xuất báo cáo dưới dạng CSV
     */
    fun exportReportToCsv(reportType: String, startDate: LocalDateTime, endDate: LocalDateTime): ByteArray
    
    /**
     * Xuất báo cáo dưới dạng Excel
     */
    fun exportReportToExcel(reportType: String, startDate: LocalDateTime, endDate: LocalDateTime): ByteArray
    
    /**
     * Xuất báo cáo dưới dạng PDF
     */
    fun exportReportToPdf(reportType: String, startDate: LocalDateTime, endDate: LocalDateTime): ByteArray
} 