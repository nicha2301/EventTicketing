package com.eventticketing.backend.dto

import org.springframework.data.domain.Page
import java.time.LocalDateTime

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
) {
    companion object {
        fun <T> from(page: Page<T>): PagedResponse<T> {
            return PagedResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                last = page.isLast
            )
        }
    }
}

data class ErrorResponse(
    val success: Boolean = false,
    val status: Int,
    val message: String,
    val path: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val errors: Map<String, String> = emptyMap()
) 