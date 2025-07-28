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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueAnalyticsScreen(
    onBackClick: () -> Unit,
    eventId: String? = null,
    viewModel: AnalyticsDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dailyRevenueState by viewModel.dailyRevenueState.collectAsState()
    
    var selectedPeriod by remember { mutableStateOf("Daily") }
    val periods = listOf("Daily", "Weekly", "Monthly")
    
    // Set event ID if provided
    LaunchedEffect(eventId) {
        if (eventId != null) {
            viewModel.updateSelectedEventForDetails(eventId)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Phân tích doanh thu",
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
                // Period selector
                var expanded by remember { mutableStateOf(false) }
                
                TextButton(
                    onClick = { expanded = true }
                ) {
                    Text(selectedPeriod)
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Chọn kỳ"
                    )
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    periods.forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period) },
                            onClick = {
                                selectedPeriod = period
                                expanded = false
                                // Update data based on selected period
                                eventId?.let { id ->
                                    viewModel.loadTicketSalesData(id)
                                }
                            }
                        )
                    }
                }
                
                // Export button
                IconButton(
                    onClick = { 
                        viewModel.exportData("revenue", "pdf")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.FileDownload,
                        contentDescription = "Xuất báo cáo"
                    )
                }
            }
        )

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Range Filter
            item {
                DateRangeFilterCard(
                    startDate = java.time.LocalDate.now().minusDays(30).toString(),
                    endDate = java.time.LocalDate.now().toString(),
                    onDateRangeChanged = { start, end ->
                        viewModel.updateDateRange(start, end)
                    }
                )
            }
            
            // Revenue Summary Cards
            item {
                RevenueSummarySection(
                    dailyRevenueState = dailyRevenueState,
                    selectedPeriod = selectedPeriod
                )
            }
            
            // Revenue Trend Chart
            item {
                RevenueTrendChart(
                    dailyRevenueState = dailyRevenueState,
                    period = selectedPeriod
                )
            }
            
            // Revenue Breakdown
            item {
                RevenueBreakdownSection(
                    dailyRevenueState = dailyRevenueState
                )
            }
            
            // Payment Methods Analysis
            item {
                PaymentMethodsAnalysis(
                    // This would need additional API endpoint
                )
            }
            
            // Revenue Forecast
            item {
                RevenueForecastSection(
                    dailyRevenueState = dailyRevenueState
                )
            }
        }
    }
}

