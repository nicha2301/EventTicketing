package com.nicha.eventticketing.ui.screens.organizer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nicha.eventticketing.data.model.EventStatus
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.EmptyStateView
import com.nicha.eventticketing.ui.components.ErrorView
import com.nicha.eventticketing.ui.components.LoadingIndicator
import com.nicha.eventticketing.viewmodel.OrganizerEventViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDashboardScreen(
    onBackClick: () -> Unit,
    onEventClick: (String) -> Unit,
    onCreateEventClick: () -> Unit,
    onScanQRClick: () -> Unit,
    viewModel: OrganizerEventViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    // Lấy danh sách sự kiện từ ViewModel
    val eventsState by viewModel.organizerEventsState.collectAsState()
    
    // Lấy dữ liệu sự kiện khi màn hình được hiển thị
    LaunchedEffect(Unit) {
        viewModel.getOrganizerEvents()
    }

    // Lọc sự kiện theo trạng thái
    val events = if (eventsState is ResourceState.Success) {
        (eventsState as ResourceState.Success).data.content
    } else {
        emptyList()
    }
    
    val activeEvents = events.filter { 
        it.status == "UPCOMING" || 
        it.status == "ONGOING" 
    }
    
    val pastEvents = events.filter { 
        it.status == "COMPLETED" || 
        it.status == "CANCELLED" 
    }
    
    val draftEvents = events.filter { 
        it.status == "DRAFT" || it.status == null
    }
    
    // Tính toán thống kê cho dashboard
    val totalAttendees = events.sumOf { it.currentAttendees ?: 0 }
    val totalTicketsSold = events.flatMap { it.ticketTypes ?: emptyList() }.sumOf { it.quantitySold ?: 0 }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý sự kiện") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onScanQRClick) {
                        Icon(
                            imageVector = Icons.Filled.QrCode2,
                            contentDescription = "Quét mã QR"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEventClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Tạo sự kiện mới",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        when (eventsState) {
            is ResourceState.Loading -> {
                LoadingIndicator(modifier = Modifier.fillMaxSize())
            }
            is ResourceState.Error -> {
                val errorMessage = (eventsState as ResourceState.Error).message
                ErrorView(
                    message = errorMessage,
                    onRetry = { viewModel.getOrganizerEvents() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Dashboard summary
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Tổng quan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                DashboardStat(
                                    icon = Icons.Filled.CalendarToday,
                                    value = "${activeEvents.size}",
                                    label = "Sự kiện đang diễn ra"
                                )
                                
                                DashboardStat(
                                    icon = Icons.Filled.PeopleAlt,
                                    value = "$totalAttendees",
                                    label = "Người tham dự"
                                )
                                
                                DashboardStat(
                                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                    value = "$totalTicketsSold",
                                    label = "Vé đã bán"
                                )
                            }
                        }
                    }
                    
                    // Event tabs
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("Đang diễn ra (${activeEvents.size})") }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Đã kết thúc (${pastEvents.size})") }
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 },
                            text = { Text("Bản nháp (${draftEvents.size})") }
                        )
                    }
                    
                    // Event list
                    when (selectedTabIndex) {
                        0 -> EventList(events = activeEvents, onEventClick = onEventClick)
                        1 -> EventList(events = pastEvents, onEventClick = onEventClick)
                        2 -> EventList(events = draftEvents, onEventClick = onEventClick)
                    }
                    
                    // Button to view all events
                    Button(
                        onClick = { onEventClick("list") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ViewList,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text("Xem tất cả sự kiện")
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EventList(
    events: List<EventDto>,
    onEventClick: (String) -> Unit
) {
    if (events.isEmpty()) {
        EmptyStateView(
            icon = Icons.Filled.Category,
            message = "Không có sự kiện nào",
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(events) { event ->
                EventItem(event = event, onEventClick = { onEventClick(event.id) })
            }
        }
    }
}

@Composable
fun EventItem(
    event: EventDto,
    onEventClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEventClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            // Event image
            if (event.featuredImageUrl != null) {
                AsyncImage(
                    model = "/api/files/${event.featuredImageUrl}",
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(120.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            // Event info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    CategoryChip(categoryName = event.categoryName ?: "Khác")
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = formatDateTimeFromString(event.startDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "${event.locationName}, ${event.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (event.isFree == true) {
                        "Miễn phí"
                    } else {
                        "${event.minTicketPrice?.toInt()?.toString()?.replace(Regex("\\B(?=(\\d{3})+(?!\\d))"), ".")} VND"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Status chip
                Spacer(modifier = Modifier.height(4.dp))
                EventStatusChip(status = event.status ?: "DRAFT")
            }
        }
    }
}

@Composable
fun CategoryChip(categoryName: String) {
    val (backgroundColor, textColor) = when (categoryName) {
        "Âm nhạc" -> Color(0xFFE1F5FE) to Color(0xFF0288D1)
        "Thể thao" -> Color(0xFFE8F5E9) to Color(0xFF43A047)
        "Giáo dục" -> Color(0xFFFFF3E0) to Color(0xFFFF9800)
        "Công nghệ" -> Color(0xFFE0F2F1) to Color(0xFF00897B)
        "Nghệ thuật" -> Color(0xFFF3E5F5) to Color(0xFF8E24AA)
        else -> Color(0xFFF5F5F5) to Color(0xFF757575)
    }
    
    Surface(
        color = backgroundColor,
        contentColor = textColor,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = categoryName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EventStatusChip(status: String) {
    val (backgroundColor, textColor, text) = when (status) {
        "UPCOMING" -> Triple(Color(0xFFE8F5E9), Color(0xFF43A047), "Sắp diễn ra")
        "DRAFT" -> Triple(Color(0xFFF5F5F5), Color(0xFF757575), "Bản nháp")
        "ONGOING" -> Triple(Color(0xFFE1F5FE), Color(0xFF0288D1), "Đang diễn ra")
        "COMPLETED" -> Triple(Color(0xFFEFEBE9), Color(0xFF5D4037), "Đã kết thúc")
        "CANCELLED" -> Triple(Color(0xFFFFEBEE), Color(0xFFE57373), "Đã hủy")
        else -> Triple(Color(0xFFF5F5F5), Color(0xFF757575), "Không xác định")
    }
    
    Surface(
        color = backgroundColor,
        contentColor = textColor,
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// Utility function để format date time từ string
fun formatDateTimeFromString(dateTimeString: String?): String {
    if (dateTimeString.isNullOrEmpty()) return "N/A"
    
    return try {
        // Dự đoán format: "2025-12-25 09:00:00"
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        
        val date = inputFormat.parse(dateTimeString)
        if (date != null) {
            outputFormat.format(date)
        } else {
            dateTimeString
        }
    } catch (e: Exception) {
        dateTimeString
    }
} 