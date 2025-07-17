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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.viewmodel.OrganizerEventViewModel

enum class EventStatusFilter {
    ALL, DRAFT, PUBLISHED, CANCELLED, COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerEventListScreen(
    onBackClick: () -> Unit,
    onEventClick: (String) -> Unit,
    onCreateEventClick: () -> Unit,
    onScanQRClick: () -> Unit,
    viewModel: OrganizerEventViewModel = hiltViewModel()
) {
    val organizerEventsState by viewModel.organizerEventsState.collectAsState()
    var selectedStatusFilter by remember { mutableStateOf(EventStatusFilter.ALL) }
    
    // Fetch events when component is first displayed
    LaunchedEffect(Unit) {
        viewModel.getOrganizerEvents()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sự kiện của tôi") },
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
            // Status filter tabs
            ScrollableTabRow(
                selectedTabIndex = selectedStatusFilter.ordinal,
                edgePadding = 16.dp
            ) {
                EventStatusFilter.values().forEachIndexed { index, filter ->
                    Tab(
                        selected = selectedStatusFilter.ordinal == index,
                        onClick = { selectedStatusFilter = filter },
                        text = {
                            Text(
                                when (filter) {
                                    EventStatusFilter.ALL -> "Tất cả"
                                    EventStatusFilter.DRAFT -> "Bản nháp"
                                    EventStatusFilter.PUBLISHED -> "Đã đăng"
                                    EventStatusFilter.CANCELLED -> "Đã hủy"
                                    EventStatusFilter.COMPLETED -> "Đã kết thúc"
                                }
                            )
                        }
                    )
                }
            }
            
            when (organizerEventsState) {
                is ResourceState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ResourceState.Error -> {
                    val errorMessage = (organizerEventsState as ResourceState.Error).message
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
                            Button(onClick = { viewModel.getOrganizerEvents() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                is ResourceState.Success -> {
                    val events = (organizerEventsState as ResourceState.Success<*>).data as? com.nicha.eventticketing.data.remote.dto.event.PageDto<EventDto>
                    
                    val filteredEvents = events?.content?.filter { event ->
                        when (selectedStatusFilter) {
                            EventStatusFilter.ALL -> true
                            EventStatusFilter.DRAFT -> event.status == "DRAFT"
                            EventStatusFilter.PUBLISHED -> event.status == "PUBLISHED"
                            EventStatusFilter.CANCELLED -> event.status == "CANCELLED"
                            EventStatusFilter.COMPLETED -> event.status == "COMPLETED"
                        }
                    } ?: emptyList()
                    
                    if (filteredEvents.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Category,
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
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Button(onClick = onCreateEventClick) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tạo sự kiện mới")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredEvents) { event ->
                                OrganizerEventItem(
                                    event = event,
                                    onEventClick = { onEventClick(event.id) }
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
        }
    }
}

@Composable
fun OrganizerEventItem(
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
            AsyncImage(
                model = event.featuredImageUrl,
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
                    
                    EventStatusChip(status = event.status)
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
                        text = event.startDate,
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
                        text = event.address ?: event.locationName ?: "Chưa cập nhật địa điểm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PeopleAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "${event.currentAttendees ?: 0}/${event.maxAttendees ?: 0} người tham dự",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EventStatusChip(status: String?) {
    val (backgroundColor, contentColor, text) = when (status?.uppercase()) {
        "DRAFT" -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "Bản nháp")
        "PUBLISHED" -> Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "Đã đăng")
        "CANCELLED" -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "Đã hủy")
        "COMPLETED" -> Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), "Đã kết thúc")
        else -> Triple(Color(0xFFEEEEEE), Color(0xFF757575), "Không xác định")
    }
    
    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
} 