package com.nicha.eventticketing.data.remote.dto.event

import com.google.gson.annotations.SerializedName

data class CreateEventWithImagesRequest(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("shortDescription")
    val shortDescription: String,
    
    @SerializedName("categoryId")
    val categoryId: String,
    
    @SerializedName("locationId")
    val locationId: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("city")
    val city: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("maxAttendees")
    val maxAttendees: Int,
    
    @SerializedName("startDate")
    val startDate: String,
    
    @SerializedName("endDate")
    val endDate: String,
    
    @SerializedName("isPrivate")
    val isPrivate: Boolean = false,
    
    @SerializedName("isDraft")
    val isDraft: Boolean = true,
    
    @SerializedName("isFree")
    val isFree: Boolean = false
)
