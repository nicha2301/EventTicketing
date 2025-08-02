package com.eventticketing.backend.service

import com.eventticketing.backend.entity.EventImage
import com.eventticketing.backend.entity.StorageProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class CloudinaryStorageService {
    
    @Autowired
    private lateinit var cloudinaryService: CloudinaryService
    
    /**
     * Upload image to Cloudinary
     */
    fun uploadImage(file: MultipartFile, folder: String): StorageUploadResult {
        if (!cloudinaryService.isConfigured()) {
            throw CloudinaryException("Cloudinary is not properly configured. Please check your credentials.")
        }
        
        return try {
            val result = cloudinaryService.uploadImage(file, folder)
            
            StorageUploadResult(
                success = true,
                provider = StorageProvider.CLOUDINARY,
                cloudinaryPublicId = result.publicId,
                cloudinaryUrl = result.url,
                thumbnailUrl = result.thumbnailUrl,
                mediumUrl = result.mediumUrl,
                bytes = result.bytes
            )
        } catch (e: Exception) {
            throw CloudinaryException("Failed to upload image to Cloudinary: ${e.message}", e)
        }
    }
    
    /**
     * Delete image from Cloudinary
     */
    fun deleteImage(eventImage: EventImage): Boolean {
        return if (eventImage.storageProvider == StorageProvider.CLOUDINARY) {
            eventImage.cloudinaryPublicId?.let { publicId ->
                cloudinaryService.deleteImage(publicId)
            } ?: false
        } else {
            false
        }
    }
    
    /**
     * Get Cloudinary URL for displaying the image
     */
    fun getImageUrl(eventImage: EventImage): String {
        
        return if (eventImage.storageProvider == StorageProvider.CLOUDINARY) {
            val cloudinaryUrl = eventImage.cloudinaryUrl ?: ""
            cloudinaryUrl
        } else {
            ""
        }
    }
    
    /**
     * Get thumbnail URL (Cloudinary automatic thumbnails)
     */
    fun getThumbnailUrl(eventImage: EventImage): String {
        return if (eventImage.storageProvider == StorageProvider.CLOUDINARY) {
            eventImage.thumbnailUrl ?: eventImage.cloudinaryUrl ?: ""
        } else {
            getImageUrl(eventImage)
        }
    }
    
    /**
     * Get medium size URL (Cloudinary automatic sizing)
     */
    fun getMediumUrl(eventImage: EventImage): String {
        return if (eventImage.storageProvider == StorageProvider.CLOUDINARY) {
            eventImage.mediumUrl ?: eventImage.cloudinaryUrl ?: ""
        } else {
            getImageUrl(eventImage)
        }
    }
    
    /**
     * Generate responsive URL for Cloudinary images
     */
    fun getResponsiveUrl(eventImage: EventImage, width: Int, height: Int): String {
        return if (eventImage.storageProvider == StorageProvider.CLOUDINARY) {
            eventImage.cloudinaryPublicId?.let { publicId ->
                cloudinaryService.generateResponsiveUrl(publicId, width, height)
            } ?: getImageUrl(eventImage)
        } else {
            getImageUrl(eventImage)
        }
    }
    
    /**
     * Check if image exists in Cloudinary
     */
    fun imageExists(eventImage: EventImage): Boolean {
        return eventImage.storageProvider == StorageProvider.CLOUDINARY && 
               !eventImage.cloudinaryUrl.isNullOrBlank()
    }
    
    /**
     * Get Cloudinary usage statistics
     */
    fun getStorageStats(): StorageStats {
        val cloudinaryStats = if (cloudinaryService.isConfigured()) {
            try {
                cloudinaryService.getUsageStats()
            } catch (e: Exception) {
                null
            }
        } else null
        
        return StorageStats(
            cloudinaryConfigured = cloudinaryService.isConfigured(),
            cloudinaryUsage = cloudinaryStats
        )
    }
}

data class StorageUploadResult(
    val success: Boolean,
    val provider: StorageProvider,
    val cloudinaryPublicId: String?,
    val cloudinaryUrl: String?,
    val thumbnailUrl: String?,
    val mediumUrl: String?,
    val bytes: Int,
    val error: String? = null
)

data class StorageStats(
    val cloudinaryConfigured: Boolean,
    val cloudinaryUsage: CloudinaryUsageStats?
)
