package com.nicha.eventticketing.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nicha.eventticketing.data.remote.dto.event.EventImageDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.viewmodel.EventImageViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventImagesScreen(
    eventId: String,
    onBackClick: () -> Unit,
    onSelectImage: () -> Unit = {},
    viewModel: EventImageViewModel = hiltViewModel()
) {
    val eventImagesState by viewModel.eventImagesState.collectAsState()
    val uploadImageState by viewModel.uploadImageState.collectAsState()
    val deleteImageState by viewModel.deleteImageState.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<EventImageDto?>(null) }
    
    // Fetch images when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.getEventImages(eventId)
    }
    
    // Handle state changes
    LaunchedEffect(uploadImageState) {
        if (uploadImageState is ResourceState.Success) {
            viewModel.resetUploadImageState()
        }
    }
    
    LaunchedEffect(deleteImageState) {
        if (deleteImageState is ResourceState.Success) {
            viewModel.resetDeleteImageState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý hình ảnh sự kiện") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSelectImage) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Thêm ảnh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (eventImagesState) {
                is ResourceState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ResourceState.Error -> {
                    val errorMessage = (eventImagesState as ResourceState.Error).message
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.getEventImages(eventId) }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                is ResourceState.Success -> {
                    val images = (eventImagesState as ResourceState.Success<List<EventImageDto>>).data
                    
                    if (images.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ImageNotSupported,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Chưa có hình ảnh nào",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Button(onClick = onSelectImage) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Thêm hình ảnh")
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(images) { image ->
                                EventImageItem(
                                    image = image,
                                    onSetAsPrimary = { imageId ->
                                        // Implement set as primary functionality
                                    },
                                    onDelete = { imageToDelete ->
                                        showDeleteConfirmDialog = imageToDelete
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Không có dữ liệu",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Upload progress overlay
            if (uploadImageState is ResourceState.Loading && uploadProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(300.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Đang tải lên hình ảnh...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            LinearProgressIndicator(
                                progress = { uploadProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "${(uploadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Handle selected image file for upload
            selectedImageFile?.let { file ->
                LaunchedEffect(file) {
                    // TODO: Implement logic to determine if this should be primary
                    val isPrimary = eventImagesState !is ResourceState.Success || 
                                    (eventImagesState as? ResourceState.Success<List<EventImageDto>>)?.data?.isEmpty() == true
                    
                    viewModel.uploadEventImage(eventId, file, isPrimary)
                    selectedImageFile = null
                }
            }
            
            // Delete confirmation dialog
            showDeleteConfirmDialog?.let { imageToDelete ->
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = null },
                    title = { Text("Xác nhận xóa") },
                    text = { Text("Bạn có chắc chắn muốn xóa hình ảnh này không?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteEventImage(eventId, imageToDelete.id)
                                showDeleteConfirmDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Xóa")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = null }) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EventImageItem(
    image: EventImageDto,
    onSetAsPrimary: (String) -> Unit,
    onDelete: (EventImageDto) -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (image.isPrimary) 3.dp else 1.dp,
                color = if (image.isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("/api/files/${image.url}")
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay with actions
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        
        // Primary indicator
        if (image.isPrimary) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Chính",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = { onSetAsPrimary(image.id) },
                enabled = !image.isPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Đặt làm ảnh chính",
                    tint = if (image.isPrimary) MaterialTheme.colorScheme.primary else Color.White
                )
            }
            
            IconButton(
                onClick = { onDelete(image) },
                enabled = true
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = Color.White
                )
            }
        }
    }
} 