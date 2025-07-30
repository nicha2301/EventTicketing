package com.nicha.eventticketing.data.remote.dto.analytics

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RatingStatisticsDto(
    @Json(name = "eventId")
    val eventId: String,
    
    @Json(name = "averageRating")
    val averageRating: Double,
    
    @Json(name = "totalRatings")
    val totalRatings: Long,
    
    @Json(name = "ratingCounts")
    val ratingCounts: Map<String, Long>
)

@JsonClass(generateAdapter = true)
data class DailyRevenueDto(
    @Json(name = "date")
    val date: String,
    
    @Json(name = "revenue")
    val revenue: Double,
    
    @Json(name = "ticketsSold")
    val ticketsSold: Int
)

@JsonClass(generateAdapter = true)
data class TicketSalesDto(
    @Json(name = "ticketTypeName")
    val ticketTypeName: String,
    
    @Json(name = "sold")
    val sold: Int,
    
    @Json(name = "available")
    val available: Int,
    
    @Json(name = "revenue")
    val revenue: Double
)

@JsonClass(generateAdapter = true)
data class CheckInStatisticsDto(
    @Json(name = "totalTickets")
    val totalTickets: Int,
    
    @Json(name = "checkedIn")
    val checkedIn: Int,
    
    @Json(name = "notCheckedIn")
    val notCheckedIn: Int,
    
    @Json(name = "checkInRate")
    val checkInRate: Double,
    
    @Json(name = "ticketTypeBreakdown")
    val ticketTypeBreakdown: Map<String, CheckInBreakdownDto>? = null
)

@JsonClass(generateAdapter = true)
data class CheckInBreakdownDto(
    @Json(name = "total")
    val total: Int,
    
    @Json(name = "checkedIn")
    val checkedIn: Int,
    
    @Json(name = "notCheckedIn")
    val notCheckedIn: Int,
    
    @Json(name = "checkInRate")
    val checkInRate: Double
)

/**
 * DTO cho tổng quan Analytics Dashboard
 */
@JsonClass(generateAdapter = true)
data class AnalyticsDashboardDto(
    @Json(name = "totalRevenue")
    val totalRevenue: Double,
    
    @Json(name = "totalTicketsSold")
    val totalTicketsSold: Int,
    
    @Json(name = "totalCheckIns")
    val totalCheckIns: Int,
    
    @Json(name = "averageRating")
    val averageRating: Double,
    
    @Json(name = "checkInRate")
    val checkInRate: Double,
    
    @Json(name = "revenueGrowth")
    val revenueGrowth: Double? = null,
    
    @Json(name = "ticketSalesGrowth")
    val ticketSalesGrowth: Double? = null,
    
    @Json(name = "period")
    val period: AnalyticsTimeRangeDto
)

/**
 * DTO cho khoảng thời gian Analytics
 */
@JsonClass(generateAdapter = true)
data class AnalyticsTimeRangeDto(
    @Json(name = "startDate")
    val startDate: String,
    
    @Json(name = "endDate")
    val endDate: String,
    
    @Json(name = "period")
    val period: String 
)

/**
 * DTO cho bộ lọc Analytics
 */
@JsonClass(generateAdapter = true)
data class AnalyticsFilterDto(
    @Json(name = "eventIds")
    val eventIds: List<String>? = null,
    
    @Json(name = "timeRange")
    val timeRange: AnalyticsTimeRangeDto,
    
    @Json(name = "ticketTypes")
    val ticketTypes: List<String>? = null,
    
    @Json(name = "revenueRange")
    val revenueRange: DoubleRange? = null
)

/**
 * Custom class for revenue range
 */
@JsonClass(generateAdapter = true)
data class DoubleRange(
    @Json(name = "min")
    val min: Double,
    
    @Json(name = "max")
    val max: Double
)

/**
 * DTO cho dữ liệu doanh thu theo ngày
 */
