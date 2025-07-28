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

    // Set event ID if provided
    LaunchedEffect(eventId) {
        if (eventId != null) {
            viewModel.updateSelectedEventForDetails(eventId)
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

            // Summary Cards
            SummaryCardsSection(
                dailyRevenueState = dailyRevenueState,
                ticketSalesState = ticketSalesState,
                checkInStatsState = checkInStatsState,
                ratingStatsState = ratingStatsState
            )

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
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Revenue Chart
        RevenueLineChart(
            data = when (dailyRevenueState) {
                is ResourceState.Success -> dailyRevenueState.data.dailyRevenue
                else -> emptyMap()
            },
            title = "Doanh thu theo thời gian"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ticket Sales Chart
            TicketSalesBarChart(
                data = when (ticketSalesState) {
                    is ResourceState.Success -> {
                        ticketSalesState.data.ticketTypeBreakdown
                    }
                    else -> emptyMap()
                },
                title = "Bán vé theo loại",
                modifier = Modifier.weight(1f)
            )

            // Check-in Chart
            CheckInPieChart(
                checkedIn = when (checkInStatsState) {
                    is ResourceState.Success -> checkInStatsState.data.checkedIn
                    else -> 0
                },
                notCheckedIn = when (checkInStatsState) {
                    is ResourceState.Success -> checkInStatsState.data.notCheckedIn
                    else -> 0
                },
                title = "Thống kê Check-in",
                modifier = Modifier.weight(1f)
            )
        }

        // Rating Distribution Chart
        RatingDistributionChart(
            ratingCounts = when (ratingStatsState) {
                is ResourceState.Success -> ratingStatsState.data.ratingCounts
                else -> emptyMap()
            },
            averageRating = when (ratingStatsState) {
                is ResourceState.Success -> ratingStatsState.data.averageRating
                else -> 0.0
            },
            title = "Phân bố đánh giá"
        )
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
