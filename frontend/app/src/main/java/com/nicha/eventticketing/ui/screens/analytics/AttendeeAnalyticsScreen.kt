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
fun AttendeeAnalyticsScreen(
    onBackClick: () -> Unit,
    eventId: String,
    viewModel: AnalyticsDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val attendeeAnalyticsState by viewModel.attendeeAnalyticsState.collectAsState()
    
    // Set event ID for analysis
    LaunchedEffect(eventId) {
        viewModel.updateSelectedEventForDetails(eventId)
        viewModel.loadAttendeeAnalytics(eventId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Phân tích người tham dự",
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
                        viewModel.exportData("attendee_analytics", "pdf")
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
            // Attendee Overview
            item {
                AttendeeOverviewSection(
                    attendeeAnalyticsState = attendeeAnalyticsState
                )
            }
            
            // Demographics Breakdown
            item {
                DemographicsSection(
                    attendeeAnalyticsState = attendeeAnalyticsState
                )
            }
            
            // Location Analysis
            item {
                LocationAnalysisSection(
                    attendeeAnalyticsState = attendeeAnalyticsState
                )
            }
            
            // Registration Timeline
            item {
                RegistrationTimelineSection(
                    attendeeAnalyticsState = attendeeAnalyticsState
                )
            }
            
            // Engagement Metrics
            item {
                EngagementMetricsSection()
            }
            
            // Repeat Attendees
            item {
                RepeatAttendeesSection(
                    attendeeAnalyticsState = attendeeAnalyticsState
                )
            }
        }
    }
}

