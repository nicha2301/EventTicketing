package com.nicha.eventticketing.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.ui.theme.LocalNeumorphismStyle
import java.text.SimpleDateFormat
import java.util.Locale
import com.nicha.eventticketing.util.ImageUtils.getPrimaryImageUrl

/**
 * Thẻ hiển thị thông tin sự kiện với thiết kế neumorphic hiện đại
 */
@Composable
fun EventCard(
    event: EventDto,
    onEventClick: () -> Unit
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    
    NeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEventClick)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            // Event image
            AsyncImage(
                model = event.getPrimaryImageUrl(),
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            
            // Event info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Status chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when(event.status) {
                        "PUBLISHED" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        "DRAFT" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        "COMPLETED" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        "CANCELLED" -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = when(event.status) {
                            "PUBLISHED" -> "Đang diễn ra"
                            "DRAFT" -> "Bản nháp"
                            "COMPLETED" -> "Đã kết thúc"
                            "CANCELLED" -> "Đã hủy"
                            else -> event.status ?: ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when(event.status) {
                            "PUBLISHED" -> MaterialTheme.colorScheme.primary
                            "DRAFT" -> MaterialTheme.colorScheme.tertiary
                            "COMPLETED" -> MaterialTheme.colorScheme.secondary
                            "CANCELLED" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Event title
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    // Format date
                    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val eventStartDate = event.startDate
                    
                    Text(
                        text = if (eventStartDate != null && eventStartDate.contains("T")) {
                            try {
                                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                val date = isoFormat.parse(eventStartDate.substringBefore("."))
                                dateFormatter.format(date)
                            } catch (e: Exception) {
                                eventStartDate
                            }
                        } else eventStartDate ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.locationName ?: event.address ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Attendees
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PeopleAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.currentAttendees ?: 0}/${event.maxAttendees ?: "Không giới hạn"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Thẻ hiển thị thống kê trên dashboard
 */
@Composable
fun DashboardStatCard(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Hiển thị trạng thái sự kiện dưới dạng chip
 */
@Composable
fun EventStatusChip(status: String?) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when(status) {
            "PUBLISHED" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            "DRAFT" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
            "COMPLETED" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            "CANCELLED" -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Text(
            text = when(status) {
                "PUBLISHED" -> "Đang diễn ra"
                "DRAFT" -> "Bản nháp"
                "COMPLETED" -> "Đã kết thúc"
                "CANCELLED" -> "Đã hủy"
                else -> status ?: "Không xác định"
            },
            style = MaterialTheme.typography.labelSmall,
            color = when(status) {
                "PUBLISHED" -> MaterialTheme.colorScheme.primary
                "DRAFT" -> MaterialTheme.colorScheme.tertiary
                "COMPLETED" -> MaterialTheme.colorScheme.secondary
                "CANCELLED" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
} 