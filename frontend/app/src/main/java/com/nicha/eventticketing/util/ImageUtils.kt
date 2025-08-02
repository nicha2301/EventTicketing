package com.nicha.eventticketing.util

import com.nicha.eventticketing.config.AppConfig
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.event.EventImageDto

/**
 * Tiện ích xử lý ảnh và URL ảnh
 */
object ImageUtils {
    
    private const val FILE_API_PATH = "api/files/"

    fun getFullImageUrl(relativePath: String?): String? {
        if (relativePath.isNullOrBlank()) return null
        
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath
        }
        
        if (relativePath.startsWith(FILE_API_PATH)) {
            return AppConfig.Api.API_BASE_URL + relativePath
        }
        
        return AppConfig.Api.API_BASE_URL + FILE_API_PATH + relativePath
    }
    
    fun getFullImageUrls(relativePaths: List<String>?): List<String> {
        if (relativePaths.isNullOrEmpty()) return emptyList()
        
        return relativePaths.mapNotNull { getFullImageUrl(it) }
    }
    
    fun EventDto.getFullFeaturedImageUrl(): String? {
        return getFullImageUrl(this.featuredImageUrl)
    }
    
    fun EventDto.getFullImageUrls(): List<String> {
        return getFullImageUrls(this.imageUrls)
    }
    
    fun EventDto.getPrimaryImageUrl(): String? {
        return when {
            this.imageUrls.isNotEmpty() -> this.imageUrls.first()
            this.featuredImageUrl != null -> getFullImageUrl(this.featuredImageUrl)
            else -> null
        }
    }
    
    fun EventDto.getAllImageUrls(): List<String> {
        return when {
            this.imageUrls.isNotEmpty() -> this.imageUrls
            this.featuredImageUrl != null -> {
                getFullImageUrl(this.featuredImageUrl)?.let { listOf(it) } ?: emptyList()
            }
            else -> emptyList()
        }
    }

    fun EventImageDto.getFullUrl(): String? {
        return getFullImageUrl(this.url)
    }
} 