@Composable
private fun AttendeeOverviewSection(
    attendeeAnalyticsState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.AttendeeAnalyticsResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tổng quan người tham dự",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (attendeeAnalyticsState) {
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
                is ResourceState.Success -> {
                    val data = attendeeAnalyticsState.data
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AttendeeStatsCard(
                            title = "Tổng đăng ký",
                            value = data.totalRegistered.toString(),
                            icon = Icons.Filled.PersonAdd,
                            color = Color.Blue,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        AttendeeStatsCard(
                            title = "Đã check-in",
                            value = data.totalCheckedIn.toString(),
                            subtitle = "${calculatePercentage(data.totalCheckedIn, data.totalRegistered)}%",
                            icon = Icons.Filled.CheckCircle,
                            color = Color.Green,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        AttendeeStatsCard(
                            title = "Không tham gia",
                            value = (data.totalRegistered - data.totalCheckedIn).toString(),
                            subtitle = "${calculatePercentage(data.totalRegistered - data.totalCheckedIn, data.totalRegistered)}%",
                            icon = Icons.Filled.Cancel,
                            color = Color.Red,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        AttendeeStatsCard(
                            title = "Tỷ lệ tham gia",
                            value = "${calculatePercentage(data.totalCheckedIn, data.totalRegistered)}%",
                            icon = Icons.Filled.TrendingUp,
                            color = androidx.compose.ui.graphics.Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = attendeeAnalyticsState.message,
                        onRetry = { /* Retry logic */ }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun DemographicsSection(
    attendeeAnalyticsState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.AttendeeAnalyticsResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Thống kê nhân khẩu học",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (attendeeAnalyticsState) {
                is ResourceState.Loading -> {
                    ShimmerChart(height = 200.dp)
                }
                is ResourceState.Success -> {
                    val data = attendeeAnalyticsState.data
                    
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Age Distribution Chart
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Phân bố theo tuổi",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            AgeDistributionChart(
                                data = data.ageDistribution,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Gender Distribution
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Phân bố giới tính",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            GenderDistributionChart(
                                data = data.genderDistribution,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Detailed breakdown
                    DemographicsBreakdown(
                        ageDistribution = data.ageDistribution,
                        genderDistribution = data.genderDistribution
                    )
                }
                is ResourceState.Error -> {
                    ErrorMessage(
                        message = "Không thể tải dữ liệu nhân khẩu học",
                        onRetry = { /* Retry logic */ }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun LocationAnalysisSection(
    attendeeAnalyticsState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.AttendeeAnalyticsResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Phân tích địa lý",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (attendeeAnalyticsState) {
                is ResourceState.Success -> {
                    val data = attendeeAnalyticsState.data
                    val topLocations = data.locationDistribution.toList()
                        .sortedByDescending { it.second }
                        .take(5)
                    
                    Column {
                        Text(
                            text = "Top 5 khu vực",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        topLocations.forEachIndexed { index, (location, count) ->
                            LocationItem(
                                rank = index + 1,
                                location = location,
                                count = count,
                                percentage = calculatePercentage(count, data.totalRegistered)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Location Distribution Chart
                        LocationDistributionChart(
                            data = data.locationDistribution,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
                is ResourceState.Loading -> {
                    ShimmerChart(height = 250.dp)
                }
                else -> {
                    Text("Không có dữ liệu vị trí")
                }
            }
        }
    }
}

@Composable
private fun RegistrationTimelineSection(
    attendeeAnalyticsState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.AttendeeAnalyticsResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Timeline đăng ký",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (attendeeAnalyticsState) {
                is ResourceState.Success -> {
                    val data = attendeeAnalyticsState.data
                    
                    RegistrationTimelineChart(
                        data = data.registrationTimeline ?: emptyMap(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Timeline insights
                    RegistrationInsights(
                        timeline = data.registrationTimeline ?: emptyMap()
                    )
                }
                is ResourceState.Loading -> {
                    ShimmerChart(height = 250.dp)
                }
                else -> {
                    Text("Không có dữ liệu timeline")
                }
            }
        }
    }
}

@Composable
private fun EngagementMetricsSection() {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Chỉ số tương tác",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mock engagement metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EngagementMetricCard(
                    title = "Email mở",
                    value = "72.4%",
                    trend = "+5.2%",
                    color = Color.Blue,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                EngagementMetricCard(
                    title = "Click link",
                    value = "34.1%",
                    trend = "+2.8%",
                    color = Color.Green,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                EngagementMetricCard(
                    title = "Chia sẻ",
                    value = "18.7%",
                    trend = "-1.3%",
                    color = androidx.compose.ui.graphics.Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                EngagementMetricCard(
                    title = "Feedback",
                    value = "45.8%",
                    trend = "+7.1%",
                    color = androidx.compose.ui.graphics.Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RepeatAttendeesSection(
    attendeeAnalyticsState: ResourceState<com.nicha.eventticketing.data.remote.dto.analytics.AttendeeAnalyticsResponseDto>
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Khách hàng trung thành",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (attendeeAnalyticsState) {
                is ResourceState.Success -> {
                    val data = attendeeAnalyticsState.data
                    // Mock repeat attendee data - would come from backend
                    val newAttendees = (data.totalRegistered * 0.6).toInt()
                    val repeatAttendees = data.totalRegistered - newAttendees
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LoyaltyMetricCard(
                            title = "Khách mới",
                            count = newAttendees,
                            percentage = calculatePercentage(newAttendees, data.totalRegistered),
                            color = Color.Blue,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        LoyaltyMetricCard(
                            title = "Khách cũ",
                            count = repeatAttendees,
                            percentage = calculatePercentage(repeatAttendees, data.totalRegistered),
                            color = Color.Green,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Lịch sử tham gia",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Attendance history breakdown
                    AttendanceHistoryBreakdown()
                }
                else -> {
                    Text("Không có dữ liệu khách hàng trung thành")
                }
            }
        }
    }
}

// Helper Components

@Composable
private fun AttendeeStatsCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun DemographicsBreakdown(
    ageDistribution: Map<String, Int>,
    genderDistribution: Map<String, Int>
) {
    Column {
        Text(
            text = "Chi tiết thống kê",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Age breakdown
        ageDistribution.forEach { (ageGroup, count) ->
            StatItem(
                label = "Độ tuổi $ageGroup",
                value = "$count người",
                color = Color.Blue
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Gender breakdown
        genderDistribution.forEach { (gender, count) ->
            StatItem(
                label = gender,
                value = "$count người",
                color = androidx.compose.ui.graphics.Color(0xFFE91E63)
            )
        }
    }
}

@Composable
private fun LocationItem(
    rank: Int,
    location: String,
    count: Int,
    percentage: Int
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
                color = MaterialTheme.colorScheme.primary
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
                text = location,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Text(
            text = "$count ($percentage%)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun RegistrationInsights(
    timeline: Map<String, Int>
) {
    if (timeline.isEmpty()) return
    
    val peakDay = timeline.maxByOrNull { it.value }
    val totalRegistrations = timeline.values.sum()
    
    Column {
        Text(
            text = "Thông tin chi tiết",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (peakDay != null) {
            StatItem(
                label = "Ngày đăng ký cao nhất",
                value = "${formatDate(peakDay.key)} (${peakDay.value} người)",
                color = Color.Green
            )
        }
        
        StatItem(
            label = "Trung bình mỗi ngày",
            value = "${totalRegistrations / timeline.size} người",
            color = Color.Blue
        )
    }
}

@Composable
private fun EngagementMetricCard(
    title: String,
    value: String,
    trend: String,
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
            
            Text(
                text = trend,
                style = MaterialTheme.typography.bodySmall,
                color = if (trend.startsWith("+")) Color.Green else Color.Red
            )
        }
    }
}

@Composable
private fun LoyaltyMetricCard(
    title: String,
    count: Int,
    percentage: Int,
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
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun AttendanceHistoryBreakdown() {
    Column {
        AttendanceHistoryItem(
            label = "Lần đầu tham gia",
            count = 156,
            percentage = 60
        )
        
        AttendanceHistoryItem(
            label = "2-3 lần",
            count = 78,
            percentage = 30
        )
        
        AttendanceHistoryItem(
            label = "4+ lần",
            count = 26,
            percentage = 10
        )
    }
}

@Composable
private fun AttendanceHistoryItem(
    label: String,
    count: Int,
    percentage: Int
) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "$count người ($percentage%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// Helper functions
private fun calculatePercentage(part: Int, total: Int): Int {
    return if (total > 0) ((part.toFloat() / total) * 100).toInt() else 0
}

private fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        "${parts[2]}/${parts[1]}"
    } catch (e: Exception) {
        dateString
    }
}
