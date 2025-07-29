package com.nicha.eventticketing.util.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.nicha.eventticketing.data.remote.dto.analytics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for generating and exporting analytics reports
 */
@Singleton
class ReportGenerator @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val EXPORT_FOLDER = "EventTicketing_Reports"
        private const val DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss"
    }

    /**
     * Generate CSV report for analytics data
     */
    suspend fun generateCsvReport(
        analytics: AnalyticsSummaryResponseDto,
        eventName: String,
        dateRange: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
            val fileName = "Analytics_Report_${eventName}_$timestamp.csv"
            val file = createReportFile(fileName)
            
            FileWriter(file).use { writer ->
                // Header
                writer.append("Analytics Report - $eventName\n")
                writer.append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                writer.append("Period: $dateRange\n\n")
                
                // Summary Section
                writer.append("SUMMARY STATISTICS\n")
                writer.append("Metric,Value\n")
                writer.append("Total Revenue,${analytics.totalRevenue}\n")
                writer.append("Total Tickets Sold,${analytics.totalTickets}\n")
                writer.append("Check-in Rate,${analytics.checkInRate}%\n")
                writer.append("Average Rating,${analytics.averageRating}\n\n")
                
                // Revenue Breakdown
                writer.append("DAILY REVENUE\n")
                writer.append("Date,Revenue\n")
                analytics.dailyRevenue.forEach { (date, amount) ->
                    writer.append("$date,$amount\n")
                }
                writer.append("\n")
                
                // Ticket Sales by Type
                writer.append("TICKET SALES BY TYPE\n")
                writer.append("Ticket Type,Quantity Sold\n")
                analytics.ticketSales.forEach { (type, count) ->
                    writer.append("$type,$count\n")
                }
                writer.append("\n")
                
                // Check-in Statistics
                writer.append("CHECK-IN STATISTICS\n")
                writer.append("Metric,Value\n")
                analytics.checkInStats.forEach { (key, value) ->
                    writer.append("$key,$value\n")
                }
            }
            
            Timber.d("CSV report generated: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate CSV report")
            null
        }
    }

    /**
     * Generate detailed analytics report in text format
     */
    suspend fun generateDetailedReport(
        analytics: AnalyticsSummaryResponseDto,
        ticketSales: TicketSalesResponseDto,
        eventName: String,
        dateRange: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
            val fileName = "Detailed_Analytics_${eventName}_$timestamp.txt"
            val file = createReportFile(fileName)
            
            FileWriter(file).use { writer ->
                writer.append("═══════════════════════════════════════\n")
                writer.append("    DETAILED ANALYTICS REPORT\n")
                writer.append("═══════════════════════════════════════\n\n")
                
                writer.append("Event: $eventName\n")
                writer.append("Report Period: $dateRange\n")
                writer.append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")
                
                // Executive Summary
                writer.append("📊 EXECUTIVE SUMMARY\n")
                writer.append("───────────────────\n")
                writer.append("• Total Revenue: \$${String.format("%.2f", analytics.totalRevenue)}\n")
                writer.append("• Tickets Sold: ${analytics.totalTickets}\n")
                writer.append("• Check-in Rate: ${String.format("%.1f", analytics.checkInRate)}%\n")
                writer.append("• Customer Rating: ${String.format("%.1f", analytics.averageRating)}/5.0\n\n")
                
                // Revenue Analysis
                writer.append("💰 REVENUE ANALYSIS\n")
                writer.append("──────────────────\n")
                val totalRevenue = analytics.dailyRevenue.values.sum()
                val avgDailyRevenue = if (analytics.dailyRevenue.isNotEmpty()) totalRevenue / analytics.dailyRevenue.size else 0.0
                writer.append("• Total Revenue: \$${String.format("%.2f", totalRevenue)}\n")
                writer.append("• Average Daily Revenue: \$${String.format("%.2f", avgDailyRevenue)}\n")
                writer.append("• Peak Revenue Day: ${analytics.dailyRevenue.maxByOrNull { it.value }?.key ?: "N/A"}\n")
                writer.append("• Revenue per Ticket: \$${String.format("%.2f", if (analytics.totalTickets > 0) totalRevenue / analytics.totalTickets else 0.0)}\n\n")
                
                // Ticket Sales Analysis
                writer.append("🎫 TICKET SALES ANALYSIS\n")
                writer.append("───────────────────────\n")
                val totalTickets = ticketSales.ticketTypeBreakdown.values.sum()
                writer.append("• Total Tickets Sold: $totalTickets\n")
                writer.append("• Ticket Type Breakdown:\n")
                ticketSales.ticketTypeBreakdown.forEach { (type, count) ->
                    val percentage = if (totalTickets > 0) (count.toDouble() / totalTickets * 100) else 0.0
                    writer.append("  - $type: $count tickets (${String.format("%.1f", percentage)}%)\n")
                }
                writer.append("\n")
                
                // Performance Metrics
                writer.append("📈 PERFORMANCE METRICS\n")
                writer.append("─────────────────────\n")
                writer.append("• Check-in Rate: ${String.format("%.1f", analytics.checkInRate)}%\n")
                writer.append("• Customer Satisfaction: ${String.format("%.1f", analytics.averageRating)}/5.0\n")
                
                val performanceScore = calculatePerformanceScore(analytics)
                writer.append("• Overall Performance Score: ${String.format("%.1f", performanceScore)}/100\n")
                writer.append("• Performance Level: ${getPerformanceLevel(performanceScore)}\n\n")
                
                // Recommendations
                writer.append("💡 RECOMMENDATIONS\n")
                writer.append("─────────────────\n")
                writer.append(generateRecommendations(analytics, ticketSales))
                
                writer.append("\n═══════════════════════════════════════\n")
                writer.append("End of Report\n")
                writer.append("═══════════════════════════════════════\n")
            }
            
            Timber.d("Detailed report generated: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate detailed report")
            null
        }
    }

    /**
     * Share report file via system share intent
     */
    fun shareReport(file: File, title: String = "Analytics Report") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = when (file.extension.lowercase()) {
                    "csv" -> "text/csv"
                    "txt" -> "text/plain"
                    else -> "application/octet-stream"
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(shareIntent, "Share Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to share report")
        }
    }

    /**
     * Create report file in app's external files directory
     */
    private fun createReportFile(fileName: String): File {
        val reportsDir = File(context.getExternalFilesDir(null), EXPORT_FOLDER)
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        return File(reportsDir, fileName)
    }

    /**
     * Calculate overall performance score
     */
    private fun calculatePerformanceScore(analytics: AnalyticsSummaryResponseDto): Double {
        val revenueScore = minOf(analytics.totalRevenue / 10000 * 30, 30.0) // Max 30 points
        val ticketScore = minOf(analytics.totalTickets.toDouble() / 100.0 * 25, 25.0) // Max 25 points
        val checkInScore = analytics.checkInRate / 100 * 25 // Max 25 points
        val ratingScore = analytics.averageRating / 5 * 20 // Max 20 points
        
        return revenueScore + ticketScore + checkInScore + ratingScore
    }

    /**
     * Get performance level based on score
     */
    private fun getPerformanceLevel(score: Double): String {
        return when {
            score >= 80 -> "Excellent"
            score >= 60 -> "Good"
            score >= 40 -> "Average"
            score >= 20 -> "Below Average"
            else -> "Poor"
        }
    }

    /**
     * Generate personalized recommendations
     */
    private fun generateRecommendations(
        analytics: AnalyticsSummaryResponseDto,
        ticketSales: TicketSalesResponseDto
    ): String {
        val recommendations = mutableListOf<String>()
        
        // Revenue recommendations
        if (analytics.totalRevenue < 5000) {
            recommendations.add("• Consider implementing promotional pricing strategies to boost revenue")
        }
        
        // Check-in recommendations
        if (analytics.checkInRate < 80) {
            recommendations.add("• Improve check-in rate by sending reminder notifications to attendees")
            recommendations.add("• Consider implementing QR code check-in for faster processing")
        }
        
        // Rating recommendations
        if (analytics.averageRating < 4.0) {
            recommendations.add("• Focus on improving customer experience to increase ratings")
            recommendations.add("• Collect detailed feedback to identify areas for improvement")
        }
        
        // Ticket sales recommendations
        val totalTickets = ticketSales.ticketTypeBreakdown.values.sum()
        if (totalTickets < 50) {
            recommendations.add("• Increase marketing efforts to boost ticket sales")
            recommendations.add("• Consider early bird discounts to encourage advance purchases")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("• Your event is performing well! Continue current strategies")
            recommendations.add("• Consider expanding to similar events or increasing capacity")
        }
        
        return recommendations.joinToString("\n")
    }
}
