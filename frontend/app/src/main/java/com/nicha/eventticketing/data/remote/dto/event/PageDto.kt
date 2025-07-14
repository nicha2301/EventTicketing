package com.nicha.eventticketing.data.remote.dto.event

/**
 * Page response for paginated results
 */
data class Page<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)

