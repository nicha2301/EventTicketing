package com.nicha.eventticketing.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.analytics.*
import com.nicha.eventticketing.ui.components.charts.*
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.viewmodel.AnalyticsDashboardViewModel
import com.nicha.eventticketing.viewmodel.ExportState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    onBackClick: () -> Unit,
    onNavigateToDetailed: (String) -> Unit = {},
    eventId: String? = null,
    viewModel: AnalyticsDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dailyRevenueState by viewModel.dailyRevenueState.collectAsState()
    val ticketSalesState by viewModel.ticketSalesState.collectAsState()
    val checkInStatsState by viewModel.checkInStatsState.collectAsState()
    val ratingStatsState by viewModel.ratingStatsState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    
    // Export dialog states
    var showExportDialog by remember { mutableStateOf(false) }
    var showExportSuccess by remember { mutableStateOf(false) }
    var showExportError by remember { mutableStateOf(false) }
    var exportedFile by remember { mutableStateOf<java.io.File?>(null) }
    var exportErrorMessage by remember { mutableStateOf("") }

    // Set event ID if provided
    LaunchedEffect(eventId) {
        if (eventId != null) {
            viewModel.updateSelectedEventForDetails(eventId)
        }
    }
    
    // Handle export state changes
    LaunchedEffect(exportState) {
        when (val currentExportState = exportState) {
            is ExportState.Success -> {
                showExportDialog = false
                exportedFile = currentExportState.file
                showExportSuccess = true
                viewModel.clearExportState()
            }
            is ExportState.Error -> {
                showExportDialog = false
                exportErrorMessage = currentExportState.message
                showExportError = true
                viewModel.clearExportState()
            }
            else -> {}
        }
    }

    // Handle export message
    uiState.exportMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or handle export message
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Analytics Dashboard",
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
                    // Export button
                    IconButton(
                        onClick = { showExportDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export Report"
                        )
                    }
                    
                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refreshData() },
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Làm mới"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter Section
            AnalyticsFilter(
                selectedDateRange = uiState.selectedDateRange,
                selectedPeriod = uiState.selectedPeriod,
                selectedEvents = uiState.selectedEvents,
                availableEvents = emptyList(), // TODO: Load from repository
                onDateRangeChange = { dateRange ->
                    viewModel.updateDateRange(dateRange.first, dateRange.second)
                },
                onPeriodChange = { period ->
                    viewModel.updateSelectedPeriod(period)
                },
                onEventsChange = { events ->
                    viewModel.updateSelectedEvents(events)
                },
                onExportClick = {
                    viewModel.exportData()
                }
            )

            // Summary Cards with loading states
            when {
                dailyRevenueState is ResourceState.Loading ||
                ticketSalesState is ResourceState.Loading ||
                checkInStatsState is ResourceState.Loading ||
                ratingStatsState is ResourceState.Loading -> {
                    SummaryCardSkeleton()
                }
                else -> {
                    SummaryCardsSection(
                        dailyRevenueState = dailyRevenueState,
                        ticketSalesState = ticketSalesState,
                        checkInStatsState = checkInStatsState,
                        ratingStatsState = ratingStatsState
                    )
                }
            }

            // Charts Section
            ChartsSection(
                dailyRevenueState = dailyRevenueState,
                ticketSalesState = ticketSalesState,
                checkInStatsState = checkInStatsState,
                ratingStatsState = ratingStatsState
            )

            // Quick Actions
            QuickActionsSection(
                onNavigateToDetailed = onNavigateToDetailed
            )
        }
    }
}