@JsonClass(generateAdapter = true)
data class DailyRevenueResponseDto(
    @Json(name = "dailyRevenue")
    val dailyRevenue: Map<String, Double>,
    
    @Json(name = "totalRevenue")
    val totalRevenue: Double,
    
    @Json(name = "currencyCode")
    val currencyCode: String,
    
    @Json(name = "startDate")
    val startDate: String,
    
    @Json(name = "endDate")
    val endDate: String
)

/**
 * DTO cho dữ liệu bán vé theo loại 
 */
@JsonClass(generateAdapter = true)
data class TicketSalesResponseDto(
    @Json(name = "ticketTypeData")
    val ticketTypeData: Map<String, TicketTypeStatsDto>,
    
    @Json(name = "totalSold")
    val totalSold: Int,
    
    @Json(name = "totalRevenue")
    val totalRevenue: Double,
    
    @Json(name = "dailySales")
    val dailySales: Map<String, Int>? = null
)

@JsonClass(generateAdapter = true)
data class TicketTypeStatsDto(
    @Json(name = "count")
    val count: Int,
    
    @Json(name = "revenue")
    val revenue: Double
)

/**
 * DTO cho phân tích người tham dự
 */
@JsonClass(generateAdapter = true)
data class AttendeeAnalyticsResponseDto(
    @Json(name = "totalRegistered")
    val totalRegistered: Int,
    
    @Json(name = "totalCheckedIn")
    val totalCheckedIn: Int,
    
    @Json(name = "ageDistribution")
    val ageDistribution: Map<String, Int>,
    
    @Json(name = "genderDistribution")
    val genderDistribution: Map<String, Int>,
    
    @Json(name = "locationDistribution")
    val locationDistribution: Map<String, Int>,
    
    @Json(name = "registrationTimeline")
    val registrationTimeline: Map<String, Int>?
)

/**
 * DTO cho hiệu suất sự kiện
 */
@JsonClass(generateAdapter = true)
data class EventPerformanceResponseDto(
    @Json(name = "ticketSalesRate")
    val ticketSalesRate: Int,
    
    @Json(name = "attendanceRate")
    val attendanceRate: Int,
    
    @Json(name = "averageRating")
    val averageRating: Double,
    
    @Json(name = "roi")
    val roi: Int,
    
    @Json(name = "totalRevenue")
    val totalRevenue: Double,
    
    @Json(name = "totalCost")
    val totalCost: Double?,
    
    @Json(name = "ticketsSold")
    val ticketsSold: Int,
    
    @Json(name = "revenueTarget")
    val revenueTarget: Double?,
    
    @Json(name = "ticketsTarget")
    val ticketsTarget: Int?,
    
    @Json(name = "npsScore")
    val npsScore: Int?,
    
    @Json(name = "costPerAttendee")
    val costPerAttendee: Double?
)

/**
 * Enhanced TicketSalesResponseDto for detailed ticket analytics
 */
@JsonClass(generateAdapter = true)
data class DetailedTicketSalesResponseDto(
    @Json(name = "ticketTypeData")
    val ticketTypeData: Map<String, TicketTypeStatsDto>,
    
    @Json(name = "totalSold")
    val totalSold: Int,
    
    @Json(name = "totalRevenue")
    val totalRevenue: Double,
    
    @Json(name = "dailySales")
    val dailySales: Map<String, Int>?,
    
    @Json(name = "peakSellingDays")
    val peakSellingDays: Map<String, Int>?
)

/**
 * DTO for Analytics Summary used in Export functionality
 */
@JsonClass(generateAdapter = true)
data class AnalyticsSummaryResponseDto(
    @Json(name = "totalRevenue")
    val totalRevenue: Double,
    
    @Json(name = "totalTickets")
    val totalTickets: Int,
    
    @Json(name = "checkInRate")
    val checkInRate: Double,
    
    @Json(name = "averageRating")
    val averageRating: Double,
    
    @Json(name = "dailyRevenue")
    val dailyRevenue: Map<String, Double>,
    
    @Json(name = "ticketSales")
    val ticketSales: Map<String, Int>,
    
    @Json(name = "checkInStats")
    val checkInStats: Map<String, Any>
)
