package com.nicha.eventticketing.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import kotlin.math.cos
import kotlin.math.sin

/**
 * Simple line chart for revenue over time
 */
@Composable
fun RevenueLineChart(
    data: Map<String, Double>,
    title: String = "Doanh thu theo thời gian",
    modifier: Modifier = Modifier
) {
    NeumorphicCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (data.isEmpty()) {
                EmptyChartPlaceholder("Không có dữ liệu doanh thu")
            } else {
                // Use the enhanced LineChart from ChartComponents.kt for full info
                com.nicha.eventticketing.ui.components.charts.LineChart(
                    data = data.mapValues { it.value.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

/**
 * Simple bar chart for ticket sales
 */
@Composable
fun TicketSalesBarChart(
    data: Map<String, Int>,
    title: String = "Bán vé theo loại",
    modifier: Modifier = Modifier
) {
    NeumorphicCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (data.isEmpty()) {
                EmptyChartPlaceholder("Không có dữ liệu bán vé")
            } else {
                SimpleBarChart(
                    data = data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

/**
 * Simple pie chart for check-in statistics
 */
@Composable
fun CheckInPieChart(
    checkedIn: Int,
    notCheckedIn: Int,
    title: String = "Thống kê Check-in",
    modifier: Modifier = Modifier
) {
    NeumorphicCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (checkedIn == 0 && notCheckedIn == 0) {
                EmptyChartPlaceholder("Không có dữ liệu check-in")
            } else {
                SimplePieChart(
                    checkedIn = checkedIn,
                    notCheckedIn = notCheckedIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

/**
 * Simple donut chart for rating distribution
 */
@Composable
fun RatingDistributionChart(
    ratingCounts: Map<String, Long>,
    averageRating: Double,
    title: String = "Phân bố đánh giá",
    modifier: Modifier = Modifier
) {
    NeumorphicCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (ratingCounts.values.sum() == 0L) {
                EmptyChartPlaceholder("Chưa có đánh giá")
            } else {
                SimpleDonutChart(
                    ratingCounts = ratingCounts,
                    averageRating = averageRating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

/**
 * Simple line chart implementation
 */
@Composable
private fun SimpleLineChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val dataList = data.values.toList()
        val maxValue = dataList.maxOrNull() ?: 0.0
        val minValue = dataList.minOrNull() ?: 0.0
        val range = maxValue - minValue

        if (range == 0.0) return@Canvas

        val width = size.width
        val height = size.height
        val stepX = width / (dataList.size - 1).coerceAtLeast(1)

        // Draw line
        val path = Path()
        dataList.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minValue) / range * height).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points
        dataList.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minValue) / range * height).toFloat()

            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // Draw value labels above points
        dataList.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minValue) / range * height).toFloat()
            drawContext.canvas.nativeCanvas.apply {
                val label = String.format("%.0f", value)
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 28f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(
                    label,
                    x,
                    y - 16.dp.toPx(),
                    paint
                )
            }
        }
    }
}

/**
 * Simple bar chart implementation
 */
@Composable
private fun SimpleBarChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val dataList = data.values.toList()
        val maxValue = dataList.maxOrNull() ?: 0
        
        if (maxValue == 0) return@Canvas
        
        val width = size.width
        val height = size.height
        val barWidth = width / data.size * 0.8f
        val barSpacing = width / data.size * 0.2f
        
        dataList.forEachIndexed { index, value ->
            val barHeight = (value.toFloat() / maxValue * height)
            val x = index * (barWidth + barSpacing) + barSpacing / 2
            val y = height - barHeight
            
            drawRect(
                color = primaryColor,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

/**
 * Simple pie chart implementation
 */
@Composable
private fun SimplePieChart(
    checkedIn: Int,
    notCheckedIn: Int,
    modifier: Modifier = Modifier
) {
    val checkedInColor = MaterialTheme.colorScheme.primary
    val notCheckedInColor = MaterialTheme.colorScheme.surfaceVariant
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pie chart
        Canvas(
            modifier = Modifier.size(120.dp)
        ) {
            val total = checkedIn + notCheckedIn
            if (total == 0) return@Canvas
            
            val checkedInAngle = (checkedIn.toFloat() / total * 360f)
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            
            // Draw checked in
            drawArc(
                color = checkedInColor,
                startAngle = 0f,
                sweepAngle = checkedInAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
            
            // Draw not checked in
            drawArc(
                color = notCheckedInColor,
                startAngle = checkedInAngle,
                sweepAngle = 360f - checkedInAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }
        
        // Legend
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChartLegendItem(
                color = checkedInColor,
                label = "Đã check-in",
                value = checkedIn.toString()
            )
            ChartLegendItem(
                color = notCheckedInColor,
                label = "Chưa check-in",
                value = notCheckedIn.toString()
            )
        }
    }
}

/**
 * Simple donut chart implementation
 */
@Composable
private fun SimpleDonutChart(
    ratingCounts: Map<String, Long>,
    averageRating: Double,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        MaterialTheme.colorScheme.primary
    )
    val defaultColor = MaterialTheme.colorScheme.surfaceVariant
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Donut chart
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val total = ratingCounts.values.sum()
                if (total == 0L) return@Canvas
                
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2
                val strokeWidth = 20.dp.toPx()
                
                var startAngle = 0f
                
                ratingCounts.entries.forEachIndexed { index, (rating, count) ->
                    val sweepAngle = (count.toFloat() / total * 360f)
                    val color = colors.getOrNull(rating.toIntOrNull()?.minus(1) ?: 0) 
                        ?: defaultColor
                    
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth)
                    )
                    
                    startAngle += sweepAngle
                }
            }
            
            // Center text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%.1f", averageRating),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "★",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Legend
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ratingCounts.entries.forEach { (rating, count) ->
                val color = colors.getOrNull(rating.toIntOrNull()?.minus(1) ?: 0) 
                    ?: MaterialTheme.colorScheme.surfaceVariant
                
                ChartLegendItem(
                    color = color,
                    label = "$rating ★",
                    value = count.toString()
                )
            }
        }
    }
}

/**
 * Chart legend item
 */
@Composable
fun ChartLegendItem(
    color: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Empty chart placeholder
 */
@Composable
private fun EmptyChartPlaceholder(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
