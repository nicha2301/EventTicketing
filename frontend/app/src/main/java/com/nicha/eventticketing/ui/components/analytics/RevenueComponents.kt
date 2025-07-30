package com.nicha.eventticketing.ui.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nicha.eventticketing.data.remote.dto.analytics.DailyRevenueResponseDto
import com.nicha.eventticketing.data.remote.dto.analytics.EventPerformanceResponseDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.charts.*
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import java.text.NumberFormat
import java.util.*

@Composable
fun RevenueOverviewSection(
    dailyRevenueState: ResourceState<DailyRevenueResponseDto>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tổng quan doanh thu",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            when (dailyRevenueState) {
                is ResourceState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ResourceState.Success -> {
                    val data = dailyRevenueState.data
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                    val totalRevenue = data.dailyRevenue?.values?.sum() ?: 0.0
                    val avgDaily = if (data.dailyRevenue?.isNotEmpty() == true) {
                        totalRevenue / data.dailyRevenue.size
                    } else 0.0
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            RevenueCard(
                                title = "Tổng doanh thu",
                                value = currencyFormat.format(totalRevenue),
                                icon = Icons.Filled.AttachMoney,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        item {
                            RevenueCard(
                                title = "Trung bình/ngày",
                                value = currencyFormat.format(avgDaily),
                                icon = Icons.Filled.TrendingUp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        item {
                            RevenueCard(
                                title = "Số ngày",
                                value = (data.dailyRevenue?.size ?: 0).toString(),
                                icon = Icons.Filled.DateRange,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = dailyRevenueState.message,
                        onRetry = { /* Handle retry */ }
                    )
                }
                else -> {
                    Text("Chưa có dữ liệu")
                }
            }
        }
    }
}

@Composable
fun RevenueTrendChart(
    dailyRevenueState: ResourceState<DailyRevenueResponseDto>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Xu hướng doanh thu",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            when (dailyRevenueState) {
                is ResourceState.Loading -> {
                    LoadingChart()
                }
                is ResourceState.Success -> {
                    val data = dailyRevenueState.data
                    
                    if (data.dailyRevenue?.isNotEmpty() == true) {
                        LineChart(
                            data = data.dailyRevenue.mapValues { it.value.toFloat() },
                            modifier = Modifier.height(250.dp),
                            lineColor = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        EmptyStateMessage("Chưa có dữ liệu xu hướng doanh thu")
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = dailyRevenueState.message,
                        onRetry = { /* Handle retry */ }
                    )
                }
                else -> {
                    Text("Chưa có dữ liệu")
                }
            }
        }
    }
}

@Composable
fun EventPerformanceOverview(
    eventPerformanceState: ResourceState<EventPerformanceResponseDto>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Hiệu suất sự kiện",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            when (eventPerformanceState) {
                is ResourceState.Loading -> {
                    LoadingPerformanceCards()
                }
                is ResourceState.Success -> {
                    val data = eventPerformanceState.data
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ROI and Revenue
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PerformanceMetricCard(
                                title = "ROI",
                                value = "${data.roi}%",
                                icon = Icons.Filled.TrendingUp,
                                color = if (data.roi >= 100) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            PerformanceMetricCard(
                                title = "Doanh thu",
                                value = currencyFormat.format(data.totalRevenue),
                                icon = Icons.Filled.AttachMoney,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // Rates
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PerformanceMetricCard(
                                title = "Tỷ lệ bán vé",
                                value = "${data.ticketSalesRate}%",
                                icon = Icons.Filled.ConfirmationNumber,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            PerformanceMetricCard(
                                title = "Tỷ lệ tham dự",
                                value = "${data.attendanceRate}%",
                                icon = Icons.Filled.People,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // Rating and NPS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PerformanceMetricCard(
                                title = "Đánh giá TB",
                                value = "${String.format("%.1f", data.averageRating)}★",
                                icon = Icons.Filled.Star,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            PerformanceMetricCard(
                                title = "NPS Score",
                                value = data.npsScore.toString(),
                                icon = Icons.Filled.ThumbUp,
                                color = if ((data.npsScore ?: 0) >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = eventPerformanceState.message,
                        onRetry = { /* Handle retry */ }
                    )
                }
                else -> {
                    Text("Chưa có dữ liệu")
                }
            }
        }
    }
}

@Composable
fun RevenueComparisonChart(
    eventPerformanceState: ResourceState<EventPerformanceResponseDto>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "So sánh doanh thu - chi phí",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            when (eventPerformanceState) {
                is ResourceState.Loading -> {
                    LoadingChart()
                }
                is ResourceState.Success -> {
                    val data = eventPerformanceState.data
                    val comparisonData = mapOf(
                        "Doanh thu" to (data.totalRevenue?.toFloat() ?: 0f),
                        "Chi phí" to (data.totalCost?.toFloat() ?: 0f),
                        "Lợi nhuận" to ((data.totalRevenue ?: 0.0) - (data.totalCost ?: 0.0)).toFloat()
                    )
                    
                    BarChart(
                        data = comparisonData,
                        modifier = Modifier.height(200.dp),
                        barColor = MaterialTheme.colorScheme.primary
                    )
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = eventPerformanceState.message,
                        onRetry = { /* Handle retry */ }
                    )
                }
                else -> {
                    Text("Chưa có dữ liệu")
                }
            }
        }
    }
}

@Composable
private fun RevenueCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    NeumorphicCard(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun PerformanceMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingPerformanceCards() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingChart() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Lỗi: $message",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Thử lại")
        }
    }
}
