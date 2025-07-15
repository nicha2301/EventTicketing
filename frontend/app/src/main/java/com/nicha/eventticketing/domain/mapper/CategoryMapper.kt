package com.nicha.eventticketing.domain.mapper

import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.domain.model.Category
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper để chuyển đổi giữa CategoryDto và Category domain model
 */
@Singleton
class CategoryMapper @Inject constructor() {

    fun mapToDomainModel(dto: CategoryDto): Category {
        return Category(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            iconUrl = dto.iconUrl
        )
    }
} 