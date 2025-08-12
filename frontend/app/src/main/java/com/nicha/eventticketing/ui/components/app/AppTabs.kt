package com.nicha.eventticketing.ui.components.app

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppTabRow(
    selectedIndex: Int,
    titles: List<String>,
    onSelect: (Int) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        indicator = {},
        divider = {},
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        edgePadding = 16.dp
    ) {
        titles.forEachIndexed { i, title ->
            val selected = i == selectedIndex
            val interaction = MutableInteractionSource()
            Surface(
                color = if (selected) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    Modifier
                        .clickable(
                            interactionSource = interaction,
                            indication = null
                        ) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelect(i)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        title,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


