package com.nicha.eventticketing.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun EventPerformanceScreen(
    onBackClick: () -> Unit,
    eventId: String,
    viewModel: AnalyticsDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val eventPerformanceState by viewModel.eventPerformanceState.collectAsState()
    
    // Set event ID for analysis
    LaunchedEffect(eventId) {
        viewModel.updateSelectedEventForDetails(eventId)
        viewModel.loadEventPerformance(eventId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Đánh giá hiệu suất sự kiện",
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
                        viewModel.exportData("event_performance", "pdf")
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
            // Performance Overview
            item {
                PerformanceOverviewSection(
                    eventPerformanceState = eventPerformanceState
                )
            }
            
            // KPI Dashboard
            item {
                KPIDashboardSection(
                    eventPerformanceState = eventPerformanceState
                )
            }
            
            // Success Metrics
            item {
                SuccessMetricsSection(
                    eventPerformanceState = eventPerformanceState
                )
            }
            
            // Marketing Performance
            item {
                MarketingPerformanceSection()
            }
            
            // ROI Analysis
            item {
                ROIAnalysisSection(
                    eventPerformanceState = eventPerformanceState
                )
            }
            
            // Comparative Analysis
            item {
                ComparativeAnalysisSection()
            }
            
            // Recommendations
            item {
                RecommendationsSection(
                    eventPerformanceState = eventPerformanceState
                )
            }
        }
    }
}

@Composable
private fun PerformanceOverviewSection(
    eventPerformanceState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.EventPerformanceResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tổng quan hiệu suất",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (eventPerformanceState) {
                is ResourceState.Loading -> {
                    ShimmerChart(height = 120.dp)
                }
                is ResourceState.Success -> {
                    val data = eventPerformanceState.data
                    val overallScore = calculateOverallScore(data)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Overall Performance Score
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Điểm tổng thể",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            PerformanceScoreCircle(
                                score = overallScore,
                                size = 100.dp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = getPerformanceRating(overallScore),
                                style = MaterialTheme.typography.bodyMedium,
                                color = getPerformanceColor(overallScore)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Key Metrics
                        Column(
                            modifier = Modifier.weight(2f)
                        ) {
                            PerformanceMetricRow(
                                label = "Tỷ lệ bán vé",
                                value = "${data.ticketSalesRate}%",
                                score = data.ticketSalesRate,
                                icon = Icons.Filled.ConfirmationNumber
                            )
                            
                            PerformanceMetricRow(
                                label = "Tỷ lệ tham gia",
                                value = "${data.attendanceRate}%",
                                score = data.attendanceRate,
                                icon = Icons.Filled.Group
                            )
                            
                            PerformanceMetricRow(
                                label = "Đánh giá TB",
                                value = String.format("%.1f/5", data.averageRating),
                                score = (data.averageRating * 20).toInt(),
                                icon = Icons.Filled.Star
                            )
                            
                            PerformanceMetricRow(
                                label = "ROI",
                                value = "${data.roi}%",
                                score = minOf(data.roi, 100),
                                icon = Icons.Filled.TrendingUp
                            )
                        }
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = eventPerformanceState.message,
                        onRetry = { /* Retry logic */ }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun KPIDashboardSection(
    eventPerformanceState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.EventPerformanceResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Bảng điều khiển KPI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (eventPerformanceState) {
                is ResourceState.Success -> {
                    val data = eventPerformanceState.data
                    
                    Column {
                        // Revenue KPIs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            KPICard(
                                title = "Doanh thu",
                                value = formatCurrency(data.totalRevenue),
                                target = formatCurrency(data.revenueTarget ?: 0.0),
                                progress = calculateProgress(data.totalRevenue, data.revenueTarget ?: 0.0),
                                color = Color.Green,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            KPICard(
                                title = "Vé bán",
                                value = data.ticketsSold.toString(),
                                target = data.ticketsTarget?.toString() ?: "0",
                                progress = calculateProgress(data.ticketsSold.toDouble(), data.ticketsTarget?.toDouble() ?: 0.0),
                                color = Color.Blue,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Engagement KPIs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            KPICard(
                                title = "Tham gia",
                                value = "${data.attendanceRate}%",
                                target = "85%",
                                progress = data.attendanceRate / 85.0,
                                color = androidx.compose.ui.graphics.Color(0xFFFF9800),
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            KPICard(
                                title = "Hài lòng",
                                value = String.format("%.1f/5", data.averageRating),
                                target = "4.0/5",
                                progress = data.averageRating / 4.0,
                                color = androidx.compose.ui.graphics.Color(0xFF9C27B0),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is ResourceState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(4) {
                            ShimmerRevenueCard()
                        }
                    }
                }
                else -> {
                    Text("Không có dữ liệu KPI")
                }
            }
        }
    }
}

@Composable
private fun SuccessMetricsSection(
    eventPerformanceState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.EventPerformanceResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Chỉ số thành công",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (eventPerformanceState) {
                is ResourceState.Success -> {
                    val data = eventPerformanceState.data
                    
                    Column {
                        SuccessMetricItem(
                            label = "Tỷ lệ bán hết vé",
                            value = "${data.ticketSalesRate}%",
                            benchmark = "Mục tiêu: 80%",
                            status = if (data.ticketSalesRate >= 80) "success" else if (data.ticketSalesRate >= 60) "warning" else "danger"
                        )
                        
                        SuccessMetricItem(
                            label = "Tỷ lệ check-in",
                            value = "${data.attendanceRate}%",
                            benchmark = "Trung bình ngành: 75%",
                            status = if (data.attendanceRate >= 75) "success" else if (data.attendanceRate >= 60) "warning" else "danger"
                        )
                        
                        SuccessMetricItem(
                            label = "NPS Score",
                            value = "${data.npsScore ?: 45}",
                            benchmark = "Tốt: >50",
                            status = if ((data.npsScore ?: 0) >= 50) "success" else if ((data.npsScore ?: 0) >= 30) "warning" else "danger"
                        )
                        
                        SuccessMetricItem(
                            label = "Chi phí mỗi khách",
                            value = formatCurrency(data.costPerAttendee ?: 150000.0),
                            benchmark = "Ngân sách: 200k",
                            status = if ((data.costPerAttendee ?: 0.0) <= 200000) "success" else "warning"
                        )
                    }
                }
                else -> {
                    Text("Không có dữ liệu chỉ số thành công")
                }
            }
        }
    }
}

