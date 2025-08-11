package com.nicha.eventticketing.ui.components.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsFilter(
    selectedDateRange: Pair<String, String>,
    selectedPeriod: String = "DAILY",
    selectedEvents: List<String> = emptyList(),
    availableEvents: List<Pair<String, String>> = emptyList(),
    onDateRangeChange: (Pair<String, String>) -> Unit,
    onPeriodChange: (String) -> Unit,
    onEventsChange: (List<String>) -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showEventSelector by remember { mutableStateOf(false) }
    var showPeriodSelector by remember { mutableStateOf(false) }

    NeumorphicCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bộ lọc Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Export button
                OutlinedButton(
                    onClick = onExportClick,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export")
                }
            }
            
            // Date range selector
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Khoảng thời gian",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${selectedDateRange.first} - ${selectedDateRange.second}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Period selector
                OutlinedCard(
                    onClick = { showPeriodSelector = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Chu kỳ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when(selectedPeriod) {
                                    "DAILY" -> "Theo ngày"
                                    "WEEKLY" -> "Theo tuần"
                                    "MONTHLY" -> "Theo tháng"
                                    else -> selectedPeriod
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Event selector
                if (availableEvents.isNotEmpty()) {
                    OutlinedCard(
                        onClick = { showEventSelector = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Sự kiện",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (selectedEvents.isEmpty()) "Tất cả" 
                                          else "${selectedEvents.size} sự kiện",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Date Picker Dialog (placeholder - will implement with actual date picker later)
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Chọn khoảng thời gian") },
            text = { 
                Text("Date picker sẽ được implement sau với thư viện compose-material-dialogs")
            },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Period Selector Dialog
    if (showPeriodSelector) {
        AlertDialog(
            onDismissRequest = { showPeriodSelector = false },
            title = { Text("Chọn chu kỳ hiển thị") },
            text = {
                Column {
                    val periods = listOf(
                        "DAILY" to "Theo ngày",
                        "WEEKLY" to "Theo tuần", 
                        "MONTHLY" to "Theo tháng"
                    )
                    
                    periods.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPeriod == value,
                                onClick = {
                                    onPeriodChange(value)
                                    showPeriodSelector = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledSelectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    disabledUnselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPeriodSelector = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    
    // Event Selector Dialog
    if (showEventSelector) {
        AlertDialog(
            onDismissRequest = { showEventSelector = false },
            title = { Text("Chọn sự kiện") },
            text = {
                Column {
                    availableEvents.forEach { (eventId, eventName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedEvents.contains(eventId),
                                onCheckedChange = { checked ->
                                    val newSelection = if (checked) {
                                        selectedEvents + eventId
                                    } else {
                                        selectedEvents - eventId
                                    }
                                    onEventsChange(newSelection)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.onSurface,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    checkmarkColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = eventName,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEventSelector = false }) {
                    Text("Xong")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onEventsChange(emptyList())
                        showEventSelector = false
                    }
                ) {
                    Text("Xóa tất cả")
                }
            }
        )
    }
}

/**
 * Quick filter chips cho các khoảng thời gian phổ biến
 */
@Composable
fun QuickFilterChips(
    onFilterSelected: (Pair<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val quickFilters = listOf(
            "7 ngày qua" to getDateRange(7),
            "30 ngày qua" to getDateRange(30),
            "3 tháng qua" to getDateRange(90),
            "Năm nay" to getCurrentYearRange()
        )
        
        quickFilters.forEach { (label, dateRange) ->
            FilterChip(
                onClick = { onFilterSelected(dateRange) },
                label = { Text(label) },
                selected = false
            )
        }
    }
}

private fun getDateRange(days: Int): Pair<String, String> {
    return "2025-07-01" to "2025-07-28"
}

private fun getCurrentYearRange(): Pair<String, String> {
    return "2025-01-01" to "2025-12-31"
}
