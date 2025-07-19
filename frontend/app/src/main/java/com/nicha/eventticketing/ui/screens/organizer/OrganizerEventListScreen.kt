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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.ui.theme.CardBackground
import com.nicha.eventticketing.ui.theme.LocalNeumorphismStyle
import com.nicha.eventticketing.viewmodel.OrganizerEventViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.nicha.eventticketing.ui.components.EventCard
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.nicha.eventticketing.R
import com.nicha.eventticketing.ui.components.EventStatusChip

enum class EventStatusFilter {
    ALL, DRAFT, PUBLISHED, CANCELLED, COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    var searchQuery by remember { mutableStateOf("") }
    
    // Fetch events when component is first displayed
    LaunchedEffect(Unit) {
        viewModel.getOrganizerEvents()
    }
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            "Sự kiện của tôi",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        ) 
                    },
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                NeumorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm kiếm sự kiện...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Xóa",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEventClick,
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Tạo sự kiện mới",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tạo sự kiện",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status filter tabs
            ScrollableTabRow(
                selectedTabIndex = selectedStatusFilter.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
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
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                                    text = "Không thể tải danh sách sự kiện",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.getOrganizerEvents() },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Thử lại")
                                }
                            }
                        }
                    }
                }
                is ResourceState.Success -> {
                    val events = (organizerEventsState as ResourceState.Success<*>).data as? com.nicha.eventticketing.data.remote.dto.event.PageDto<EventDto>
                    
                    val filteredEvents = events?.content?.filter { event ->
                        val matchesStatus = when (selectedStatusFilter) {
                            EventStatusFilter.ALL -> true
                            EventStatusFilter.DRAFT -> event.status == "DRAFT"
                            EventStatusFilter.PUBLISHED -> event.status == "PUBLISHED"
                            EventStatusFilter.CANCELLED -> event.status == "CANCELLED"
                            EventStatusFilter.COMPLETED -> event.status == "COMPLETED"
                        }
                        
                        val matchesSearch = if (searchQuery.isEmpty()) {
                            true
                        } else {
                            event.title.contains(searchQuery, ignoreCase = true) ||
                            event.description.contains(searchQuery, ignoreCase = true) ||
                            event.locationName.contains(searchQuery, ignoreCase = true)
                        }
                        
                        matchesStatus && matchesSearch
                    } ?: emptyList()
                    
                    if (filteredEvents.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = if (searchQuery.isNotEmpty()) {
                                            "Không tìm thấy sự kiện phù hợp"
                                        } else {
                                            when (selectedStatusFilter) {
                                                EventStatusFilter.ALL -> "Không có sự kiện nào"
                                                EventStatusFilter.DRAFT -> "Không có bản nháp sự kiện"
                                                EventStatusFilter.PUBLISHED -> "Không có sự kiện đang diễn ra"
                                                EventStatusFilter.CANCELLED -> "Không có sự kiện đã hủy"
                                                EventStatusFilter.COMPLETED -> "Không có sự kiện đã kết thúc"
                                            }
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = onCreateEventClick,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
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
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = filteredEvents,
                                key = { it.id }
                            ) { event ->
                                EventCard(
                                    event = event,
                                    onEventClick = { onEventClick(event.id) }
                                )
                            }
                            
                            // Thêm khoảng trống ở cuối để tránh FAB che nội dung
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
} 