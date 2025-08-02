package com.nicha.eventticketing.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nicha.eventticketing.util.CloudinaryResult
import com.nicha.eventticketing.util.CloudinaryService
import com.nicha.eventticketing.util.UploadProgress
import kotlinx.coroutines.launch

/**
 * Cloudinary Image Uploader Component
 */
@Composable
fun CloudinaryImageUploader(
    modifier: Modifier = Modifier,
    currentImageUrl: String? = null,
    onImageUploaded: (CloudinaryResult.Success) -> Unit,
    onImageRemoved: () -> Unit,
    onError: (String) -> Unit,
    enableMultipleImages: Boolean = false,
    maxImages: Int = 5,
    folder: String = "event-images"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cloudinaryService = remember { CloudinaryService.getInstance() }
    
    var uploadProgress by remember { mutableStateOf(UploadProgress()) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            scope.launch {
                uploadImageToCloudinary(
                    uri = it,
                    context = context,
                    cloudinaryService = cloudinaryService,
                    folder = folder,
                    onProgress = { progress ->
                        uploadProgress = uploadProgress.copy(
                            percentage = progress,
                            isUploading = true,
                            isCompleted = false
                        )
                    },
                    onSuccess = { result ->
                        uploadProgress = uploadProgress.copy(
                            isUploading = false,
                            isCompleted = true,
                            percentage = 100
                        )
                        onImageUploaded(result)
                        selectedUri = null
                    },
                    onError = { error ->
                        uploadProgress = uploadProgress.copy(
                            isUploading = false,
                            isCompleted = false,
                            error = error,
                            percentage = 0
                        )
                        onError(error)
                        selectedUri = null
                    }
                )
            }
        }
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (currentImageUrl != null) {
            CurrentImageDisplay(
                imageUrl = currentImageUrl,
                onRemove = onImageRemoved
            )
        }
        
        if (uploadProgress.isUploading) {
            UploadProgressDisplay(uploadProgress = uploadProgress)
        }
        
        uploadProgress.error?.let { error ->
            ErrorDisplay(
                error = error,
                onDismiss = {
                    uploadProgress = uploadProgress.copy(error = null)
                }
            )
        }
        
        if (!uploadProgress.isUploading && currentImageUrl == null) {
            ImagePickerButton(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                }
            )
        }
        
        if (uploadProgress.isCompleted && uploadProgress.error == null) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                uploadProgress = UploadProgress()
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tải ảnh lên thành công!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentImageDisplay(
    imageUrl: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Current Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Image",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun UploadProgressDisplay(uploadProgress: UploadProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đang tải ảnh lên...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${uploadProgress.percentage}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            LinearProgressIndicator(
                progress = uploadProgress.percentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ErrorDisplay(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    }
}

@Composable
private fun ImagePickerButton(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Chọn ảnh",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Hỗ trợ JPG, PNG",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private suspend fun uploadImageToCloudinary(
    uri: Uri,
    context: Context,
    cloudinaryService: CloudinaryService,
    folder: String,
    onProgress: (Int) -> Unit,
    onSuccess: (CloudinaryResult.Success) -> Unit,
    onError: (String) -> Unit
) {
    try {
        when (val result = cloudinaryService.uploadImage(
            uri = uri,
            context = context,
            folder = folder,
            onProgress = onProgress
        )) {
            is CloudinaryResult.Success -> {
                onSuccess(result)
            }
            is CloudinaryResult.Error -> {
                onError(result.message)
            }
            is CloudinaryResult.Loading -> {
            }
        }
    } catch (e: Exception) {
        onError(e.message ?: "Lỗi không xác định khi tải ảnh")
    }
}
