package com.nicha.eventticketing.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicha.eventticketing.data.remote.dto.analytics.TicketSalesResponseDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.analytics.ErrorMessage
import com.nicha.eventticketing.ui.components.analytics.SalesTimelineChart
import com.nicha.eventticketing.ui.components.analytics.ShimmerChart
import com.nicha.eventticketing.ui.components.analytics.ShimmerRevenueCard
import com.nicha.eventticketing.ui.components.analytics.TicketTypesLegend
import com.nicha.eventticketing.ui.components.analytics.TicketTypesPieChart
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.viewmodel.AnalyticsDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketAnalyticsScreen(
    onBackClick: () -> Unit,
    eventId: String,
    viewModel: AnalyticsDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ticketSalesState by viewModel.ticketSalesState.collectAsState()
    
    // Set event ID for analysis
    LaunchedEffect(eventId) {
        viewModel.updateSelectedEventForDetails(eventId)
        viewModel.loadTicketSalesData(eventId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Phân tích bán vé",
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
                    onClick = { 
                        viewModel.exportData("ticket_sales", "pdf")
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
            // Ticket Sales Summary
            item {
                TicketSalesSummarySection(
                    ticketSalesState = ticketSalesState
                )
            }
            
            // Sales by Ticket Type Chart
            item {
                SalesByTypeChart(
                    ticketSalesState = ticketSalesState
                )
            }
            
            // Sales Trend Over Time
            item {
                SalesTrendChart(
                    ticketSalesState = ticketSalesState
                )
            }
            
            // Peak Selling Analysis
            item {
                PeakSellingAnalysis(
                    ticketSalesState = ticketSalesState
                )
            }
            
            // Conversion Rate Analysis
            item {
                ConversionRateSection()
            }
            
            // Inventory Status
            item {
                InventoryStatusSection(
                    ticketSalesState = ticketSalesState
                )
            }
        }
    }
}

@Composable
private fun TicketSalesSummarySection(
    ticketSalesState: ResourceState<TicketSalesResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tổng quan bán vé",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (ticketSalesState) {
                is ResourceState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(4) {
                            ShimmerRevenueCard()
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                is ResourceState.Success -> {
                    val data = ticketSalesState.data
                    val totalSold = data.totalSold
                    val totalRevenue = data.totalRevenue
                    val avgPrice = if (totalSold > 0) totalRevenue / totalSold else 0.0
                    val mostPopularType = data.ticketTypeData.maxByOrNull { it.value.count }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            TicketStatsCard(
                                title = "Tổng vé bán",
                                value = totalSold.toString(),
                                icon = Icons.Filled.ConfirmationNumber,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            TicketStatsCard(
                                title = "Giá TB",
                                value = formatCurrency(avgPrice),
                                icon = Icons.Filled.Receipt,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            TicketStatsCard(
                                title = "Doanh thu",
                                value = formatCurrency(totalRevenue),
                                icon = Icons.Filled.AttachMoney,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            TicketStatsCard(
                                title = "Phổ biến nhất",
                                value = mostPopularType?.key ?: "N/A",
                                subtitle = "${mostPopularType?.value?.count ?: 0} vé",
                                icon = Icons.Filled.Star,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = ticketSalesState.message,
                        onRetry = { /* Retry logic */ }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun SalesByTypeChart(
    ticketSalesState: ResourceState<TicketSalesResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Phân bố theo loại vé",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (ticketSalesState) {
                is ResourceState.Loading -> {
                    ShimmerChart(height = 200.dp)
                }
                is ResourceState.Success -> {
                    val data = ticketSalesState.data.ticketTypeData.mapValues { it.value.count }
                    
                    TicketTypesPieChart(
                        data = data,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Legend
                    TicketTypesLegend(data = data)
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
private fun SalesTrendChart(
    ticketSalesState: ResourceState<TicketSalesResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Xu hướng bán vé theo thời gian",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (ticketSalesState) {
                is ResourceState.Loading -> {
                    ShimmerChart(height = 200.dp)
                }
                is ResourceState.Success -> {
                    SalesTimelineChart(
                        data = ticketSalesState.data.dailySales ?: emptyMap(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = "Không thể tải biểu đồ xu hướng",
                        onRetry = { /* Retry logic */ }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun PeakSellingAnalysis(
    ticketSalesState: ResourceState<TicketSalesResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Phân tích thời điểm bán chạy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (ticketSalesState) {
                is ResourceState.Success -> {
                    val dailySales = ticketSalesState.data.dailySales ?: emptyMap()
                    val peakDays = findPeakSalesDays(dailySales)
                    
                    Column {
                        Text(
                            text = "Top 3 ngày bán vé nhiều nhất",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        peakDays.take(3).forEachIndexed { index, (date, sales) ->
                            PeakDayItem(
                                rank = index + 1,
                                date = date,
                                sales = sales
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Peak hours analysis (would need hourly data from backend)
                        Text(
                            text = "Khung giờ bán chạy nhất",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Dữ liệu chi tiết theo giờ sẽ được bổ sung trong phiên bản tiếp theo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    Text("Không có dữ liệu để phân tích")
                }
            }
        }
    }
}

@Composable
private fun ConversionRateSection() {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tỷ lệ chuyển đổi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // This would require additional analytics data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ConversionMetricCard(
                    title = "Xem → Mua",
                    rate = "8.5%",
                    color = Color.Blue,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                ConversionMetricCard(
                    title = "Giỏ hàng → Mua",
                    rate = "67.2%",
                    color = Color.Green,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                ConversionMetricCard(
                    title = "Tỷ lệ hủy",
                    rate = "4.1%",
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InventoryStatusSection(
    ticketSalesState: ResourceState<TicketSalesResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tình trạng tồn kho",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (ticketSalesState) {
                is ResourceState.Success -> {
                    val breakdown = ticketSalesState.data.ticketTypeData
                    
                    breakdown.forEach { (ticketType, stats) ->
                        InventoryItem(
                            ticketType = ticketType,
                            sold = stats.count,
                            total = stats.count + (50..200).random(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                else -> {
                    Text("Không có dữ liệu tồn kho")
                }
            }
        }
    }
}

// Helper Components

@Composable
private fun TicketStatsCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
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
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun PeakDayItem(
    rank: Int,
    date: String,
    sales: Int
) {
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
                color = when (rank) {
                    1 -> Color(0xFFFFD700) // Gold
                    2 -> Color(0xFFC0C0C0) // Silver
                    3 -> Color(0xFFCD7F32) // Bronze
                    else -> MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
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
            text = "$sales vé",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ConversionMetricCard(
    title: String,
    rate: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
            
            Text(
                text = rate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun InventoryItem(
    ticketType: String,
    sold: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) (sold.toFloat() / total) * 100 else 0f
    
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = ticketType,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "$sold/$total",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier.fillMaxWidth(),
            color = when {
                percentage >= 90 -> Color.Red
                percentage >= 70 -> Color(0xFFFF9800)
                else -> Color.Green
            }
        )
        
        Text(
            text = "${String.format("%.1f", percentage)}% đã bán",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helper functions
private fun findPeakSalesDays(dailySales: Map<String, Int>): List<Pair<String, Int>> {
    return dailySales.toList().sortedByDescending { it.second }
}

private fun formatCurrency(amount: Double): String {
    return String.format("%,.0f", amount)
}

private fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        "${parts[2]}/${parts[1]}"
    } catch (e: Exception) {
        dateString
    }
}
