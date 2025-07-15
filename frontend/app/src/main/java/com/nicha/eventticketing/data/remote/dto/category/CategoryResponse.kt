package com.nicha.eventticketing.data.remote.dto.category

import com.nicha.eventticketing.data.remote.dto.event.PageableDto
import com.nicha.eventticketing.data.remote.dto.event.SortDto
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Lớp wrapper để xử lý response từ API categories
 */
@JsonClass(generateAdapter = true)
data class CategoryResponse(
    @Json(name = "content") val content: List<CategoryDto>? = null,
    @Json(name = "pageable") val pageable: PageableDto? = null,
    @Json(name = "totalPages") val totalPages: Int? = null,
    @Json(name = "totalElements") val totalElements: Long? = null,
    @Json(name = "last") val last: Boolean? = null,
    @Json(name = "size") val size: Int? = null,
    @Json(name = "number") val number: Int? = null,
    @Json(name = "sort") val sort: SortDto? = null,
    @Json(name = "numberOfElements") val numberOfElements: Int? = null,
    @Json(name = "first") val first: Boolean? = null,
    @Json(name = "empty") val empty: Boolean? = null
) 