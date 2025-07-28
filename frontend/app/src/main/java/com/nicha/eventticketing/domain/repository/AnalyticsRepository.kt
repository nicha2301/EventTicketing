package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.analytics.*
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface cho Analytics
 */
interface AnalyticsRepository {
    
    /**
     * Lấy dữ liệu doanh thu theo ngày
     * @param eventId ID sự kiện
     * @param startDate Ngày bắt đầu (format: yyyy-MM-dd)
     * @param endDate Ngày kết thúc (format: yyyy-MM-dd)
     * @return Flow<Resource<DailyRevenueResponseDto>>
     */
    fun getDailyRevenue(
        eventId: String? = null,
        startDate: String,
        endDate: String
    ): Flow<Resource<DailyRevenueResponseDto>>
    
    /**
     * Lấy thống kê bán vé theo loại
     * @param eventId ID sự kiện
     * @return Flow<Resource<TicketSalesResponseDto>>
     */
    fun getTicketSalesByType(
        eventId: String
    ): Flow<Resource<TicketSalesResponseDto>>
    
    /**
     * Lấy thống kê check-in
     * @param eventId ID sự kiện
     * @return Flow<Resource<CheckInStatisticsDto>>
     */
    fun getCheckInStatistics(
        eventId: String
    ): Flow<Resource<CheckInStatisticsDto>>
    
    /**
     * Lấy thống kê đánh giá
     * @param eventId ID sự kiện
     * @return Flow<Resource<RatingStatisticsDto>>
     */
    fun getRatingStatistics(
        eventId: String
    ): Flow<Resource<RatingStatisticsDto>>
    
    /**
     * Lấy tổng quan Analytics Dashboard
     * @param filter Bộ lọc analytics
     * @return Flow<Resource<AnalyticsDashboardDto>>
     */
    fun getAnalyticsSummary(
        filter: AnalyticsFilterDto
    ): Flow<Resource<AnalyticsDashboardDto>>
    
    /**
     * Export dữ liệu analytics ra PDF
     * @param eventId ID sự kiện
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Flow<Resource<ByteArray>>
     */
    fun exportAnalyticsToPdf(
        eventId: String? = null,
        startDate: String,
        endDate: String
    ): Flow<Resource<ByteArray>>
    
    /**
     * Export dữ liệu analytics ra Excel
     * @param eventId ID sự kiện
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Flow<Resource<ByteArray>>
     */
    fun exportAnalyticsToExcel(
        eventId: String? = null,
        startDate: String,
        endDate: String
    ): Flow<Resource<ByteArray>>
}
