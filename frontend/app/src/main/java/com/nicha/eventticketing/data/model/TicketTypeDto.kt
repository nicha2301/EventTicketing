package com.nicha.eventticketing.data.model

import java.util.UUID

data class TicketTypeDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val price: Double,
    val availableQuantity: Int,
    val maxPerOrder: Int?,
    val eventId: UUID,
    val saleStartDate: String?,
    val saleEndDate: String?
) 