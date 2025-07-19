package com.nicha.eventticketing.data.remote.dto.ticket

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CheckInRequestDto(
    @Json(name = "ticketId")
    val ticketId: String,
    
    @Json(name = "eventId")
    val eventId: String,
    
    @Json(name = "userId")
    val userId: String
) 