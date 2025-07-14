package com.nicha.eventticketing.ui.components.carousel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nicha.eventticketing.data.model.EventEntity
import com.nicha.eventticketing.ui.theme.LocalNeumorphismStyle
import com.nicha.eventticketing.ui.theme.NeumorphicCard
import com.nicha.eventticketing.ui.theme.NeumorphicTag
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EventCarousel(
    events: List<EventEntity>,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val carouselState = rememberLazyListState()
    
    Column(modifier = modifier) {
        LazyRow(
            state = carouselState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(events) { event ->
                CarouselEventCard(
                    event = event,
                    onClick = { onEventClick(event.id) }
                )
            }
        }
    }
}

@Composable
fun CarouselEventCard(
    event: EventEntity,
    onClick: () -> Unit
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    
    NeumorphicCard(
        modifier = Modifier
            .width(280.dp)
            .height(320.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            // Event image
            Box(
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            ) {
                AsyncImage(
                    model = event.featuredImageUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = neumorphismStyle.cornerRadius, topEnd = neumorphismStyle.cornerRadius))
                )
                
                // Date tag
                NeumorphicTag(
                    text = formatDate(event.startDate),
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
            }
            
            // Event details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = formatDate(event.startDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Type tag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeumorphicTag(
                        text = event.type.toString(),
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

// Helper function to format date
private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return dateString)
    } catch (e: Exception) {
        dateString
    }
}