@Composable
private fun SummaryCardsSection(
    dailyRevenueState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.DailyRevenueResponseDto>,
    ticketSalesState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.TicketSalesResponseDto>,
    checkInStatsState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.CheckInStatisticsDto>,
    ratingStatsState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.RatingStatisticsDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tổng quan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Revenue Card
                RevenueCard(
                    title = "Doanh thu",
                    amount = when (dailyRevenueState) {
                        is ResourceState.Success -> {
                            formatCurrency(dailyRevenueState.data.totalRevenue)
                        }
                        is ResourceState.Loading -> "..."
                        else -> "0"
                    },
                    currency = "VND",
                    modifier = Modifier.weight(1f)
                )

                // Tickets Sold Card
                StatsCard(
                    title = "Vé đã bán",
                    value = when (ticketSalesState) {
                        is ResourceState.Success -> ticketSalesState.data.ticketTypeBreakdown.values.sum().toString()
                        is ResourceState.Loading -> "..."
                        else -> "0"
                    },
                    icon = Icons.Default.ConfirmationNumber,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Check-in Rate Card
                PercentageCard(
                    title = "Tỷ lệ check-in",
                    percentage = when (checkInStatsState) {
                        is ResourceState.Success -> checkInStatsState.data.checkInRate * 100
                        else -> 0.0
                    },
                    description = when (checkInStatsState) {
                        is ResourceState.Success -> {
                            "${checkInStatsState.data.checkedIn}/${checkInStatsState.data.totalTickets} vé"
                        }
                        else -> null
                    },
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )

                // Rating Card
                StatsCard(
                    title = "Đánh giá TB",
                    value = when (ratingStatsState) {
                        is ResourceState.Success -> String.format("%.1f", ratingStatsState.data.averageRating)
                        is ResourceState.Loading -> "..."
                        else -> "0.0"
                    },
                    subtitle = when (ratingStatsState) {
                        is ResourceState.Success -> "${ratingStatsState.data.totalRatings} đánh giá"
                        else -> null
                    },
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFFFFB000),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ChartsSection(
    dailyRevenueState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.DailyRevenueResponseDto>,
    ticketSalesState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.TicketSalesResponseDto>,
    checkInStatsState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.CheckInStatisticsDto>,
    ratingStatsState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.RatingStatisticsDto>
) {
    // Optimized with remember to avoid recomposition
    val revenueData = remember(dailyRevenueState) {
        when (dailyRevenueState) {
            is ResourceState.Success -> dailyRevenueState.data.dailyRevenue
            else -> emptyMap()
        }
    }
    
    val ticketSalesData = remember(ticketSalesState) {
        when (ticketSalesState) {
            is ResourceState.Success -> ticketSalesState.data.ticketTypeBreakdown
            else -> emptyMap()
        }
    }
    
    val (checkedIn, totalTickets) = remember(checkInStatsState) {
        when (checkInStatsState) {
            is ResourceState.Success -> Pair(checkInStatsState.data.checkedIn, checkInStatsState.data.totalTickets)
            else -> Pair(0, 0)
        }
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Revenue Chart with optimized rendering
        if (revenueData.isNotEmpty()) {
            RevenueLineChart(
                data = revenueData,
                title = "Doanh thu theo thời gian"
            )
        } else {
            ChartPlaceholder(
                title = "Doanh thu theo thời gian",
                isLoading = dailyRevenueState is ResourceState.Loading
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ticket Sales Chart with conditional rendering
            if (ticketSalesData.isNotEmpty()) {
                TicketSalesBarChart(
                    data = ticketSalesData,
                    title = "Bán vé theo loại",
                    modifier = Modifier.weight(1f)
                )
            } else {
                ChartPlaceholder(
                    title = "Bán vé theo loại",
                    isLoading = ticketSalesState is ResourceState.Loading,
                    modifier = Modifier.weight(1f)
                )
            }

            // Check-in Chart with optimized data
            if (totalTickets > 0) {
                CheckInPieChart(
                    checkedIn = checkedIn,
                    notCheckedIn = totalTickets - checkedIn,
                    title = "Thống kê Check-in",
                    modifier = Modifier.weight(1f)
                )
            } else {
                ChartPlaceholder(
                    title = "Thống kê Check-in",
                    isLoading = checkInStatsState is ResourceState.Loading,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Rating Distribution Chart with optimized rendering
        val ratingData = remember(ratingStatsState) {
            when (ratingStatsState) {
                is ResourceState.Success -> ratingStatsState.data.ratingCounts
                else -> emptyMap()
            }
        }
        
        val averageRating = remember(ratingStatsState) {
            when (ratingStatsState) {
                is ResourceState.Success -> ratingStatsState.data.averageRating
                else -> 0.0
            }
        }
        
        if (ratingData.isNotEmpty()) {
            RatingDistributionChart(
                ratingCounts = ratingData,
                averageRating = averageRating,
                title = "Phân bố đánh giá"
            )
        } else {
            ChartPlaceholder(
                title = "Phân bố đánh giá",
                isLoading = ratingStatsState is ResourceState.Loading
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNavigateToDetailed: (String) -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Xem chi tiết",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { onNavigateToDetailed("revenue") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Doanh thu")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { onNavigateToDetailed("tickets") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Vé bán")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { onNavigateToDetailed("attendees") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Check-in")
                }
            }
        }
    }
}

// Helper function to format currency
private fun formatCurrency(amount: Double): String {
    return when {
        amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000)
        amount >= 1_000 -> String.format("%.0fK", amount / 1_000)
        else -> String.format("%.0f", amount)
    }
}

// Performance optimized chart placeholder
@Composable
private fun ChartPlaceholder(
    title: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    NeumorphicCard(
        modifier = modifier.height(200.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Đang tải...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chưa có dữ liệu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
