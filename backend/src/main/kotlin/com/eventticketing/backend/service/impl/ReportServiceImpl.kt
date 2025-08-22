package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.event.EventSummaryDto
import com.eventticketing.backend.dto.report.ReportDto
import com.eventticketing.backend.dto.report.ReportRequest
import com.eventticketing.backend.dto.report.ReportSummaryDto
import com.eventticketing.backend.dto.user.UserSummaryDto
import com.eventticketing.backend.entity.Report
import com.eventticketing.backend.entity.TicketStatus
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.repository.*
import com.eventticketing.backend.service.ReportService
import com.fasterxml.jackson.databind.ObjectMapper
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.math.RoundingMode

@Service
class ReportServiceImpl(
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val paymentRepository: PaymentRepository,
    private val ticketRepository: TicketRepository,
    private val objectMapper: ObjectMapper
) : ReportService {

    private val logger = LoggerFactory.getLogger(ReportServiceImpl::class.java)

    @Transactional
    override fun generateRevenueReport(userId: UUID, request: ReportRequest): ReportDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        val event = request.eventId?.let { eventId ->
            eventRepository.findById(eventId)
                .orElseThrow { ResourceNotFoundException("Event not found with id: $eventId") }
        }

        // Parse parameters
        val startDate = request.parameters?.get("startDate")?.toString()?.let {
            LocalDate.parse(it)
        } ?: LocalDate.now().minusMonths(1)

        val endDate = request.parameters?.get("endDate")?.toString()?.let {
            LocalDate.parse(it)
        } ?: LocalDate.now()

        // Generate revenue data
        val revenueData = if (event != null) {
            // Revenue data for specific event
            paymentRepository.findSuccessfulPaymentsByEventIdAndDateRange(
                event.id!!,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
            )
        } else {
            // Revenue data for all events (for admin)
            paymentRepository.findSuccessfulPaymentsByDateRange(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
            )
        }

        // Transform to report format
        val dailyRevenue = revenueData.groupBy { 
            it.createdAt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        }.mapValues { entry ->
            entry.value.sumOf { it.amount }
        }

        val totalRevenue = revenueData.sumOf { it.amount }
        val avgTicketPrice = if (revenueData.isNotEmpty()) {
            totalRevenue.divide(revenueData.size.toBigDecimal(), 2, RoundingMode.HALF_UP)
        } else {
            java.math.BigDecimal.ZERO
        }

        val resultData = mapOf(
            "totalRevenue" to totalRevenue,
            "averageTicketPrice" to avgTicketPrice,
            "dailyRevenue" to dailyRevenue,
            "currencyCode" to "VND",
            "startDate" to startDate,
            "endDate" to endDate
        )

        // Create report entity
        val report = Report(
            name = request.name,
            type = "REVENUE",
            description = request.description,
            parameters = objectMapper.writeValueAsString(request.parameters),
            resultData = objectMapper.writeValueAsString(resultData),
            generatedBy = user,
            event = event,
            filePath = null
        )

        val savedReport = reportRepository.save(report)
        
        return mapToReportDto(savedReport)
    }

    @Transactional
    override fun generateSalesReport(userId: UUID, request: ReportRequest): ReportDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        val event = request.eventId?.let { eventId ->
            eventRepository.findById(eventId)
                .orElseThrow { ResourceNotFoundException("Event not found with id: $eventId") }
        }

        // Parse parameters
        val startDate = request.parameters?.get("startDate")?.toString()?.let {
            LocalDate.parse(it)
        } ?: LocalDate.now().minusMonths(1)

        val endDate = request.parameters?.get("endDate")?.toString()?.let {
            LocalDate.parse(it)
        } ?: LocalDate.now()

        // Generate sales data
        val tickets = if (event != null) {
            ticketRepository.findByEventIdAndCreatedAtBetween(
                event.id!!,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
            )
        } else {
            ticketRepository.findByCreatedAtBetween(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
            )
        }

        // Transform to report format
        val dailySales = tickets.groupBy { 
            it.createdAt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        }.mapValues { entry ->
            entry.value.size
        }

        val ticketTypeData = tickets.groupBy { it.ticketType.name }
            .mapValues { entry -> entry.value.size }

        val resultData = mapOf(
            "totalSales" to tickets.size,
            "dailySales" to dailySales,
            "ticketTypeBreakdown" to ticketTypeData,
            "startDate" to startDate,
            "endDate" to endDate
        )

        // Create report entity
        val report = Report(
            name = request.name,
            type = "SALES",
            description = request.description,
            parameters = objectMapper.writeValueAsString(request.parameters),
            resultData = objectMapper.writeValueAsString(resultData),
            generatedBy = user,
            event = event,
            filePath = null
        )

        val savedReport = reportRepository.save(report)
        
        return mapToReportDto(savedReport)
    }

    @Transactional
    override fun generateAttendanceReport(userId: UUID, request: ReportRequest): ReportDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        val eventId = request.eventId
            ?: throw IllegalArgumentException("Event ID is required for attendance reports")

        val event = eventRepository.findById(eventId)
            .orElseThrow { ResourceNotFoundException("Event not found with id: $eventId") }

        // Get all tickets for the event
        val tickets = ticketRepository.findByEventId(event.id!!)
        
        val checkedInCount = tickets.count { it.status == TicketStatus.CHECKED_IN }
        val notCheckedInCount = tickets.count { it.status != TicketStatus.CHECKED_IN }
        val checkInRate = if (tickets.isNotEmpty()) {
            (checkedInCount.toDouble() / tickets.size) * 100
        } else {
            0.0
        }

        // Group by ticket types
        val ticketTypeAttendance = tickets.groupBy { it.ticketType.name }
            .mapValues { entry ->
                val typedTickets = entry.value
                val typedCheckedIn = typedTickets.count { it.status == TicketStatus.CHECKED_IN }
                mapOf(
                    "total" to typedTickets.size,
                    "checkedIn" to typedCheckedIn,
                    "notCheckedIn" to (typedTickets.size - typedCheckedIn),
                    "checkInRate" to if (typedTickets.isNotEmpty()) 
                                     (typedCheckedIn.toDouble() / typedTickets.size) * 100 
                                   else 0.0
                )
            }

        val resultData = mapOf(
            "totalTickets" to tickets.size,
            "checkedIn" to checkedInCount,
            "notCheckedIn" to notCheckedInCount,
            "checkInRate" to checkInRate,
            "ticketTypeBreakdown" to ticketTypeAttendance
        )

        // Create report entity
        val report = Report(
            name = request.name,
            type = "ATTENDANCE",
            description = request.description,
            parameters = objectMapper.writeValueAsString(request.parameters),
            resultData = objectMapper.writeValueAsString(resultData),
            generatedBy = user,
            event = event,
            filePath = null
        )

        val savedReport = reportRepository.save(report)
        
        return mapToReportDto(savedReport)
    }

    override fun getReportById(reportId: Long): ReportDto {
        val report = reportRepository.findById(reportId)
            .orElseThrow { ResourceNotFoundException("Report not found with id: $reportId") }
            
        return mapToReportDto(report)
    }

    override fun getReportsByUser(userId: UUID, pageable: Pageable): Page<ReportSummaryDto> {
        return reportRepository.findByGeneratedByIdOrderByDateGeneratedDesc(userId, pageable)
            .map { mapToReportSummaryDto(it) }
    }

    override fun getReportsByEvent(eventId: UUID, pageable: Pageable): Page<ReportSummaryDto> {
        return reportRepository.findByEventIdOrderByDateGeneratedDesc(eventId, pageable)
            .map { mapToReportSummaryDto(it) }
    }

    override fun getReportsByType(type: String, pageable: Pageable): Page<ReportSummaryDto> {
        return reportRepository.findByTypeOrderByDateGeneratedDesc(type, pageable)
            .map { mapToReportSummaryDto(it) }
    }

    override fun exportReportToPdf(reportId: Long, outputStream: OutputStream) {
        val report = reportRepository.findById(reportId)
            .orElseThrow { ResourceNotFoundException("Report not found with id: $reportId") }
            
        try {
            val writer = PdfWriter(outputStream)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            
            document.add(Paragraph("Report: ${report.name}"))
            document.add(Paragraph("Generated: ${report.dateGenerated}"))
            document.add(Paragraph("Type: ${report.type}"))
            
            if (report.description != null) {
                document.add(Paragraph("Description: ${report.description}"))
            }
            
            // Add report data
            val resultData = objectMapper.readValue(report.resultData, Map::class.java)
            resultData.forEach { (key, value) ->
                when (value) {
                    is Map<*, *> -> {
                        document.add(Paragraph(key.toString()))
                        val table = Table(2)
                        value.forEach { (k, v) ->
                            table.addCell(k.toString())
                            table.addCell(v.toString())
                        }
                        document.add(table)
                    }
                    else -> document.add(Paragraph("$key: $value"))
                }
            }
            
            document.close()
        } catch (e: Exception) {
            logger.error("Error generating PDF report", e)
            throw RuntimeException("Failed to generate PDF report", e)
        }
    }

    override fun exportReportToExcel(reportId: Long, outputStream: OutputStream) {
        val report = reportRepository.findById(reportId)
            .orElseThrow { ResourceNotFoundException("Report not found with id: $reportId") }
            
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Report")
            
            // Create header style
            val headerStyle = workbook.createCellStyle()
            headerStyle.fillForegroundColor = IndexedColors.LIGHT_BLUE.index
            headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
            
            // Add report metadata
            var rowIndex = 0
            var row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue("Report Name")
            row.createCell(1).setCellValue(report.name)
            
            row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue("Generated Date")
            row.createCell(1).setCellValue(report.dateGenerated.toString())
            
            row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue("Type")
            row.createCell(1).setCellValue(report.type)
            
            if (report.description != null) {
                row = sheet.createRow(rowIndex++)
                row.createCell(0).setCellValue("Description")
                row.createCell(1).setCellValue(report.description)
            }
            
            // Add an empty row as separator
            rowIndex++
            
            // Add report data
            val resultData = objectMapper.readValue(report.resultData, Map::class.java)
            resultData.forEach { (key, value) ->
                when (value) {
                    is Map<*, *> -> {
                        row = sheet.createRow(rowIndex++)
                        val headerCell = row.createCell(0)
                        headerCell.setCellValue(key.toString())
                        headerCell.cellStyle = headerStyle
                        
                        // Create table headers
                        row = sheet.createRow(rowIndex++)
                        row.createCell(0).setCellValue("Key")
                        row.createCell(1).setCellValue("Value")
                        
                        // Add data rows
                        value.forEach { (k, v) ->
                            row = sheet.createRow(rowIndex++)
                            row.createCell(0).setCellValue(k.toString())
                            row.createCell(1).setCellValue(v.toString())
                        }
                        
                        // Add empty row after table
                        rowIndex++
                    }
                    else -> {
                        row = sheet.createRow(rowIndex++)
                        row.createCell(0).setCellValue(key.toString())
                        row.createCell(1).setCellValue(value.toString())
                    }
                }
            }
            
            // Auto-size columns
            for (i in 0..1) {
                sheet.autoSizeColumn(i)
            }
            
            workbook.write(outputStream)
            workbook.close()
        } catch (e: Exception) {
            logger.error("Error generating Excel report", e)
            throw RuntimeException("Failed to generate Excel report", e)
        }
    }

    override fun getDailyRevenue(eventId: UUID?, startDate: LocalDate, endDate: LocalDate): Map<String, Any> {
        val payments = if (eventId != null) {
            paymentRepository.findSuccessfulPaymentsByEventIdAndDateRange(
                eventId,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
            )
        } else {
            paymentRepository.findSuccessfulPaymentsByDateRange(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
            )
        }
        
        val dailyRevenue = payments.groupBy { 
            it.createdAt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        }.mapValues { entry ->
            entry.value.sumOf { it.amount }
        }
        
        val totalRevenue = payments.sumOf { it.amount }
        
        return mapOf(
            "dailyRevenue" to dailyRevenue,
            "totalRevenue" to totalRevenue,
            "currencyCode" to "VND",
            "startDate" to startDate,
            "endDate" to endDate
        )
    }

    override fun getTicketSalesByType(eventId: UUID): Map<String, Any> {
        val tickets = ticketRepository.findByEventId(eventId)
        
        val ticketTypeData = tickets.groupBy { it.ticketType.name }
            .mapValues { entry -> 
                mapOf(
                    "count" to entry.value.size,
                    "revenue" to entry.value.sumOf { 
                        it.price ?: it.ticketType.price
                    }
                )
            }
            
        val totalSold = tickets.size
        val totalRevenue = tickets.sumOf { it.price ?: it.ticketType.price }
        
        return mapOf(
            "ticketTypeData" to ticketTypeData,
            "totalSold" to totalSold,
            "totalRevenue" to totalRevenue
        )
    }

    override fun getCheckInStatistics(eventId: UUID): Map<String, Any> {
        val tickets = ticketRepository.findByEventId(eventId)
        
        val checkedInCount = tickets.count { it.status == TicketStatus.CHECKED_IN }
        val notCheckedInCount = tickets.count { it.status != TicketStatus.CHECKED_IN }
        val checkInRate = if (tickets.isNotEmpty()) {
            (checkedInCount.toDouble() / tickets.size) * 100
        } else {
            0.0
        }
        
        // Group by ticket types
        val ticketTypeStats = tickets.groupBy { it.ticketType.name }
            .mapValues { entry ->
                val typedTickets = entry.value
                val typedCheckedIn = typedTickets.count { it.status == TicketStatus.CHECKED_IN }
                mapOf(
                    "total" to typedTickets.size,
                    "checkedIn" to typedCheckedIn,
                    "notCheckedIn" to (typedTickets.size - typedCheckedIn),
                    "checkInRate" to if (typedTickets.isNotEmpty()) 
                                     (typedCheckedIn.toDouble() / typedTickets.size) * 100 
                                   else 0.0
                )
            }
        
        return mapOf(
            "totalTickets" to tickets.size,
            "checkedIn" to checkedInCount,
            "notCheckedIn" to notCheckedInCount,
            "checkInRate" to checkInRate,
            "ticketTypeBreakdown" to ticketTypeStats
        )
    }

    // Helper methods to map entities to DTOs
    private fun mapToReportDto(report: Report): ReportDto {
        val resultData: Any = try {
            objectMapper.readValue(report.resultData, Map::class.java)
        } catch (e: Exception) {
            report.resultData ?: ""
        }
        
        val parameters: Map<String, Any>? = if (report.parameters != null) {
            try {
                @Suppress("UNCHECKED_CAST")
                objectMapper.readValue(report.parameters, Map::class.java) as Map<String, Any>?
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
        
        return ReportDto(
            id = report.id,
            name = report.name,
            type = report.type,
            description = report.description,
            dateGenerated = report.dateGenerated,
            parameters = parameters,
            resultData = resultData,
            filePath = report.filePath,
            generatedBy = UserSummaryDto(
                id = report.generatedBy.id!!,
                fullName = report.generatedBy.fullName,
                email = report.generatedBy.email,
                role = report.generatedBy.role,
                profileImageUrl = null
            ),
            event = report.event?.let { event ->
                EventSummaryDto(
                    id = event.id!!.hashCode().toLong(), // Chuyển đổi UUID sang Long
                    title = event.title,
                    description = event.description,
                    startDate = event.startDate,
                    endDate = event.endDate,
                    location = event.location.name,
                    organizerId = event.organizer.id!!,
                    organizerName = event.organizer.fullName,
                    coverImageUrl = event.featuredImageUrl,
                    ticketsSold = event.currentAttendees,
                    ticketsAvailable = event.maxAttendees
                )
            }
        )
    }
    
    private fun mapToReportSummaryDto(report: Report): ReportSummaryDto {
        return ReportSummaryDto(
            id = report.id,
            name = report.name,
            type = report.type,
            dateGenerated = report.dateGenerated,
            filePath = report.filePath
        )
    }
} 