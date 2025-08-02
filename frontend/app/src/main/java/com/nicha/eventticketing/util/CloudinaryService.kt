package com.nicha.eventticketing.util

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.nicha.eventticketing.config.AppConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

/**
 * Cloudinary Service
 */
class CloudinaryService private constructor() {
    
    companion object {
        private const val TAG = "CloudinaryService"
        
        @Volatile
        private var INSTANCE: CloudinaryService? = null
        
        fun getInstance(): CloudinaryService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CloudinaryService().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Initialize Cloudinary MediaManager
     */
    fun initialize(context: Context) {
        try {
            val config = mapOf(
                "cloud_name" to AppConfig.CloudinaryConfig.CLOUD_NAME,
                "api_key" to AppConfig.CloudinaryConfig.API_KEY,
                "api_secret" to AppConfig.CloudinaryConfig.API_SECRET,
                "secure" to true
            )
            
            MediaManager.init(context, config)
        } catch (e: Exception) {
        }
    }
    
    /**
     * Upload image from URI with progress tracking
     */
    suspend fun uploadImage(
        uri: Uri,
        context: Context,
        folder: String = AppConfig.CloudinaryConfig.FOLDER_PREFIX,
        onProgress: ((Int) -> Unit)? = null
    ): CloudinaryResult = suspendCancellableCoroutine { continuation ->
        
        try {
            val requestId = MediaManager.get().upload(uri)
                .option("folder", folder)
                .option("resource_type", "image")
                .option("quality", "auto")
                .option("fetch_format", "auto")
                .option("unique_filename", true)
                .option("overwrite", false)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        onProgress?.invoke(0)
                    }
                    
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        if (totalBytes > 0) {
                            val progress = ((bytes * 100) / totalBytes).toInt()
                            onProgress?.invoke(progress)
                        }
                    }
                    
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val publicId = resultData["public_id"] as? String
                        val secureUrl = resultData["secure_url"] as? String
                        val format = resultData["format"] as? String
                        val width = resultData["width"] as? Int
                        val height = resultData["height"] as? Int
                        
                        if (publicId != null && secureUrl != null) {
                            val result = CloudinaryResult.Success(
                                publicId = publicId,
                                secureUrl = secureUrl,
                                format = format ?: "jpg",
                                width = width ?: 0,
                                height = height ?: 0,
                                thumbnailUrl = AppConfig.CloudinaryConfig.generateThumbnailUrl(publicId),
                                mediumUrl = AppConfig.CloudinaryConfig.generateMediumUrl(publicId)
                            )
                            continuation.resume(result)
                        } else {
                            val error = CloudinaryResult.Error("Missing required fields in response")
                            continuation.resume(error)
                        }
                    }
                    
                    override fun onError(requestId: String, error: ErrorInfo) {
                        val result = CloudinaryResult.Error(error.description ?: "Upload failed")
                        continuation.resume(result)
                    }
                    
                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                    }
                })
                .dispatch()
                
            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
            }
            
        } catch (e: Exception) {
            continuation.resume(CloudinaryResult.Error(e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Upload image from file path
     */
    suspend fun uploadImageFromFile(
        filePath: String,
        context: Context,
        folder: String = AppConfig.CloudinaryConfig.FOLDER_PREFIX,
        onProgress: ((Int) -> Unit)? = null
    ): CloudinaryResult {
        val file = File(filePath)
        return if (file.exists()) {
            val uri = Uri.fromFile(file)
            uploadImage(uri, context, folder, onProgress)
        } else {
            CloudinaryResult.Error("File not found: $filePath")
        }
    }
    
    /**
     * Generate responsive image URL for specific dimensions
     */
    fun generateResponsiveUrl(publicId: String, width: Int, height: Int): String {
        return AppConfig.CloudinaryConfig.generateResponsiveUrl(publicId, width, height)
    }
    
    /**
     * Generate optimized image URL with quality and format auto
     */
    fun generateOptimizedUrl(publicId: String): String {
        return AppConfig.CloudinaryConfig.generateImageUrl(publicId)
    }
    
    /**
     * Get thumbnail URL
     */
    fun getThumbnailUrl(publicId: String): String {
        return AppConfig.CloudinaryConfig.generateThumbnailUrl(publicId)
    }
    
    /**
     * Get medium size URL
     */
    fun getMediumUrl(publicId: String): String {
        return AppConfig.CloudinaryConfig.generateMediumUrl(publicId)
    }
}

sealed class CloudinaryResult {
    data class Success(
        val publicId: String,
        val secureUrl: String,
        val format: String,
        val width: Int,
        val height: Int,
        val thumbnailUrl: String,
        val mediumUrl: String
    ) : CloudinaryResult()
    
    data class Error(
        val message: String
    ) : CloudinaryResult()
    
    object Loading : CloudinaryResult()
}

data class UploadProgress(
    val percentage: Int = 0,
    val isUploading: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)
