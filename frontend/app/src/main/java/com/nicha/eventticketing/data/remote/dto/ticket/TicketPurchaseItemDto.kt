package com.nicha.eventticketing.data.remote.dto.ticket

import kotlinx.serialization.Serializable

@Serializable
data class TicketPurchaseItemDto(
    val ticketTypeId: String,
    val quantity: Int
)
