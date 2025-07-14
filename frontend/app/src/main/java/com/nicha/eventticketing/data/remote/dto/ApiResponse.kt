package com.nicha.eventticketing.data.remote.dto

/**
 * Class chung cho tất cả các API response
 */
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null
) 