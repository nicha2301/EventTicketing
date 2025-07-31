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
import com.nicha.eventticketing.data.remote.dto.analytics.AttendeeAnalyticsResponseDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.charts.*
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard

@Composable
fun AttendeeOverviewSection(
    attendeeAnalyticsState: ResourceState<AttendeeAnalyticsResponseDto>
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
                text = "Tổng quan người tham dự",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            when (attendeeAnalyticsState) {
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
                    val data = attendeeAnalyticsState.data
                    val checkInRate = if (data.totalRegistered > 0) {
                        (data.totalCheckedIn.toDouble() / data.totalRegistered * 100)
                    } else 0.0
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            StatCard(
                                title = "Đã đăng ký",
                                value = data.totalRegistered.toString(),
                                icon = Icons.Filled.PersonAdd,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        item {
                            StatCard(
                                title = "Đã check-in",
                                value = data.totalCheckedIn.toString(),
                                icon = Icons.Filled.CheckCircle,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        item {
                            StatCard(
                                title = "Tỷ lệ tham dự",
                                value = "${String.format("%.1f", checkInRate)}%",
                                icon = Icons.Filled.Analytics,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = attendeeAnalyticsState.message,
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
fun DemographicsSection(
    attendeeAnalyticsState: ResourceState<AttendeeAnalyticsResponseDto>
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
                text = "Phân tích nhân khẩu học",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            when (attendeeAnalyticsState) {
                is ResourceState.Loading -> {
                    LoadingChart()
                }
                is ResourceState.Success -> {
                    val data = attendeeAnalyticsState.data
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Age Distribution
                        Text(
                            text = "Phân bố theo độ tuổi",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        
                        DoughnutChart(
                            data = data.ageDistribution.mapValues { it.value.toFloat() },
                            modifier = Modifier.height(200.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Gender Distribution  
                        Text(
                            text = "Phân bố theo giới tính",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            data.genderDistribution.forEach { (gender, count) ->
                                GenderStatItem(
                                    gender = gender,
                                    count = count,
                                    total = data.genderDistribution.values.sum()
                                )
                            }
                        }
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = attendeeAnalyticsState.message,
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
fun LocationAnalysisSection(
    attendeeAnalyticsState: ResourceState<AttendeeAnalyticsResponseDto>
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
                text = "Phân tích địa lý",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            when (attendeeAnalyticsState) {
                is ResourceState.Loading -> {
                    LoadingChart()
                }
                is ResourceState.Success -> {
                    val data = attendeeAnalyticsState.data
                    val sortedLocations = data.locationDistribution.toList()
                        .sortedByDescending { it.second }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sortedLocations.forEach { (location, count) ->
                            LocationItem(
                                location = location,
                                count = count,
                                total = data.locationDistribution.values.sum()
                            )
                        }
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = attendeeAnalyticsState.message,
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
fun RegistrationTimelineSection(
    attendeeAnalyticsState: ResourceState<AttendeeAnalyticsResponseDto>
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
                text = "Lịch sử đăng ký",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            when (attendeeAnalyticsState) {
                is ResourceState.Loading -> {
                    LoadingChart()
                }
                is ResourceState.Success -> {
                    val data = attendeeAnalyticsState.data
                    
                    LineChart(
                        data = data.registrationTimeline?.mapValues { it.value.toFloat() } ?: emptyMap(),
                        modifier = Modifier.height(200.dp),
                        lineColor = MaterialTheme.colorScheme.primary
                    )
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = attendeeAnalyticsState.message,
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
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    NeumorphicCard(
        modifier = Modifier
            .width(120.dp)
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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
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
private fun GenderStatItem(
    gender: String,
    count: Int,
    total: Int
) {
    val percentage = if (total > 0) (count.toFloat() / total * 100) else 0f
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "${String.format("%.1f", percentage)}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = gender,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LocationItem(
    location: String,
    count: Int,
    total: Int
) {
    val percentage = if (total > 0) (count.toFloat() / total) else 0f
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = location,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            LinearProgressIndicator(
                progress = percentage,
                modifier = Modifier.width(60.dp),
                color = MaterialTheme.colorScheme.primary
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
