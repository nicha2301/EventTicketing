package com.eventticketing.backend.service.impl

import com.eventticketing.backend.messaging.dto.AnalyticsMessageDto
import com.eventticketing.backend.service.AnalyticsService
import com.eventticketing.backend.service.MessageQueueService
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter

@Service
class AnalyticsServiceImpl(
    private val messageQueueService: MessageQueueService,
    private val jdbcTemplate: JdbcTemplate
) : AnalyticsService {

    private val logger = LoggerFactory.getLogger(AnalyticsServiceImpl::class.java)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    override fun trackUserEvent(userId: UUID, eventType: String, data: Map<String, Any>) {
        val analyticsMessage = AnalyticsMessageDto.fromUserEvent(
            eventType = eventType,
            userId = userId,
            data = data
        )
        messageQueueService.trackEvent(analyticsMessage)
    }

    override fun trackSystemEvent(eventType: String, data: Map<String, Any>) {
        val analyticsMessage = AnalyticsMessageDto.fromSystemEvent(
            eventType = eventType,
            data = data
        )
        messageQueueService.trackEvent(analyticsMessage)
    }

    override fun trackEventActivity(eventId: UUID, userId: UUID?, eventType: String, data: Map<String, Any>) {
        val analyticsMessage = AnalyticsMessageDto.fromEventActivity(
            eventType = eventType,
            userId = userId,
            eventId = eventId,
            data = data
        )
        messageQueueService.trackEvent(analyticsMessage)
    }

    override fun getDashboardStats(startDate: LocalDateTime, endDate: LocalDateTime): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        // Tổng số sự kiện
        val totalEvents = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM events WHERE created_at BETWEEN ? AND ?",
            Int::class.java,
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        ) ?: 0
        result["totalEvents"] = totalEvents
        
        // Tổng số vé đã bán
        val totalTickets = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM tickets WHERE created_at BETWEEN ? AND ? AND status = 'PAID'",
            Int::class.java,
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        ) ?: 0
        result["totalTickets"] = totalTickets
        
        // Tổng doanh thu
        val totalRevenue = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(price), 0) FROM tickets WHERE created_at BETWEEN ? AND ? AND status = 'PAID'",
            Double::class.java,
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        ) ?: 0.0
        result["totalRevenue"] = totalRevenue
        
        // Tổng số người dùng mới
        val newUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE created_at BETWEEN ? AND ?",
            Int::class.java,
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        ) ?: 0
        result["newUsers"] = newUsers
        
        return result
    }

    override fun getRevenueReport(startDate: LocalDateTime, endDate: LocalDateTime): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        // Doanh thu theo ngày
        val revenueByDay = jdbcTemplate.queryForList(
            """
            SELECT 
                DATE(created_at) as date,
                COUNT(*) as tickets_count,
                SUM(price) as revenue
            FROM tickets 
            WHERE created_at BETWEEN ? AND ? 
            AND status = 'PAID'
            GROUP BY DATE(created_at)
            ORDER BY date
            """,
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )
        result["revenueByDay"] = revenueByDay
        
        // Doanh thu theo danh mục
        val revenueByCategory = jdbcTemplate.queryForList(
            """
            SELECT 
                c.name as category,
                COUNT(t.id) as tickets_count,
                SUM(t.price) as revenue
            FROM tickets t
            JOIN events e ON t.event_id = e.id
            JOIN categories c ON e.category_id = c.id
            WHERE t.created_at BETWEEN ? AND ?
            AND t.status = 'PAID'
            GROUP BY c.name
            ORDER BY revenue DESC
            """,
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )
        result["revenueByCategory"] = revenueByCategory
        
        // Doanh thu theo phương thức thanh toán
        val revenueByPaymentMethod = jdbcTemplate.queryForList(
            """
            SELECT 
                p.payment_method,
                COUNT(t.id) as tickets_count,
                SUM(t.price) as revenue
            FROM tickets t
            JOIN payments p ON t.payment_id = p.id
            WHERE t.created_at BETWEEN ? AND ?
            AND t.status = 'PAID'
            GROUP BY p.payment_method
            ORDER BY revenue DESC
            """,
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )
        result["revenueByPaymentMethod"] = revenueByPaymentMethod
        
        return result
    }

    override fun getPopularEventsReport(startDate: LocalDateTime, endDate: LocalDateTime, limit: Int): List<Map<String, Any>> {
        return jdbcTemplate.queryForList(
            """
            SELECT 
                e.id as event_id,
                e.title as event_name,
                c.name as category,
                COUNT(t.id) as tickets_count,
                SUM(t.price) as revenue,
                e.start_date,
                e.end_date,
                e.max_attendees,
                e.current_attendees,
                COALESCE(e.average_rating, 0) as rating
            FROM events e
            JOIN tickets t ON e.id = t.event_id
            JOIN categories c ON e.category_id = c.id
            WHERE t.created_at BETWEEN ? AND ?
            AND t.status = 'PAID'
            GROUP BY e.id, e.title, c.name, e.start_date, e.end_date, e.max_attendees, e.current_attendees, e.average_rating
            ORDER BY tickets_count DESC, revenue DESC
            LIMIT ?
            """,
            startDate.format(dateFormatter),
            endDate.format(dateFormatter),
            limit
        )
    }

    override fun getActiveUsersReport(startDate: LocalDateTime, endDate: LocalDateTime): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        // Người dùng hoạt động nhất (mua nhiều vé nhất)
        val mostActiveUsers = jdbcTemplate.queryForList(
            """
            SELECT 
                u.id as user_id,
                u.full_name,
                u.email,
                COUNT(t.id) as tickets_count,
                SUM(t.price) as total_spent
            FROM users u
            JOIN tickets t ON u.id = t.user_id
            WHERE t.created_at BETWEEN ? AND ?
            AND t.status = 'PAID'
            GROUP BY u.id, u.full_name, u.email
            ORDER BY tickets_count DESC, total_spent DESC
            LIMIT 10
            """,
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )
        result["mostActiveUsers"] = mostActiveUsers
        
        // Người tổ chức hoạt động nhất (tạo nhiều sự kiện nhất)
        val mostActiveOrganizers = jdbcTemplate.queryForList(
            """
            SELECT 
                u.id as organizer_id,
                u.full_name,
                u.email,
                COUNT(e.id) as events_count,
                SUM(e.current_attendees) as total_attendees
            FROM users u
            JOIN events e ON u.id = e.organizer_id
            WHERE e.created_at BETWEEN ? AND ?
            GROUP BY u.id, u.full_name, u.email
            ORDER BY events_count DESC, total_attendees DESC
            LIMIT 10
            """,
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )
        result["mostActiveOrganizers"] = mostActiveOrganizers
        
        return result
    }

    override fun exportReportToCsv(reportType: String, startDate: LocalDateTime, endDate: LocalDateTime): ByteArray {
        val data = getReportData(reportType, startDate, endDate)
        val output = ByteArrayOutputStream()
        
        // Viết header
        val headers = data.firstOrNull()?.keys ?: return ByteArray(0)
        output.write(headers.joinToString(",").toByteArray())
        output.write("\n".toByteArray())
        
        // Viết dữ liệu
        data.forEach { row ->
            val values = headers.map { header -> row[header]?.toString() ?: "" }
            output.write(values.joinToString(",").toByteArray())
            output.write("\n".toByteArray())
        }
        
        return output.toByteArray()
    }

    override fun exportReportToExcel(reportType: String, startDate: LocalDateTime, endDate: LocalDateTime): ByteArray {
        val data = getReportData(reportType, startDate, endDate)
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet(reportType)
        
        // Tạo header row
        val headers = data.firstOrNull()?.keys ?: return ByteArray(0)
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }
        
        // Tạo data rows
        data.forEachIndexed { rowIndex, rowData ->
            val row = sheet.createRow(rowIndex + 1)
            headers.forEachIndexed { colIndex, header ->
                val cell = row.createCell(colIndex)
                when (val value = rowData[header]) {
                    is Number -> cell.setCellValue(value.toDouble())
                    is Date -> cell.setCellValue(value.toString())
                    is LocalDateTime -> cell.setCellValue(value.format(dateFormatter))
                    else -> cell.setCellValue(value?.toString() ?: "")
                }
            }
        }
        
        // Auto size columns
        headers.indices.forEach { sheet.autoSizeColumn(it) }
        
        // Ghi workbook vào ByteArrayOutputStream
        val output = ByteArrayOutputStream()
        workbook.write(output)
        workbook.close()
        
        return output.toByteArray()
    }

    override fun exportReportToPdf(reportType: String, startDate: LocalDateTime, endDate: LocalDateTime): ByteArray {
        val data = getReportData(reportType, startDate, endDate)
        val output = ByteArrayOutputStream()
        
        // Tạo document
        val document = Document()
        PdfWriter.getInstance(document, output)
        document.open()
        
        // Thêm tiêu đề
        val titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
        val title = Paragraph("$reportType Report (${startDate.format(DateTimeFormatter.ISO_DATE)} - ${endDate.format(DateTimeFormatter.ISO_DATE)})", titleFont)
        title.alignment = Element.ALIGN_CENTER
        title.spacingAfter = 20f
        document.add(title)
        
        // Tạo bảng
        val headers = data.firstOrNull()?.keys?.toList() ?: return ByteArray(0)
        val table = PdfPTable(headers.size)
        table.widthPercentage = 100f
        
        // Thêm header cells
        headers.forEach { header ->
            val cell = PdfPCell(Phrase(header))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(5f)
            table.addCell(cell)
        }
        
        // Thêm data rows
        data.forEach { rowData ->
            headers.forEach { header ->
                val value = rowData[header]?.toString() ?: ""
                table.addCell(value)
            }
        }
        
        document.add(table)
        document.close()
        
        return output.toByteArray()
    }
    
    /**
     * Lấy dữ liệu báo cáo dựa trên loại báo cáo
     */
    private fun getReportData(reportType: String, startDate: LocalDateTime, endDate: LocalDateTime): List<Map<String, Any>> {
        return when (reportType) {
            "revenue" -> {
                val revenueReport = getRevenueReport(startDate, endDate)
                revenueReport["revenueByDay"] as List<Map<String, Any>>
            }
            "popular_events" -> getPopularEventsReport(startDate, endDate, 20)
            "active_users" -> {
                val usersReport = getActiveUsersReport(startDate, endDate)
                usersReport["mostActiveUsers"] as List<Map<String, Any>>
            }
            "organizers" -> {
                val usersReport = getActiveUsersReport(startDate, endDate)
                usersReport["mostActiveOrganizers"] as List<Map<String, Any>>
            }
            else -> emptyList()
        }
    }
} 