package com.nicha.eventticketing.data.remote.dto.request

data class CloudinaryImageRequest(
    val publicId: String,
    val secureUrl: String,
    val width: Int,
    val height: Int,
    val isPrimary: Boolean = false
)
