package com.nicha.eventticketing.data.remote.dto.ticket

/**
 * Lớp wrapper cho response trả về từ API khi lấy danh sách loại vé
 * API trả về một đối tượng paging chứa danh sách loại vé
 */
data class TicketTypePageResponse(
    val content: List<TicketTypeDto>,
    val pageable: PageableDto,
    val last: Boolean,
    val totalPages: Int,
    val totalElements: Int,
    val first: Boolean,
    val numberOfElements: Int,
    val size: Int,
    val number: Int,
    val sort: SortDto,
    val empty: Boolean
) {
    data class PageableDto(
        val pageNumber: Int,
        val pageSize: Int,
        val sort: SortDto,
        val offset: Int,
        val paged: Boolean,
        val unpaged: Boolean
    )
    
    data class SortDto(
        val sorted: Boolean,
        val unsorted: Boolean,
        val empty: Boolean
    )
} 