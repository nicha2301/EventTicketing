package com.nicha.eventticketing.domain.model

/**
 * Lớp quản lý trạng thái dữ liệu
 */
sealed class ResourceState<out T> {
    object Initial : ResourceState<Nothing>()
    object Loading : ResourceState<Nothing>()
    data class Success<T>(val data: T) : ResourceState<T>()
    data class Error(val message: String) : ResourceState<Nothing>()
} 