@Composable
private fun MarketingPerformanceSection() {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Hiệu quả Marketing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mock marketing data
            Column {
                MarketingChannelItem(
                    channel = "Facebook Ads",
                    reach = "25,400",
                    clicks = "1,850",
                    conversions = "157",
                    cost = "2,500,000",
                    roas = "8.5x"
                )
                
                MarketingChannelItem(
                    channel = "Google Ads",
                    reach = "18,200",
                    clicks = "2,100",
                    conversions = "198",
                    cost = "3,200,000",
                    roas = "6.2x"
                )
                
                MarketingChannelItem(
                    channel = "Email Marketing",
                    reach = "12,000",
                    clicks = "840",
                    conversions = "89",
                    cost = "500,000",
                    roas = "17.8x"
                )
                
                MarketingChannelItem(
                    channel = "Organic Social",
                    reach = "8,500",
                    clicks = "320",
                    conversions = "45",
                    cost = "0",
                    roas = "∞"
                )
            }
        }
    }
}

@Composable
private fun ROIAnalysisSection(
    eventPerformanceState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.EventPerformanceResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Phân tích ROI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (eventPerformanceState) {
                is ResourceState.Success -> {
                    val data = eventPerformanceState.data
                    val totalCost = data.totalCost ?: (data.totalRevenue * 0.7)
                    val profit = data.totalRevenue - totalCost
                    val profitMargin = (profit / data.totalRevenue) * 100
                    
                    Column {
                        // ROI Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ROIMetricCard(
                                title = "Tổng chi phí",
                                value = formatCurrency(totalCost),
                                color = Color.Red,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            ROIMetricCard(
                                title = "Lợi nhuận",
                                value = formatCurrency(profit),
                                color = if (profit > 0) Color.Green else Color.Red,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            ROIMetricCard(
                                title = "ROI",
                                value = "${data.roi}%",
                                color = if (data.roi > 0) Color.Green else Color.Red,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            ROIMetricCard(
                                title = "Margin",
                                value = String.format("%.1f%%", profitMargin),
                                color = if (profitMargin > 20) Color.Green else androidx.compose.ui.graphics.Color(0xFFFF9800),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Cost Breakdown Chart
                        Text(
                            text = "Phân bố chi phí",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        CostBreakdownChart(
                            data = mapOf(
                                "Venue" to (totalCost * 0.4),
                                "Marketing" to (totalCost * 0.25),
                                "Catering" to (totalCost * 0.2),
                                "Staff" to (totalCost * 0.1),
                                "Other" to (totalCost * 0.05)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
                else -> {
                    Text("Không có dữ liệu ROI")
                }
            }
        }
    }
}

@Composable
private fun ComparativeAnalysisSection() {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "So sánh với sự kiện khác",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mock comparative data
            Column {
                ComparisonItem(
                    metric = "Doanh thu",
                    currentValue = "25,500,000",
                    previousValue = "22,100,000",
                    industryAverage = "20,800,000"
                )
                
                ComparisonItem(
                    metric = "Tỷ lệ tham gia",
                    currentValue = "87%",
                    previousValue = "82%",
                    industryAverage = "75%"
                )
                
                ComparisonItem(
                    metric = "NPS Score",
                    currentValue = "68",
                    previousValue = "54",
                    industryAverage = "45"
                )
                
                ComparisonItem(
                    metric = "ROI",
                    currentValue = "245%",
                    previousValue = "198%",
                    industryAverage = "180%"
                )
            }
        }
    }
}

@Composable
private fun RecommendationsSection(
    eventPerformanceState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.EventPerformanceResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Khuyến nghị cải thiện",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (eventPerformanceState) {
                is ResourceState.Success -> {
                    val data = eventPerformanceState.data
                    val recommendations = generateRecommendations(data)
                    
                    Column {
                        recommendations.forEach { recommendation ->
                            RecommendationItem(
                                priority = recommendation.priority,
                                title = recommendation.title,
                                description = recommendation.description,
                                impact = recommendation.impact
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                else -> {
                    Text("Không có khuyến nghị")
                }
            }
        }
    }
}

// Helper Components and Data Classes

data class Recommendation(
    val priority: String,
    val title: String,
    val description: String,
    val impact: String
)

@Composable
private fun PerformanceMetricRow(
    label: String,
    value: String,
    score: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = getPerformanceColor(score),
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            
            LinearProgressIndicator(
                progress = score / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = getPerformanceColor(score)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = getPerformanceColor(score)
        )
    }
}

@Composable
private fun KPICard(
    title: String,
    value: String,
    target: String,
    progress: Double,
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
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = "Mục tiêu: $target",
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = minOf(progress.toFloat(), 1f),
                modifier = Modifier.fillMaxWidth(),
                color = color
            )
        }
    }
}

@Composable
private fun SuccessMetricItem(
    label: String,
    value: String,
    benchmark: String,
    status: String
) {
    val statusColor = when (status) {
        "success" -> Color.Green
        "warning" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        "danger" -> Color.Red
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = benchmark,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
    }
}

@Composable
private fun MarketingChannelItem(
    channel: String,
    reach: String,
    clicks: String,
    conversions: String,
    cost: String,
    roas: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = channel,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MarketingMetric("Reach", reach)
                MarketingMetric("Clicks", clicks)
                MarketingMetric("Conv.", conversions)
                MarketingMetric("Cost", formatCurrencyShort(cost))
                MarketingMetric("ROAS", roas)
            }
        }
    }
}

