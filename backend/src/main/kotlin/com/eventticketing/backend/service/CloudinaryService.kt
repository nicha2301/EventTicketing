package com.eventticketing.backend.service

import com.cloudinary.Cloudinary
import com.cloudinary.Transformation
import com.cloudinary.utils.ObjectUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*
import jakarta.annotation.PostConstruct

@Service
class CloudinaryService {
    
    @Value("\${app.cloudinary.cloud-name}")
    private lateinit var cloudName: String
    
    @Value("\${app.cloudinary.api-key}")
    private lateinit var apiKey: String
    
    @Value("\${app.cloudinary.api-secret}")
    private lateinit var apiSecret: String
    
    @Value("\${app.cloudinary.folder-prefix}")
    private lateinit var folderPrefix: String
    
    @Value("\${app.cloudinary.secure}")
    private var secure: Boolean = true
    
    private lateinit var cloudinary: Cloudinary
    
    @PostConstruct
    fun init() {
        val config = mapOf(
            "cloud_name" to cloudName,
            "api_key" to apiKey,
            "api_secret" to apiSecret,
            "secure" to secure
        )
        cloudinary = Cloudinary(config)
    }
    
    /**
     * Upload image to Cloudinary with automatic optimizations
     */
    fun uploadImage(file: MultipartFile, folder: String, publicId: String? = null): CloudinaryUploadResult {
        try {
            val uniquePublicId = publicId ?: "${UUID.randomUUID()}"
            
            val uploadParams = ObjectUtils.asMap(
                "folder", "$folderPrefix/$folder",
                "public_id", uniquePublicId,
                "resource_type", "image"
            )
            
            val result = cloudinary.uploader().upload(file.bytes, uploadParams)
            
            return CloudinaryUploadResult(
                publicId = result["public_id"] as String,
                url = result["secure_url"] as String,
                thumbnailUrl = generateThumbnailUrl(result["public_id"] as String),
                mediumUrl = generateMediumUrl(result["public_id"] as String),
                format = result["format"] as String,
                bytes = result["bytes"] as Int,
                width = result["width"] as Int,
                height = result["height"] as Int,
                version = result["version"] as Int
            )
        } catch (e: Exception) {
            throw CloudinaryException("Failed to upload image: ${e.message}", e)
        }
    }
    
    /**
     * Delete image from Cloudinary
     */
    fun deleteImage(publicId: String): Boolean {
        return try {
            val result = cloudinary.uploader().destroy(publicId, emptyMap<String, Any>())
            val deleteResult = result["result"] as String
            val success = deleteResult == "ok"
            
            success
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate optimized thumbnail URL
     */
    fun generateThumbnailUrl(publicId: String): String {
        return "https://res.cloudinary.com/$cloudName/image/upload/w_300,h_300,c_thumb,g_face,q_auto,f_auto/$publicId"
    }
    
    /**
     * Generate optimized medium size URL
     */
    fun generateMediumUrl(publicId: String): String {
        return "https://res.cloudinary.com/$cloudName/image/upload/w_800,h_600,c_fill,q_auto,f_auto/$publicId"
    }
    
    /**
     * Generate custom transformation URL
     */
    fun generateTransformationUrl(publicId: String, transformation: String): String {
        return "https://res.cloudinary.com/$cloudName/image/upload/$transformation/$publicId"
    }
    
    /**
     * Generate responsive image URL based on device size
     */
    fun generateResponsiveUrl(publicId: String, width: Int, height: Int): String {
        return "https://res.cloudinary.com/$cloudName/image/upload/w_$width,h_$height,c_fill,q_auto,f_auto/$publicId"
    }
    
    /**
     * Check if Cloudinary is properly configured
     */
    fun isConfigured(): Boolean {
        return try {
            cloudName.isNotBlank() && 
            apiKey.isNotBlank() && 
            apiSecret.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get Cloudinary usage stats
     */
    fun getUsageStats(): CloudinaryUsageStats {
        return try {
            val result = cloudinary.api().usage(emptyMap<String, Any>())
            
            CloudinaryUsageStats(
                storage = (result["storage"] as Map<String, Any>)["size"] as Long,
                bandwidth = (result["bandwidth"] as Map<String, Any>)["usage"] as Long,
                transformations = (result["transformations"] as Map<String, Any>)["usage"] as Long,
                requests = (result["requests"] as Long)
            )
        } catch (e: Exception) {
            CloudinaryUsageStats(0, 0, 0, 0)
        }
    }
}

/**
 * Data class for Cloudinary upload result
 */
data class CloudinaryUploadResult(
    val publicId: String,
    val url: String,
    val thumbnailUrl: String,
    val mediumUrl: String,
    val format: String,
    val bytes: Int,
    val width: Int,
    val height: Int,
    val version: Int
)

data class CloudinaryUsageStats(
    val storage: Long,
    val bandwidth: Long,
    val transformations: Long,
    val requests: Long
)

/**
 * Custom exception for Cloudinary operations
 */
class CloudinaryException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
