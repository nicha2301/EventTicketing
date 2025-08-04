package com.eventticketing.backend.dto

data class EventWithMultipleImagesRequest(
    val event: EventCreateDto,
    val primaryImageIndex: Int? = null
)
