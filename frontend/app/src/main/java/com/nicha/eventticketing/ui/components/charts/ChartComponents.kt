package com.nicha.eventticketing.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DoughnutChart(
    data: Map<String, Float>,
    modifier: Modifier = Modifier,
    colors: List<Color> = defaultChartColors(),
    strokeWidth: Float = 40f
) {
    val density = LocalDensity.current
    val strokeWidthDp = with(density) { strokeWidth.toDp() }
    
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            EmptyChart()
        }
    } else {
        val total = data.values.sum()
        if (total > 0) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        drawDoughnutChart(data, colors, strokeWidth, total)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ChartLegend(
                    data = data,
                    colors = colors,
                    total = total,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                EmptyChart()
            }
        }
    }
}

@Composable
fun LineChart(
    data: Map<String, Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 4f
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            EmptyChart()
        }
    } else {
        val sortedData = data.toList().sortedBy { it.first }
        val xLabels = if (sortedData.size > 6) {
            val step = (sortedData.size - 1) / 5
            (0..5).map { i -> sortedData.getOrNull(i * step)?.first ?: "" }
        } else {
            sortedData.map { it.first }
        }
        val yValues = sortedData.map { it.second }
        val yMin = yValues.minOrNull() ?: 0f
        val yMax = yValues.maxOrNull() ?: 0f
        val yStep = if (yMax - yMin > 0) (yMax - yMin) / 4 else 1f
        val yLabels = (0..4).map { i -> yMin + i * yStep }

        val scrollState = rememberScrollState()
        val labelWidth = 60.dp
        val chartWidth = if (xLabels.size > 6) labelWidth * xLabels.size else null
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Y-axis labels
                    Column(
                        modifier = Modifier.width(40.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        yLabels.reversed().forEach { label ->
                            Text(
                                text = String.format("%.0f", label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Box(
                            modifier = if (chartWidth != null) Modifier
                                .horizontalScroll(scrollState)
                                .width(chartWidth)
                                .fillMaxHeight()
                            else Modifier.fillMaxSize()
                        ) {
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Draw line
                                drawLineChart(data, lineColor, strokeWidth)

                                // Draw points and value labels
                                val values = data.values.toList()
                                val maxValue = values.maxOrNull() ?: 0f
                                val minValue = values.minOrNull() ?: 0f
                                val range = maxValue - minValue
                                if (values.size > 1 && range > 0f) {
                                    val stepX = size.width / (values.size - 1)
                                    values.forEachIndexed { index, value ->
                                        val x = index * stepX
                                        val y = size.height - (value - minValue) / range * size.height
                                        // Draw point
                                        drawCircle(
                                            color = lineColor,
                                            radius = 5.dp.toPx(),
                                            center = Offset(x, y)
                                        )
                                    }
                                    // Draw value labels above points
                                    values.forEachIndexed { index, value ->
                                        val x = index * stepX
                                        val y = size.height - (value - minValue) / range * size.height
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
                        }
                    }
                }
            }
            // X-axis labels, horizontally scrollable if many
            if (xLabels.size > 6) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                ) {
                    Row(
                        modifier = Modifier
                            .width(chartWidth!!)
                            .padding(start = 40.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        xLabels.forEach { label ->
                            Box(
                                modifier = Modifier.width(labelWidth),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    xLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BarChart(
    data: Map<String, Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (data.isEmpty()) {
            EmptyChart()
        } else {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawBarChart(data, barColor)
            }
        }
    }
}

@Composable
private fun ChartLegend(
    data: Map<String, Float>,
    colors: List<Color>,
    total: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        data.entries.take(colors.size).forEachIndexed { index, (label, value) ->
            val percentage = (value / total * 100).takeIf { it.isFinite() } ?: 0f
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(colors[index])
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${String.format("%.1f", percentage)}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun EmptyChart() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Không có dữ liệu",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun DrawScope.drawDoughnutChart(
    data: Map<String, Float>,
    colors: List<Color>,
    strokeWidth: Float,
    total: Float
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = minOf(centerX, centerY) - strokeWidth / 2
    
    var startAngle = -90f
    
    data.entries.take(colors.size).forEachIndexed { index, (_, value) ->
        val sweepAngle = (value / total * 360f).takeIf { it.isFinite() } ?: 0f
        
        drawArc(
            color = colors[index],
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        startAngle += sweepAngle
    }
}

private fun DrawScope.drawLineChart(
    data: Map<String, Float>,
    lineColor: Color,
    strokeWidth: Float
) {
    if (data.size < 2) return
    
    val values = data.values.toList()
    val maxValue = values.maxOrNull() ?: 0f
    val minValue = values.minOrNull() ?: 0f
    val range = maxValue - minValue
    
    if (range <= 0) return
    
    val path = Path()
    val stepX = size.width / (values.size - 1)
    
    values.forEachIndexed { index, value ->
        val x = index * stepX
        val y = size.height - (value - minValue) / range * size.height
        
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    
    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawBarChart(
    data: Map<String, Float>,
    barColor: Color
) {
    val values = data.values.toList()
    val maxValue = values.maxOrNull() ?: 0f
    
    if (maxValue <= 0) return
    
    val barWidth = size.width / values.size * 0.7f
    val barSpacing = size.width / values.size * 0.3f
    
    values.forEachIndexed { index, value ->
        val barHeight = (value / maxValue) * size.height
        val x = index * (barWidth + barSpacing) + barSpacing / 2
        val y = size.height - barHeight
        
        drawRect(
            color = barColor,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight)
        )
    }
}

@Composable
fun defaultChartColors(): List<Color> {
    return listOf(
        Color(0xFF4F8BC9),
        Color(0xFF81B29A),
        Color(0xFFF2CC8F),
        Color(0xFFE07A5F),
        Color(0xFFB4AEE8),
        Color(0xFFB8B8FF),
        Color(0xFFB5EAD7),
        Color(0xFFFFDAC1),
        Color(0xFFB0C4DE),
        Color(0xFFD6A4A4)
    )
}