@Composable
private fun RevenueSummarySection(
    dailyRevenueState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.DailyRevenueResponseDto>,
    selectedPeriod: String
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tổng quan doanh thu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (dailyRevenueState) {
                is ResourceState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(3) {
                            ShimmerRevenueCard()
                        }
                    }
                }
                is ResourceState.Success -> {
                    val data = dailyRevenueState.data
                    val totalRevenue = data.totalRevenue
                    val dailyRevenue = data.dailyRevenue
                    
                    // Calculate metrics
                    val avgDailyRevenue = if (dailyRevenue.isNotEmpty()) {
                        totalRevenue / dailyRevenue.size
                    } else 0.0
                    
                    val growth = calculateGrowth(dailyRevenue)
                    val peakDay = dailyRevenue.maxByOrNull { it.value }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RevenueCard(
                            title = "Tổng doanh thu",
                            amount = formatCurrency(totalRevenue),
                            currency = data.currencyCode,
                            growth = growth,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        RevenueCard(
                            title = "TB ${selectedPeriod.lowercase()}",
                            amount = formatCurrency(avgDailyRevenue),
                            currency = data.currencyCode,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        RevenueCard(
                            title = "Ngày cao nhất",
                            amount = formatCurrency(peakDay?.value ?: 0.0),
                            currency = data.currencyCode,
                            subtitle = peakDay?.key?.let { formatDate(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = dailyRevenueState.message,
                        onRetry = { /* Retry logic */ }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun RevenueTrendChart(
    dailyRevenueState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.DailyRevenueResponseDto>,
    period: String
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xu hướng doanh thu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (dailyRevenueState) {
                is ResourceState.Loading -> {
                    ShimmerChart(height = 200.dp)
                }
                is ResourceState.Success -> {
                    RevenueLineChart(
                        data = dailyRevenueState.data.dailyRevenue,
                        title = "Doanh thu theo $period",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = "Không thể tải biểu đồ",
                        onRetry = { /* Retry logic */ }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun RevenueBreakdownSection(
    dailyRevenueState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.DailyRevenueResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Phân tích chi tiết",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (dailyRevenueState) {
                is ResourceState.Success -> {
                    val data = dailyRevenueState.data.dailyRevenue
                    
                    // Top performing days
                    val topDays = data.toList()
                        .sortedByDescending { it.second }
                        .take(5)
                    
                    Text(
                        text = "Top 5 ngày doanh thu cao nhất",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    topDays.forEachIndexed { index, (date, revenue) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(24.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = when (index) {
                                        0 -> Color(0xFFFFD700) // Gold
                                        1 -> Color(0xFFC0C0C0) // Silver
                                        2 -> Color(0xFFCD7F32) // Bronze
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = formatDate(date),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Text(
                                text = formatCurrency(revenue),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                else -> {
                    Text("Không có dữ liệu để hiển thị")
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodsAnalysis() {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Phương thức thanh toán",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // This would require additional API endpoints
            Text(
                text = "Tính năng sẽ được thêm trong phiên bản tiếp theo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RevenueForecastSection(
    dailyRevenueState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.DailyRevenueResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Dự báo doanh thu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (dailyRevenueState) {
                is ResourceState.Success -> {
                    val data = dailyRevenueState.data.dailyRevenue
                    val forecast = calculateSimpleForecast(data)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ForecastCard(
                            title = "Tuần tới",
                            amount = formatCurrency(forecast.nextWeek),
                            trend = if (forecast.weeklyGrowth > 0) "Tăng" else "Giảm",
                            trendColor = if (forecast.weeklyGrowth > 0) Color.Green else Color.Red,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        ForecastCard(
                            title = "Tháng tới",
                            amount = formatCurrency(forecast.nextMonth),
                            trend = if (forecast.monthlyGrowth > 0) "Tăng" else "Giảm",
                            trendColor = if (forecast.monthlyGrowth > 0) Color.Green else Color.Red,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> {
                    Text("Không đủ dữ liệu để dự báo")
                }
            }
        }
    }
}

// Helper functions
private fun calculateGrowth(dailyRevenue: Map<String, Double>): Double {
    if (dailyRevenue.size < 2) return 0.0
    
    val sortedData = dailyRevenue.toList().sortedBy { it.first }
    val firstHalf = sortedData.take(sortedData.size / 2).sumOf { it.second }
    val secondHalf = sortedData.drop(sortedData.size / 2).sumOf { it.second }
    
    return if (firstHalf > 0) ((secondHalf - firstHalf) / firstHalf) * 100 else 0.0
}

private fun formatCurrency(amount: Double): String {
    return String.format("%,.0f", amount)
}

private fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("dd/MM"))
    } catch (e: Exception) {
        dateString
    }
}

data class ForecastData(
    val nextWeek: Double,
    val nextMonth: Double,
    val weeklyGrowth: Double,
    val monthlyGrowth: Double
)

private fun calculateSimpleForecast(dailyRevenue: Map<String, Double>): ForecastData {
    if (dailyRevenue.isEmpty()) {
        return ForecastData(0.0, 0.0, 0.0, 0.0)
    }
    
    val avgDaily = dailyRevenue.values.average()
    val recentTrend = calculateGrowth(dailyRevenue) / 100
    
    return ForecastData(
        nextWeek = avgDaily * 7 * (1 + recentTrend),
        nextMonth = avgDaily * 30 * (1 + recentTrend),
        weeklyGrowth = recentTrend,
        monthlyGrowth = recentTrend
    )
}
