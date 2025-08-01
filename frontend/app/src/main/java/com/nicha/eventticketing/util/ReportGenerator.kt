package com.nicha.eventticketing.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.nicha.eventticketing.data.remote.dto.analytics.*
import com.opencsv.CSVWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        dateRange: String,
        exporterName: String,
        exporterEmail: String,
        organizerName: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
            val fileName = "Analytics_Report_${eventName}_$timestamp.csv"
            
            val file = createReportFileDirectlyInDownloads(fileName)
            
            file.outputStream().use { fos ->
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                
                fos.writer(Charsets.UTF_8).use { writer ->
                    CSVWriter(writer).use { csvWriter ->
                        csvWriter.writeNext(arrayOf("ANALYTICS REPORT"))
                        csvWriter.writeNext(arrayOf("================"))
                        csvWriter.writeNext(arrayOf(""))
                        
                        csvWriter.writeNext(arrayOf("Event Information"))
                        csvWriter.writeNext(arrayOf("Event Name", eventName))
                        csvWriter.writeNext(arrayOf("Organizer", organizerName))
                        csvWriter.writeNext(arrayOf("Report Period", dateRange))
                        csvWriter.writeNext(arrayOf(""))
                        
                        csvWriter.writeNext(arrayOf("Export Information"))
                        csvWriter.writeNext(arrayOf("Exported By", exporterName))
                        csvWriter.writeNext(arrayOf("Exporter Email", exporterEmail))
                        csvWriter.writeNext(arrayOf("Export Date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())))
                        csvWriter.writeNext(arrayOf("Export Source", "EventTicketing Mobile App"))
                        csvWriter.writeNext(arrayOf(""))
                        
                        csvWriter.writeNext(arrayOf("SUMMARY STATISTICS"))
                        csvWriter.writeNext(arrayOf("Metric", "Value", "Description"))
                        csvWriter.writeNext(arrayOf("Total Revenue", analytics.totalRevenue.toString(), "Total revenue generated from ticket sales"))
                        csvWriter.writeNext(arrayOf("Total Tickets Sold", analytics.totalTickets.toString(), "Number of tickets sold during the period"))
                        csvWriter.writeNext(arrayOf("Check-in Rate", "${analytics.checkInRate}%", "Percentage of ticket holders who checked in"))
                        csvWriter.writeNext(arrayOf("Average Rating", analytics.averageRating.toString(), "Average customer rating (1-5 stars)"))
                        csvWriter.writeNext(arrayOf(""))
                        
                        csvWriter.writeNext(arrayOf("DAILY REVENUE BREAKDOWN"))
                        csvWriter.writeNext(arrayOf("Date", "Revenue (USD)", "Notes"))
                        analytics.dailyRevenue.forEach { (date, amount) ->
                            csvWriter.writeNext(arrayOf(date, amount.toString(), "Daily revenue for $eventName"))
                        }
                        csvWriter.writeNext(arrayOf(""))
                        
                        csvWriter.writeNext(arrayOf("TICKET SALES BY TYPE"))
                        csvWriter.writeNext(arrayOf("Ticket Type", "Quantity Sold", "Event"))
                        analytics.ticketSales.forEach { (type, count) ->
                            csvWriter.writeNext(arrayOf(type, count.toString(), eventName))
                        }
                        csvWriter.writeNext(arrayOf(""))
                        
                        csvWriter.writeNext(arrayOf("CHECK-IN STATISTICS"))
                        csvWriter.writeNext(arrayOf("Metric", "Value", "Event"))
                        analytics.checkInStats.forEach { (key, value) ->
                            csvWriter.writeNext(arrayOf(key, value.toString(), eventName))
                        }
                        csvWriter.writeNext(arrayOf(""))
                        
                        csvWriter.writeNext(arrayOf("REPORT FOOTER"))
                        csvWriter.writeNext(arrayOf("Generated by", "EventTicketing Analytics System"))
                        csvWriter.writeNext(arrayOf("Data Accuracy", "This report contains accurate data as of export time"))
                        csvWriter.writeNext(arrayOf("Contact Support", "support@eventticketing.com"))
                        csvWriter.writeNext(arrayOf("Report Version", "1.0"))
                        
                        csvWriter.flush()
                        writer.flush()
                    }
                }
            }
            
            if (file.exists() && file.length() > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    copyToDownloadsFolder(file, fileName)
                }
                
                file
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun generateDetailedReport(
        analytics: AnalyticsSummaryResponseDto,
        ticketSales: TicketSalesResponseDto,
        eventName: String,
        dateRange: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
            val fileName = "Detailed_Analytics_${eventName}_$timestamp.txt"
            val file = createReportFileInAppDirectory(fileName)
            
            FileWriter(file).use { writer ->
                writer.append("═══════════════════════════════════════\n")
                writer.append("    DETAILED ANALYTICS REPORT\n")
                writer.append("═══════════════════════════════════════\n\n")
                
                writer.append("Event: $eventName\n")
                writer.append("Report Period: $dateRange\n")
                writer.append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")
                
                writer.append("📊 EXECUTIVE SUMMARY\n")
                writer.append("───────────────────\n")
                writer.append("• Total Revenue: \$${String.format("%.2f", analytics.totalRevenue)}\n")
                writer.append("• Tickets Sold: ${analytics.totalTickets}\n")
                writer.append("• Check-in Rate: ${String.format("%.1f", analytics.checkInRate)}%\n")
                writer.append("• Customer Rating: ${String.format("%.1f", analytics.averageRating)}/5.0\n\n")
                
                writer.append("💰 REVENUE ANALYSIS\n")
                writer.append("──────────────────\n")
                val totalRevenue = analytics.dailyRevenue.values.sum()
                val avgDailyRevenue = if (analytics.dailyRevenue.isNotEmpty()) totalRevenue / analytics.dailyRevenue.size else 0.0
                writer.append("• Total Revenue: \$${String.format("%.2f", totalRevenue)}\n")
                writer.append("• Average Daily Revenue: \$${String.format("%.2f", avgDailyRevenue)}\n")
                writer.append("• Peak Revenue Day: ${analytics.dailyRevenue.maxByOrNull { it.value }?.key ?: "N/A"}\n")
                writer.append("• Revenue per Ticket: \$${String.format("%.2f", if (analytics.totalTickets > 0) totalRevenue / analytics.totalTickets else 0.0)}\n\n")
                
                writer.append("🎫 TICKET SALES ANALYSIS\n")
                writer.append("───────────────────────\n")
                val totalTickets = ticketSales.totalSold
                writer.append("• Total Tickets Sold: $totalTickets\n")
                writer.append("• Ticket Type Breakdown:\n")
                ticketSales.ticketTypeData.forEach { (type, stats) ->
                    val percentage = if (totalTickets > 0) (stats.count.toDouble() / totalTickets * 100) else 0.0
                    writer.append("  - $type: ${stats.count} tickets (${String.format("%.1f", percentage)}%)\n")
                }
                writer.append("\n")
                
                writer.append("📈 PERFORMANCE METRICS\n")
                writer.append("─────────────────────\n")
                writer.append("• Check-in Rate: ${String.format("%.1f", analytics.checkInRate)}%\n")
                writer.append("• Customer Satisfaction: ${String.format("%.1f", analytics.averageRating)}/5.0\n")
                
                val performanceScore = calculatePerformanceScore(analytics)
                writer.append("• Overall Performance Score: ${String.format("%.1f", performanceScore)}/100\n")
                writer.append("• Performance Level: ${getPerformanceLevel(performanceScore)}\n\n")
                
                writer.append("💡 RECOMMENDATIONS\n")
                writer.append("─────────────────\n")
                writer.append(generateRecommendations(analytics, ticketSales))
                
                writer.append("\n═══════════════════════════════════════\n")
                writer.append("End of Report\n")
                writer.append("═══════════════════════════════════════\n")
            }
            
            file
        } catch (e: Exception) {
            null
        }
    }

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
                    "csv" -> "application/vnd.ms-excel" 
                    "txt" -> "text/plain"
                    else -> "application/octet-stream"
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Analytics report generated on ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(shareIntent, "Open/Share Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            
        } catch (e: Exception) {
        }
    }

    private fun createReportFileDirectlyInDownloads(fileName: String): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val tempFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            tempFile
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            file
        }
    }

    private fun createReportFileInAppDirectory(fileName: String): File {
        val reportsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), EXPORT_FOLDER)
        if (!reportsDir.exists()) {
            val created = reportsDir.mkdirs()
        }
        
        val file = File(reportsDir, fileName)
        return file
    }
    
    private fun copyToDownloadsFolder(sourceFile: File, fileName: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) // Direct to Downloads
                }
                
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                
                uri?.let { downloadUri ->
                    resolver.openOutputStream(downloadUri)?.use { outputStream ->
                        sourceFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetFile = File(downloadsDir, fileName)
                sourceFile.copyTo(targetFile, overwrite = true)
            }
        } catch (e: Exception) {
        }
    }

    private fun calculatePerformanceScore(analytics: AnalyticsSummaryResponseDto): Double {
        val revenueScore = minOf(analytics.totalRevenue / 10000 * 30, 30.0)
        val ticketScore = minOf(analytics.totalTickets.toDouble() / 100.0 * 25, 25.0)
        val checkInScore = analytics.checkInRate / 100 * 25
        val ratingScore = analytics.averageRating / 5 * 20
        
        return revenueScore + ticketScore + checkInScore + ratingScore
    }

    private fun getPerformanceLevel(score: Double): String {
        return when {
            score >= 80 -> "Excellent"
            score >= 60 -> "Good"
            score >= 40 -> "Average"
            score >= 20 -> "Below Average"
            else -> "Poor"
        }
    }

    private fun generateRecommendations(
        analytics: AnalyticsSummaryResponseDto,
        ticketSales: TicketSalesResponseDto
    ): String {
        val recommendations = mutableListOf<String>()
        
        if (analytics.totalRevenue < 5000) {
            recommendations.add("• Consider implementing promotional pricing strategies to boost revenue")
        }
        
        if (analytics.checkInRate < 80) {
            recommendations.add("• Improve check-in rate by sending reminder notifications to attendees")
            recommendations.add("• Consider implementing QR code check-in for faster processing")
        }
        
        if (analytics.averageRating < 4.0) {
            recommendations.add("• Focus on improving customer experience to increase ratings")
            recommendations.add("• Collect detailed feedback to identify areas for improvement")
        }
        
        val totalTickets = ticketSales.totalSold
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