@Composable
private fun MarketingMetric(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ROIMetricCard(
    title: String,
    value: String,
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
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun ComparisonItem(
    metric: String,
    currentValue: String,
    previousValue: String,
    industryAverage: String
) {
    Column(
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Text(
            text = metric,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ComparisonValue("Hiện tại", currentValue, Color.Blue)
            ComparisonValue("Trước đó", previousValue, Color.Gray)
            ComparisonValue("TB ngành", industryAverage, androidx.compose.ui.graphics.Color(0xFFFF9800))
        }
    }
}

@Composable
private fun ComparisonValue(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.7f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun RecommendationItem(
    priority: String,
    title: String,
    description: String,
    impact: String
) {
    val priorityColor = when (priority.lowercase()) {
        "high" -> Color.Red
        "medium" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        "low" -> Color.Green
        else -> MaterialTheme.colorScheme.primary
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Surface(
                    color = priorityColor.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = priority.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Tác động: $impact",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Helper functions
private fun calculateOverallScore(data: com.nicha.eventticketing.data.remote.dto.analytics.EventPerformanceResponseDto): Int {
    val ticketScore = data.ticketSalesRate
    val attendanceScore = data.attendanceRate
    val ratingScore = (data.averageRating * 20).toInt()
    val roiScore = minOf(data.roi, 100)
    
    return (ticketScore + attendanceScore + ratingScore + roiScore) / 4
}

private fun getPerformanceRating(score: Int): String {
    return when {
        score >= 90 -> "Xuất sắc"
        score >= 80 -> "Tốt"
        score >= 70 -> "Khá"
        score >= 60 -> "Trung bình"
        else -> "Cần cải thiện"
    }
}

private fun getPerformanceColor(score: Int): Color {
    return when {
        score >= 80 -> Color.Green
        score >= 60 -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        else -> Color.Red
    }
}

private fun calculateProgress(current: Double, target: Double): Double {
    return if (target > 0) current / target else 0.0
}

private fun formatCurrency(amount: Double): String {
    return String.format("%,.0f VNĐ", amount)
}

private fun formatCurrencyShort(amount: String): String {
    return try {
        val value = amount.replace(",", "").toDouble()
        when {
            value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000)
            value >= 1_000 -> String.format("%.0fK", value / 1_000)
            else -> amount
        }
    } catch (e: Exception) {
        amount
    }
}

private fun generateRecommendations(data: com.nicha.eventticketing.data.remote.dto.analytics.EventPerformanceResponseDto): List<Recommendation> {
    val recommendations = mutableListOf<Recommendation>()
    
    if (data.ticketSalesRate < 80) {
        recommendations.add(
            Recommendation(
                priority = "High",
                title = "Cải thiện chiến lược bán vé",
                description = "Tỷ lệ bán vé còn thấp. Cần xem xét giá vé, kênh phân phối và chiến lược marketing.",
                impact = "Tăng 15-25% doanh thu"
            )
        )
    }
    
    if (data.attendanceRate < 75) {
        recommendations.add(
            Recommendation(
                priority = "Medium",
                title = "Tăng cường engagement trước sự kiện",
                description = "Gửi email nhắc nhở, tạo buzz trên social media, cung cấp thông tin chi tiết về sự kiện.",
                impact = "Cải thiện 10-15% tỷ lệ tham gia"
            )
        )
    }
    
    if (data.averageRating < 4.0) {
        recommendations.add(
            Recommendation(
                priority = "High",
                title = "Nâng cao chất lượng sự kiện",
                description = "Xem xét feedback từ khách hàng, cải thiện venue, catering, và chương trình.",
                impact = "Tăng customer satisfaction và word-of-mouth"
            )
        )
    }
    
    if (data.roi < 150) {
        recommendations.add(
            Recommendation(
                priority = "Medium",
                title = "Tối ưu hóa chi phí",
                description = "Xem xét lại các khoản chi phí, tìm nhà cung cấp tốt hơn, tối ưu budget allocation.",
                impact = "Cải thiện 20-30% ROI"
            )
        )
    }
    
    return recommendations
}
