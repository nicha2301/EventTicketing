package com.nicha.eventticketing.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.nicha.eventticketing.util.CloudinaryService

/**
 * Responsive Cloudinary Image Component
 */
@Composable
fun ResponsiveCloudinaryImage(
    publicId: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(8.dp),
    showLoading: Boolean = true,
    fallbackImageRes: Int? = null,
    targetWidth: Dp? = null,
    targetHeight: Dp? = null,
    quality: ImageQuality = ImageQuality.AUTO
) {
    if (publicId == null) {
        Box(
            modifier = modifier.clip(shape),
            contentAlignment = Alignment.Center
        ) {
            fallbackImageRes?.let {
                AsyncImage(
                    model = it,
                    contentDescription = contentDescription,
                    modifier = Modifier.matchParentSize(),
                    contentScale = contentScale
                )
            }
        }
        return
    }
    
    val density = LocalDensity.current
    val context = LocalContext.current
    val cloudinaryService = remember { CloudinaryService.getInstance() }
    
    val imageUrl = remember(publicId, targetWidth, targetHeight, quality) {
        when {
            targetWidth != null && targetHeight != null -> {
                val widthPx = with(density) { targetWidth.roundToPx() }
                val heightPx = with(density) { targetHeight.roundToPx() }
                cloudinaryService.generateResponsiveUrl(publicId, widthPx, heightPx)
            }
            quality == ImageQuality.THUMBNAIL -> {
                cloudinaryService.getThumbnailUrl(publicId)
            }
            quality == ImageQuality.MEDIUM -> {
                cloudinaryService.getMediumUrl(publicId)
            }
            else -> {
                cloudinaryService.generateOptimizedUrl(publicId)
            }
        }
    }
    
    var imageState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    
    Box(
        modifier = modifier.clip(shape),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = contentScale,
            onState = { state ->
                imageState = state
            }
        )
        
        if (showLoading && imageState is AsyncImagePainter.State.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        }
        
        if (imageState is AsyncImagePainter.State.Error && fallbackImageRes != null) {
            AsyncImage(
                model = fallbackImageRes,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = contentScale
            )
        }
    }
}

/**
 * Predefined image quality levels for different use cases
 */
enum class ImageQuality {
    THUMBNAIL, 
    MEDIUM,    
    HIGH,      
    AUTO     
}

/**
 * Cloudinary Image for Event Thumbnails
 */
@Composable
fun CloudinaryEventThumbnail(
    publicId: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    ResponsiveCloudinaryImage(
        publicId = publicId,
        modifier = modifier,
        contentDescription = contentDescription,
        quality = ImageQuality.THUMBNAIL,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Cloudinary Image for Event Cards
 */
@Composable
fun CloudinaryEventCard(
    publicId: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    targetWidth: Dp = 400.dp,
    targetHeight: Dp = 240.dp
) {
    ResponsiveCloudinaryImage(
        publicId = publicId,
        modifier = modifier,
        contentDescription = contentDescription,
        targetWidth = targetWidth,
        targetHeight = targetHeight,
        quality = ImageQuality.MEDIUM,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Cloudinary Image for Hero/Banner displays
 */
@Composable
fun CloudinaryEventHero(
    publicId: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    targetWidth: Dp = 800.dp,
    targetHeight: Dp = 400.dp
) {
    ResponsiveCloudinaryImage(
        publicId = publicId,
        modifier = modifier,
        contentDescription = contentDescription,
        targetWidth = targetWidth,
        targetHeight = targetHeight,
        quality = ImageQuality.HIGH,
        shape = RoundedCornerShape(0.dp),
        contentScale = ContentScale.Crop
    )
}

/**
 * Helper function to extract Cloudinary public ID from various URL formats
 */
fun extractCloudinaryPublicId(url: String?): String? {
    if (url == null) return null
    
    val cloudinaryPattern = """https?://res\.cloudinary\.com/[^/]+/image/upload/(?:v\d+/)?(?:[^/]+/)*([^/.]+)""".toRegex()
    val match = cloudinaryPattern.find(url)
    return match?.groupValues?.get(1)
}

/**
 * Backward compatibility for existing image URLs
 */
@Composable
fun SmartCloudinaryImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    quality: ImageQuality = ImageQuality.AUTO,
    targetWidth: Dp? = null,
    targetHeight: Dp? = null
) {
    val publicId = remember(imageUrl) {
        extractCloudinaryPublicId(imageUrl)
    }
    
    if (publicId != null) {
        ResponsiveCloudinaryImage(
            publicId = publicId,
            modifier = modifier,
            contentDescription = contentDescription,
            quality = quality,
            targetWidth = targetWidth,
            targetHeight = targetHeight
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}
