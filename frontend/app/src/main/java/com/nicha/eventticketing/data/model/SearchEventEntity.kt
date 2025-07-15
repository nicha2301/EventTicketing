package com.nicha.eventticketing.data.model

import com.nicha.eventticketing.data.model.EventType

/**
 * Data class for search results
 */
data class SearchEventEntity(
    val id: String,
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val imageUrl: String,
    val price: Double,
    val type: EventType
) 