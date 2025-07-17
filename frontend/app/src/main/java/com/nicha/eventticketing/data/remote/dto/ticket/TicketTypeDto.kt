package com.nicha.eventticketing.data.remote.dto.ticket

data class TicketTypeDto(
    val id: String = "",
    val eventId: String,
    val name: String,
    val description: String?,
    val price: Double,
    val quantity: Int,
    val availableQuantity: Int = 0,
    val quantitySold: Int = 0,
    val salesStartDate: String? = null,
    val salesEndDate: String? = null,
    val maxTicketsPerCustomer: Int? = null,
    val minTicketsPerOrder: Int = 1,
    val isEarlyBird: Boolean = false,
    val isVIP: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
) 