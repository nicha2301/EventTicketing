package com.nicha.eventticketing.domain.mapper

import com.nicha.eventticketing.data.local.entity.TicketEntity
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.domain.model.Ticket
import com.nicha.eventticketing.domain.model.TicketStatus
import com.nicha.eventticketing.domain.model.TicketType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper để chuyển đổi giữa TicketDto và Ticket domain model
 */
@Singleton
class TicketMapper @Inject constructor() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    fun mapToDomainModel(dto: TicketDto): Ticket {
        return Ticket(
            id = dto.id,
            eventId = dto.eventId,
            eventTitle = dto.eventTitle,
            eventImageUrl = dto.eventImageUrl,
            userId = dto.userId,
            ticketCode = dto.ticketNumber,
            ticketType = TicketType.fromString(dto.ticketTypeName),
            price = dto.price,
            purchaseDate = if (dto.purchaseDate != null) parseDate(dto.purchaseDate) else Date(),
            isUsed = dto.status.equals("USED", ignoreCase = true),
            usedDate = if (dto.status.equals("USED", ignoreCase = true)) {
                if (dto.purchaseDate != null) parseDate(dto.purchaseDate) else Date()
            } else null,
            expiryDate = parseDate(dto.eventEndDate),
            status = TicketStatus.fromString(dto.status)
        )
    }

    fun mapToEntity(dto: TicketDto): TicketEntity {
        val purchasedDate = if (dto.purchaseDate != null) parseDate(dto.purchaseDate) else Date()
        return TicketEntity(
            id = dto.id,
            ticketTypeId = dto.ticketTypeId,
            userId = dto.userId,
            eventId = dto.eventId,
            orderCode = dto.ticketNumber,
            qrCode = dto.ticketNumber,
            price = dto.price,
            status = dto.status,
            checkedIn = dto.status.equals("USED", ignoreCase = true),
            checkedInAt = if (dto.status.equals("USED", ignoreCase = true)) purchasedDate else null,
            purchasedAt = purchasedDate,
            createdAt = purchasedDate, 
            updatedAt = purchasedDate
        )
    }

    fun mapToDomainModel(entity: TicketEntity): Ticket {
        return Ticket(
            id = entity.id,
            eventId = entity.eventId,
            eventTitle = "", 
            eventImageUrl = "", 
            userId = entity.userId,
            ticketCode = entity.qrCode ?: "",
            ticketType = TicketType.STANDARD, 
            price = entity.price,
            purchaseDate = entity.purchasedAt,
            isUsed = entity.checkedIn,
            usedDate = entity.checkedInAt,
            expiryDate = null, 
            status = TicketStatus.fromString(entity.status)
        )
    }

    private fun parseDate(dateString: String?): Date {
        return try {
            if (dateString == null) return Date()
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
} 