package com.nicha.eventticketing.data.remote.dto.location

/**
 * DTO cho thông tin địa điểm
 */
data class LocationDto(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int?,
    val description: String?,
    val imageUrl: String?,
    val contactInfo: String?,
    val website: String?
) 