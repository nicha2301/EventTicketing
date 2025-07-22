package com.nicha.eventticketing.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Component hiển thị badge thông báo với số lượng
 * @param count Số lượng thông báo chưa đọc
 * @param maxCount Số lượng tối đa hiển thị, nếu vượt quá sẽ hiển thị dưới dạng "maxCount+"
 */
@Composable
fun NotificationBadge(
    count: Int,
    maxCount: Int = 99,
    modifier: Modifier = Modifier,
    badgeColor: Color = MaterialTheme.colorScheme.error
) {
    AnimatedVisibility(
        visible = count > 0,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            val displayText = if (count > maxCount) "$maxCount+" else count.toString()
            Text(
                text = displayText,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(1.dp)
            )
        }
    }
} 