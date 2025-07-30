package com.eventticketing.backend.dto.analytics

import java.math.BigDecimal

/**
 * DTO for Attendee Analytics
 */
data class AttendeeAnalyticsDto(
    val totalRegistered: Int,
    val totalCheckedIn: Int,
    val ageDistribution: Map<String, Int>,
    val genderDistribution: Map<String, Int>,
    val locationDistribution: Map<String, Int>,
    val registrationTimeline: Map<String, Int>
)

/**
 * DTO for Event Performance Analytics
 */
data class EventPerformanceDto(
    val ticketSalesRate: Int,
    val attendanceRate: Int,
    val averageRating: Double,
    val roi: Double,
    val totalRevenue: BigDecimal,
    val totalCost: BigDecimal?,
    val ticketsSold: Int,
    val revenueTarget: BigDecimal?,
    val ticketsTarget: Int?,
    val npsScore: Int?,
    val costPerAttendee: BigDecimal?,
    val profitMargin: Double
)

/**
 * DTO for Payment Methods Analytics
 */
data class PaymentMethodsDto(
    val paymentMethods: Map<String, PaymentMethodStats>,
    val totalTransactions: Int,
    val totalAmount: BigDecimal,
    val averageTransactionAmount: BigDecimal
)

data class PaymentMethodStats(
    val transactionCount: Int,
    val totalAmount: BigDecimal,
    val averageAmount: BigDecimal,
    val percentage: Double
)

/**
 * DTO for Marketing Analytics
 */
data class MarketingAnalyticsDto(
    val channels: List<MarketingChannelDto>,
    val totalReach: Int,
    val totalClicks: Int,
    val totalConversions: Int,
    val totalCost: BigDecimal,
    val averageROAS: Double
)

data class MarketingChannelDto(
    val channel: String,
    val reach: Int,
    val clicks: Int,
    val conversions: Int,
    val cost: BigDecimal,
    val roas: Double
)
