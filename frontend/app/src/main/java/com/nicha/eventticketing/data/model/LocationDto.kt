package com.nicha.eventticketing.data.model

import java.util.UUID

data class LocationDto(
    val id: UUID,
    val name: String,
    val address: String,
    val city: String?,
    val state: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int?
) 