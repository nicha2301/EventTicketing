package com.nicha.eventticketing.data.model

data class TicketType(
    val id: String,
    val name: String,
    val price: Double,
    val description: String,
    val availableQuantity: Int
) 