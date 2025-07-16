package com.nicha.eventticketing.data.remote.dto.event

import com.google.gson.annotations.SerializedName

data class EventImageDto(
    @SerializedName("id") val id: String,
    @SerializedName("url") val url: String,
    @SerializedName("eventId") val eventId: String,
    @SerializedName("isPrimary") val isPrimary: Boolean,
    @SerializedName("createdAt") val createdAt: String
) 