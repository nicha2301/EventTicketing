package com.nicha.eventticketing.data.remote.dto.event

/**
 * DTO cho phân trang
 */
data class PageDto<T>(
    val content: List<T>,
    val pageable: PageableDto,
    val totalPages: Int,
    val totalElements: Long,
    val last: Boolean,
    val size: Int,
    val number: Int,
    val sort: SortDto,
    val numberOfElements: Int,
    val first: Boolean,
    val empty: Boolean
)

/**
 * DTO cho thông tin phân trang
 */
data class PageableDto(
    val pageNumber: Int,
    val pageSize: Int,
    val sort: SortDto,
    val offset: Long,
    val paged: Boolean,
    val unpaged: Boolean
)

/**
 * DTO cho thông tin sắp xếp
 */
data class SortDto(
    val sorted: Boolean,
    val unsorted: Boolean,
    val empty: Boolean
)

