package com.nicha.eventticketing.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.viewmodel.OrganizerEventViewModel
import com.nicha.eventticketing.ui.components.EventStatusChip
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.ui.theme.LocalNeumorphismStyle
import com.nicha.eventticketing.util.ImageUtils.getFullFeaturedImageUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerEventDetailScreen(
    eventId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onManageTicketsClick: (String) -> Unit,
    onManageImagesClick: (String) -> Unit,
    viewModel: OrganizerEventViewModel = hiltViewModel()
) {
    val eventDetailState by viewModel.eventDetailState.collectAsState()
    val deleteEventState by viewModel.deleteEventState.collectAsState()
    val publishEventState by viewModel.publishEventState.collectAsState()
    val cancelEventState by viewModel.cancelEventState.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPublishDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancellationReason by remember { mutableStateOf("") }
    
    // Refresh event when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.getEventById(eventId)
    }
    
    // Handle state changes
    LaunchedEffect(deleteEventState) {
        if (deleteEventState is ResourceState.Success) {
            onBackClick()
            viewModel.resetDeleteEventState()
        }
    }
    
    LaunchedEffect(publishEventState) {
        if (publishEventState is ResourceState.Success) {
            viewModel.getEventById(eventId)
            viewModel.resetPublishEventState()
        }
    }
    
    LaunchedEffect(cancelEventState) {
        if (cancelEventState is ResourceState.Success) {
            viewModel.getEventById(eventId)
            viewModel.resetCancelEventState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sự kiện") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    val event = (eventDetailState as? ResourceState.Success<EventDto>)?.data
                    if (event?.status == "DRAFT") {
                        IconButton(onClick = { showPublishDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Publish,
                                contentDescription = "Công bố sự kiện"
                            )
                        }
                    }
                    if (event?.status == "PUBLISHED") {
                        IconButton(onClick = { showCancelDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = "Hủy sự kiện"
                            )
                        }
                    }
                    IconButton(onClick = { onEditClick(eventId) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Chỉnh sửa"
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Xóa sự kiện"
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
            when (eventDetailState) {
                is ResourceState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ResourceState.Error -> {
                    val errorMessage = (eventDetailState as ResourceState.Error).message
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
                            Button(onClick = { viewModel.getEventById(eventId) }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                is ResourceState.Success -> {
                    val event = (eventDetailState as ResourceState.Success<EventDto>).data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Event image with gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        ) {
                            AsyncImage(
                                model = event.getFullFeaturedImageUrl(),
                                contentDescription = "Event image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                            )
                            
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = event.shortDescription ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                EventStatusChip(status = event.status)
                            }
                        }
                        
                        // Action buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ActionButton(
                                icon = Icons.AutoMirrored.Filled.EventNote,
                                text = "Loại vé",
                                onClick = { onManageTicketsClick(eventId) }
                            )
                            
                            ActionButton(
                                icon = Icons.Default.Image,
                                text = "Hình ảnh",
                                onClick = { onManageImagesClick(eventId) }
                            )
                            
                            ActionButton(
                                icon = Icons.Default.Analytics,
                                text = "Thống kê",
                                onClick = { /* TODO */ }
                            )
                            
                            ActionButton(
                                icon = Icons.Default.QrCode2,
                                text = "Check-in",
                                onClick = { /* TODO */ }
                            )
                        }
                        
                        // Event details
                        NeumorphicCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Chi tiết sự kiện",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                DetailItem(
                                    icon = Icons.Default.CalendarToday,
                                    title = "Thời gian",
                                    content = "${event.startDate} đến ${event.endDate}"
                                )
                                
                                DetailItem(
                                    icon = Icons.Default.LocationOn,
                                    title = "Địa điểm",
                                    content = "${event.locationName}, ${event.address}, ${event.city ?: ""}"
                                )
                                
                                DetailItem(
                                    icon = Icons.Default.Category,
                                    title = "Danh mục",
                                    content = event.categoryName ?: "Chưa phân loại"
                                )
                                
                                DetailItem(
                                    icon = Icons.Default.PeopleAlt,
                                    title = "Số người tham dự",
                                    content = "${event.currentAttendees ?: 0}/${event.maxAttendees ?: 0}"
                                )
                                
                                DetailItem(
                                    icon = Icons.Default.MonetizationOn,
                                    title = "Giá vé",
                                    content = if (event.isFree) "Miễn phí" else "${event.minTicketPrice ?: 0} - ${event.maxTicketPrice ?: 0} VND"
                                )
                            }
                        }
                        
                        // Event description
                        NeumorphicCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Mô tả",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodyMedium
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
            
            // Delete dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Xác nhận xóa sự kiện") },
                    text = { Text("Bạn có chắc chắn muốn xóa sự kiện này không? Hành động này không thể hoàn tác.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteEvent(eventId)
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Xóa sự kiện")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }
            
            // Publish dialog
            if (showPublishDialog) {
                AlertDialog(
                    onDismissRequest = { showPublishDialog = false },
                    title = { Text("Xác nhận công bố sự kiện") },
                    text = { Text("Khi công bố, sự kiện sẽ hiển thị với tất cả người dùng. Bạn có muốn tiếp tục?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.publishEvent(eventId)
                                showPublishDialog = false
                            }
                        ) {
                            Text("Công bố")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPublishDialog = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }
            
            // Cancel event dialog
            if (showCancelDialog) {
                Dialog(
                    onDismissRequest = { showCancelDialog = false },
                    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Hủy sự kiện",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Vui lòng cho biết lý do hủy sự kiện. Lý do sẽ được thông báo đến những người đã đăng ký tham dự.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            OutlinedTextField(
                                value = cancellationReason,
                                onValueChange = { cancellationReason = it },
                                label = { Text("Lý do hủy") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showCancelDialog = false }) {
                                    Text("Đóng")
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Button(
                                    onClick = {
                                        if (cancellationReason.isNotEmpty()) {
                                            viewModel.cancelEvent(eventId, cancellationReason)
                                            showCancelDialog = false
                                        }
                                    },
                                    enabled = cancellationReason.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Hủy sự kiện")
                                }
                            }
                        }
                    }
                }
            }
            
            // Loading indicators for state changes
            if (deleteEventState is ResourceState.Loading ||
                publishEventState is ResourceState.Loading ||
                cancelEventState is ResourceState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DetailItem(
    icon: ImageVector,
    title: String,
    content: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 