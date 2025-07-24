package com.nicha.eventticketing.domain.mapper

import com.nicha.eventticketing.data.local.entity.TicketEntity
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto

object TicketMapper {
    fun dtoToEntity(dto: TicketDto): TicketEntity = TicketEntity(
        id = dto.id,
        eventId = dto.eventId,
        ticketTypeId = dto.ticketTypeId,
        userId = dto.userId,
        ticketNumber = dto.ticketNumber,
        status = dto.status,
        purchaseDate = dto.purchaseDate,
        eventTitle = dto.eventTitle,
        eventStartDate = dto.eventStartDate,
        eventEndDate = dto.eventEndDate,
        eventLocation = dto.eventLocation,
        ticketTypeName = dto.ticketTypeName,
        ticketTypePrice = dto.price,
        quantity = null,
        totalPrice = null, 
        qrCode = dto.qrCodeUrl
    )

    fun entityToDto(entity: TicketEntity): TicketDto = TicketDto(
        id = entity.id,
        ticketNumber = entity.ticketNumber,
        userId = entity.userId,
        userName = "", 
        eventId = entity.eventId,
        eventTitle = entity.eventTitle ?: "",
        ticketTypeId = entity.ticketTypeId,
        ticketTypeName = entity.ticketTypeName ?: "",
        price = entity.ticketTypePrice ?: 0.0,
        status = entity.status,
        qrCodeUrl = entity.qrCode,
        purchaseDate = entity.purchaseDate,
        checkedInAt = null, 
        eventStartDate = entity.eventStartDate ?: "",
        eventEndDate = entity.eventEndDate ?: "",
        eventLocation = entity.eventLocation ?: "",
        eventAddress = "", 
        eventImageUrl = null 
    )
} 