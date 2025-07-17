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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nicha.eventticketing.data.model.EventType
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.viewmodel.OrganizerEventViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    val organizerEventsState = viewModel.organizerEventsState.collectAsState().value
    
    var activeEvents by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var pastEvents by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var draftEvents by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    
    var totalAttendees by remember { mutableStateOf(0) }
    var totalTicketsSold by remember { mutableStateOf(0) }
    
    LaunchedEffect(key1 = true) {
        viewModel.getOrganizerEvents()
    }
    
    when (organizerEventsState) {
        is ResourceState.Success -> {
            val events = organizerEventsState.data.content
            
            activeEvents = events.filter { it.status == "PUBLISHED" }
            pastEvents = events.filter { it.status == "COMPLETED" || it.status == "CANCELLED" }
            draftEvents = events.filter { it.status == "DRAFT" }
            
            totalAttendees = events.sumOf { it.currentAttendees ?: 0 }
            totalTicketsSold = events.flatMap { it.ticketTypes ?: emptyList() }.sumOf { it.quantitySold ?: 0 }
        }
        is ResourceState.Error -> {
        }
        is ResourceState.Loading -> {
        }
        else -> {}
    }
    
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
            
            // Show loading or event lists
            when (organizerEventsState) {
                is ResourceState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ResourceState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không thể tải danh sách sự kiện: ${(organizerEventsState as ResourceState.Error).message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is ResourceState.Success -> {
                    // Event list
                    when (selectedTabIndex) {
                        0 -> EventList(events = activeEvents, onEventClick = onEventClick)
                        1 -> EventList(events = pastEvents, onEventClick = onEventClick)
                        2 -> EventList(events = draftEvents, onEventClick = onEventClick)
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Button to view all events
            Button(
                onClick = { onEventClick("list") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text("Xem tất cả sự kiện")
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
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun EventList(
    events: List<EventDto>,
    onEventClick: (String) -> Unit
) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Không có sự kiện nào",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val eventStartDate = event.startDate?.let { if (it.contains("T")) LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) else null }
    
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
            AsyncImage(
                model = event.featuredImageUrl?.let { "https://eventticketing.epark.vn/api/files/$it" } ?: "https://picsum.photos/id/1/300/200",
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(120.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            
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
                    
                    CategoryChip(event.categoryName ?: "Khác")
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
                        text = if (eventStartDate != null) "${eventStartDate.format(dateFormatter)} ${eventStartDate.format(timeFormatter)}" else event.startDate ?: "N/A",
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
                        text = event.locationName ?: event.address ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val priceText = when {
                    event.isFree == true -> "Miễn phí"
                    event.minTicketPrice != null -> "${event.minTicketPrice.toInt().toString().replace(Regex("\\B(?=(\\d{3})+(?!\\d))"), ".")} VND"
                    else -> "N/A"
                }
                
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CategoryChip(categoryName: String) {
    // Màu dựa trên tên danh mục
    val (backgroundColor, textColor) = when (categoryName) {
        "Âm nhạc" -> Pair(Color(0xFFE1F5FE), Color(0xFF0288D1))
        "Thể thao" -> Pair(Color(0xFFE8F5E9), Color(0xFF43A047))
        "Giáo dục" -> Pair(Color(0xFFFFF3E0), Color(0xFFFF9800))
        "Công nghệ" -> Pair(Color(0xFFE0F2F1), Color(0xFF00897B))
        "Nghệ thuật" -> Pair(Color(0xFFF3E5F5), Color(0xFF8E24AA))
        else -> Pair(Color(0xFFF5F5F5), Color(0xFF757575))
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