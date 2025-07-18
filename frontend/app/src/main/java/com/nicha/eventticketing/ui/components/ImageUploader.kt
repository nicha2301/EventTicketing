package com.nicha.eventticketing.ui.components

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nicha.eventticketing.util.ImagePickerUtil
import com.nicha.eventticketing.util.PermissionUtils

/**
 * Thành phần tùy chỉnh để tải và hiển thị ảnh
 */
@Composable
fun ImageUploader(
    imageUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    onImageRemoved: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Ảnh sự kiện",
    placeholder: String = "Nhấn để chọn ảnh",
    aspectRatio: Float = 16f / 9f
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Launcher để chụp ảnh từ camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            ImagePickerUtil.tempImageUri?.let { onImageSelected(it) }
        }
    }
    
    // Launcher để yêu cầu quyền camera
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Quyền đã được cấp, tiến hành chụp ảnh
            ImagePickerUtil.createImageUri(context)?.let { uri ->
                ImagePickerUtil.tempImageUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            // Quyền bị từ chối
            Toast.makeText(
                context,
                "Cần quyền camera để chụp ảnh",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Launcher để chọn ảnh từ thư viện
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImageSelected(it) }
    }
    
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        if (imageUri != null) {
            // Hiển thị ảnh đã chọn
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Ảnh đã chọn",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Nút xóa ảnh
                IconButton(
                    onClick = onImageRemoved,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa ảnh",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            // Hiển thị vùng chọn ảnh
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { showImageSourceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    
    // Dialog chọn nguồn ảnh
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Chọn ảnh từ") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*")
                            showImageSourceDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thư viện ảnh")
                    }
                    
                    Button(
                        onClick = {
                            // Kiểm tra quyền camera trước khi chụp ảnh
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    PermissionUtils.CAMERA_PERMISSION
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            ) {
                                ImagePickerUtil.createImageUri(context)?.let { uri ->
                                    ImagePickerUtil.tempImageUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            } else {
                                // Yêu cầu quyền camera
                                requestPermissionLauncher.launch(PermissionUtils.CAMERA_PERMISSION)
                            }
                            showImageSourceDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chụp ảnh mới")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
} 