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
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.viewmodel.OrganizerEventViewModel
import com.nicha.eventticketing.ui.components.EventCard
import com.nicha.eventticketing.ui.components.DashboardStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDashboardScreen(
    onBackClick: () -> Unit,
    onEventClick: (String) -> Unit,
    onCreateEventClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
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
                title = { 
                    Text(
                        "Quản lý sự kiện",
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
                    IconButton(onClick = onAnalyticsClick) {
                        Icon(
                            imageVector = Icons.Filled.Analytics,
                            contentDescription = "Thống kê và báo cáo"
                        )
                    }
                    IconButton(onClick = onScanQRClick) {
                        Icon(
                            imageVector = Icons.Filled.QrCode2,
                            contentDescription = "Quét mã QR"
                        )
                    }
                    IconButton(onClick = { onEventClick("profile") }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Hồ sơ nhà tổ chức"
                        )
                }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEventClick,
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
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
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                        DashboardStatCard(
                            icon = Icons.Filled.CalendarToday,
                            value = "${activeEvents.size}",
                            label = "Sự kiện đang diễn ra"
                        )
                        
                        DashboardStatCard(
                            icon = Icons.Filled.PeopleAlt,
                            value = "$totalAttendees",
                            label = "Người tham dự"
                        )
                        
                        DashboardStatCard(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            value = "$totalTicketsSold",
                            label = "Vé đã bán"
                        )
                        
                        Box(
                            modifier = Modifier.clickable { onAnalyticsClick() }
                        ) {
                            DashboardStatCard(
                                icon = Icons.Default.Analytics,
                                value = "📊",
                                label = "Thống kê"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { onEventClick("list") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tất cả sự kiện")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = onCreateEventClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tạo sự kiện")
                        }
                    }
                }
            }
            
            // Event tabs with custom styling
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Đang diễn ra (${activeEvents.size})") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Đã kết thúc (${pastEvents.size})") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Bản nháp (${draftEvents.size})") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Không thể tải danh sách sự kiện",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = (organizerEventsState as ResourceState.Error).message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { viewModel.getOrganizerEvents() },
                                shape = RoundedCornerShape(8.dp)
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
                is ResourceState.Success -> {
                    val eventsToShow = when (selectedTabIndex) {
                        0 -> activeEvents
                        1 -> pastEvents
                        2 -> draftEvents
                        else -> emptyList()
                    }
                    
                    if (eventsToShow.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = when (selectedTabIndex) {
                                        0 -> "Không có sự kiện đang diễn ra"
                                        1 -> "Không có sự kiện đã kết thúc"
                                        else -> "Không có bản nháp sự kiện"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = onCreateEventClick,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null
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
                            items(eventsToShow) { event ->
                                EventCard(
                                    event = event,
                                    onEventClick = { onEventClick(event.id) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun DashboardStat(
    icon: ImageVector,
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 