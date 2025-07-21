package com.nicha.eventticketing.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Component hiển thị icon thông báo với badge
 * @param count Số lượng thông báo chưa đọc
 * @param onClick Callback khi click vào icon
 */
@Composable
fun NotificationIconWithBadge(
    count: Int,
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Notifications,
    contentDescription: String? = "Thông báo"
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Badge
        NotificationBadge(
            count = count,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-2).dp, y = 2.dp)
        )
    }
} 