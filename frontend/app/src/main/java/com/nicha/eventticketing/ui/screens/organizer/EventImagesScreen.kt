package com.nicha.eventticketing.ui.screens.organizer

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nicha.eventticketing.data.remote.dto.event.EventImageDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.ImageUploader
import com.nicha.eventticketing.ui.components.app.AppButton
import com.nicha.eventticketing.ui.components.app.AppDestructiveButton
import com.nicha.eventticketing.ui.components.app.AppOutlinedButton
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.util.ImageUtils.getFullUrl
import com.nicha.eventticketing.viewmodel.EventImageViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventImagesScreen(
    eventId: String,
    onBackClick: () -> Unit,
    viewModel: EventImageViewModel = hiltViewModel()
) {
    val eventImagesState by viewModel.eventImagesState.collectAsState()
    val uploadImageState by viewModel.uploadImageState.collectAsState()
    val deleteImageState by viewModel.deleteImageState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<EventImageDto?>(null) }
    var showAddImageDialog by remember { mutableStateOf(false) }
    var selectedViewImage by remember { mutableStateOf<EventImageDto?>(null) }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }

    // Fetch images when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.getEventImages(eventId)
    }

    // Handle state changes
    LaunchedEffect(uploadImageState) {
        if (uploadImageState is ResourceState.Success<*>) {
            viewModel.resetUploadImageState()
            viewModel.getEventImages(eventId)
        }
    }

    LaunchedEffect(deleteImageState) {
        if (deleteImageState is ResourceState.Success<*>) {
            viewModel.resetDeleteImageState()
            viewModel.getEventImages(eventId)
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
                    IconButton(onClick = { showAddImageDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Thêm ảnh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (eventImagesState) {
            is ResourceState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ResourceState.Error -> {
                val errorMessage = (eventImagesState as ResourceState.Error).message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    NeumorphicCard(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(300.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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
                            AppButton(
                                onClick = { viewModel.getEventImages(eventId) }
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
            }

            is ResourceState.Success<*> -> {
                val images = (eventImagesState as ResourceState.Success<List<EventImageDto>>).data

                if (images.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        NeumorphicCard(
                            modifier = Modifier
                                .width(300.dp)
                                .wrapContentHeight()
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ImageNotSupported,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(80.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "Chưa có hình ảnh nào",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Hãy thêm ảnh đầu tiên cho sự kiện của bạn",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                AppButton(
                                    onClick = { showAddImageDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Thêm hình ảnh",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Hình ảnh sự kiện (${images.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 160.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(images) { imageItem ->
                                    EventImageItem(
                                        image = imageItem,
                                        onClick = { selectedViewImage = imageItem },
                                        onDelete = { imageToDelete ->
                                            showDeleteConfirmDialog = imageToDelete
                                        }
                                    )
                                }
                            }
                        }

                        if (uploadImageState is ResourceState.Loading && uploadState.progress > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .width(300.dp)
                                    ,
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 6.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Đang tải lên hình ảnh...",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        LinearProgressIndicator(
                                            progress = { uploadState.progress },
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = "${(uploadState.progress * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có dữ liệu",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (uploadImageState is ResourceState.Loading && uploadState.progress > 0f &&
            (eventImagesState as? ResourceState.Success<List<EventImageDto>>)?.data?.isNotEmpty() == false
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .width(300.dp)
                    ,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Đang tải lên hình ảnh...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        LinearProgressIndicator(
                            progress = { uploadState.progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "${(uploadState.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        showDeleteConfirmDialog?.let { imageToDelete ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = null },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa hình ảnh này không?") },
                confirmButton = {
                    AppDestructiveButton(
                        onClick = {
                            viewModel.deleteEventImage(eventId, imageToDelete.id)
                            showDeleteConfirmDialog = null
                        }
                    ) {
                        Text("Xóa")
                    }
                },
                dismissButton = {
                    AppOutlinedButton(
                        onClick = { showDeleteConfirmDialog = null }
                    ) {
                        Text("Hủy")
                    }
                }
            )
        }

        if (showAddImageDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddImageDialog = false
                    viewModel.resetUploadState()
                },
                title = {
                    Text(
                        text = "Thêm hình ảnh sự kiện",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    ImageUploader(
                        modifier = Modifier.fillMaxWidth(),
                        uploadState = uploadState,
                        onImageSelected = { file, isPrimary ->
                            selectedImageFile = file
                            val shouldBePrimary = eventImagesState !is ResourceState.Success<*> ||
                                    (eventImagesState as? ResourceState.Success<List<EventImageDto>>)?.data?.isEmpty() == true
                            viewModel.uploadEventImage(eventId, file, shouldBePrimary)
                        },
                        onImageRemoved = {
                            selectedImageFile = null
                            viewModel.resetUploadState()
                        },
                        onError = { error ->
                            // Handle error
                        }
                    )
                },
                confirmButton = {
                    if (uploadState.isCompleted) {
                        AppButton(
                            onClick = {
                                showAddImageDialog = false
                                viewModel.resetUploadState()
                            }
                        ) {
                            Text("Hoàn tất")
                        }
                    }
                },
                dismissButton = {
                    if (!uploadState.isUploading && !uploadState.isCompleted) {
                        AppOutlinedButton(onClick = {
                            showAddImageDialog = false
                            viewModel.resetUploadState()
                        }) {
                            Text("Đóng")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurface
            )
        }

        selectedViewImage?.let { image ->
            ImageViewerDialog(
                image = image,
                onDismiss = { selectedViewImage = null }
            )
        }
    }
}

@Composable
fun ImageViewerDialog(
    image: EventImageDto,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDismiss() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image.getFullUrl())
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )

            // Thông tin ảnh
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                if (image.isPrimary) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ảnh chính",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Kích thước: ${image.width ?: "N/A"} x ${image.height ?: "N/A"}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun EventImageItem(
    image: EventImageDto,
    onClick: () -> Unit,
    onDelete: (EventImageDto) -> Unit
) {
    val context = LocalContext.current

    NeumorphicCard(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(image.getFullUrl())
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
                    .background(Color.Black.copy(alpha = 0.2f))
            )

            // Primary indicator
            if (image.isPrimary) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ảnh chính",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Delete button
            IconButton(
                onClick = { onDelete(image) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
} 