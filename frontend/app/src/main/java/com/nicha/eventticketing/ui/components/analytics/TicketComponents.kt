package com.nicha.eventticketing.ui.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.nicha.eventticketing.data.remote.dto.analytics.TicketSalesResponseDto
import com.nicha.eventticketing.data.remote.dto.analytics.TicketTypeStatsDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.charts.*
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import java.text.NumberFormat
import java.util.*

@Composable
fun TicketSalesSummarySection(
    ticketSalesState: ResourceState<TicketSalesResponseDto>
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
                text = "Tổng quan bán vé",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            when (ticketSalesState) {
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
                    val data = ticketSalesState.data
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SummaryCard(
                                title = "Tổng vé bán",
                                value = data.totalSold.toString(),
                                icon = Icons.Filled.ConfirmationNumber,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        item {
                            SummaryCard(
                                title = "Doanh thu",
                                value = currencyFormat.format(data.totalRevenue),
                                icon = Icons.Filled.AttachMoney,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        item {
                            SummaryCard(
                                title = "Loại vé",
                                value = data.ticketTypeData.size.toString(),
                                icon = Icons.Filled.Category,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = ticketSalesState.message,
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
fun SalesByTypeChart(
    ticketSalesState: ResourceState<TicketSalesResponseDto>
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
                text = "Bán vé theo loại",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            when (ticketSalesState) {
                is ResourceState.Loading -> {
                    LoadingChart()
                }
                is ResourceState.Success -> {
                    val data = ticketSalesState.data
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Pie Chart
                        DoughnutChart(
                            data = data.ticketTypeData.mapValues { it.value.count.toFloat() },
                            modifier = Modifier.height(200.dp)
                        )
                        
                        // Details List
                        data.ticketTypeData.forEach { (type, stats) ->
                            TicketTypeItem(
                                type = type,
                                stats = stats,
                                totalSold = data.totalSold
                            )
                        }
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = ticketSalesState.message,
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
fun SalesTrendChart(
    ticketSalesState: ResourceState<TicketSalesResponseDto>
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
                text = "Xu hướng bán vé",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            when (ticketSalesState) {
                is ResourceState.Loading -> {
                    LoadingChart()
                }
                is ResourceState.Success -> {
                    val data = ticketSalesState.data
                    
                    if (data.dailySales?.isNotEmpty() == true) {
                        LineChart(
                            data = data.dailySales?.mapValues { it.value.toFloat() } ?: emptyMap(),
                            modifier = Modifier.height(200.dp),
                            lineColor = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        EmptyStateMessage("Chưa có dữ liệu xu hướng bán vé")
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = ticketSalesState.message,
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
fun TicketDetailsList(
    ticketSalesState: ResourceState<TicketSalesResponseDto>
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
                text = "Chi tiết bán vé",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            when (ticketSalesState) {
                is ResourceState.Loading -> {
                    repeat(3) {
                        TicketItemSkeleton()
                    }
                }
                is ResourceState.Success -> {
                    val data = ticketSalesState.data
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        data.ticketTypeData.forEach { (type, stats) ->
                            DetailedTicketItem(
                                type = type,
                                stats = stats,
                                currencyFormat = currencyFormat
                            )
                        }
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = ticketSalesState.message,
                        onRetry = { /* Handle retry */ }
                    )
                }
                else -> {
                    EmptyStateMessage("Chưa có dữ liệu chi tiết")
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    NeumorphicCard(
        modifier = Modifier
            .width(140.dp)
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
                maxLines = 1
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
private fun TicketTypeItem(
    type: String,
    stats: TicketTypeStatsDto,
    totalSold: Int
) {
    val percentage = if (totalSold > 0) (stats.count.toFloat() / totalSold * 100) else 0f
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = type,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = "${stats.count} vé (${String.format("%.1f", percentage)}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = currencyFormat.format(stats.revenue),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary
            )
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier.width(80.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DetailedTicketItem(
    type: String,
    stats: TicketTypeStatsDto,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ConfirmationNumber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "${stats.count} vé đã bán",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = currencyFormat.format(stats.revenue),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TicketItemSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    )
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
        }
    }
}

@Composable
private fun LoadingChart() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
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
            .height(200.dp),
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
