package com.nicha.eventticketing.domain.model

/**
 * Domain model cho Category, được sử dụng trong business logic
 */
data class Category(
    val id: String,
    val name: String,
    val description: String?,
    val iconUrl: String?
) 