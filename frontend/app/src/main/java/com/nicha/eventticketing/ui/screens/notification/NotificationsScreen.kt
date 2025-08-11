package com.nicha.eventticketing.ui.screens.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.ErrorView
import com.nicha.eventticketing.ui.components.LoadingIndicator
import com.nicha.eventticketing.ui.components.app.AppButton
import com.nicha.eventticketing.ui.components.app.AppDestructiveButton
import com.nicha.eventticketing.ui.components.app.AppOutlinedButton
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (String, String, String) -> Unit = { _, _, _ -> },
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notificationsState by viewModel.notificationsState.collectAsState()
    val markAsReadState by viewModel.markAsReadState.collectAsState()
    val markAllAsReadState by viewModel.markAllAsReadState.collectAsState()
    val deleteNotificationState by viewModel.deleteNotificationState.collectAsState()
    val deleteAllNotificationsState by viewModel.deleteAllNotificationsState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showMarkAllReadDialog by remember { mutableStateOf(false) }

    // Load notifications when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.getNotifications()
        viewModel.getUnreadNotificationCount()
    }

    // Handle mark as read state changes
    LaunchedEffect(markAsReadState) {
        when (markAsReadState) {
            is ResourceState.Success -> {
                viewModel.resetMarkAsReadState()
            }

            is ResourceState.Error -> {
                val errorMessage = (markAsReadState as ResourceState.Error).message
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.resetMarkAsReadState()
            }

            else -> {}
        }
    }

    // Handle mark all as read state changes
    LaunchedEffect(markAllAsReadState) {
        when (markAllAsReadState) {
            is ResourceState.Success -> {
                val count = (markAllAsReadState as ResourceState.Success<*>).data
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Đã đánh dấu tất cả thông báo là đã đọc",
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.resetMarkAllAsReadState()
            }

            is ResourceState.Error -> {
                val errorMessage = (markAllAsReadState as ResourceState.Error).message
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.resetMarkAllAsReadState()
            }

            else -> {}
        }
    }

    // Handle delete notification state changes
    LaunchedEffect(deleteNotificationState) {
        when (deleteNotificationState) {
            is ResourceState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Đã xóa thông báo",
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.resetDeleteNotificationState()
            }

            is ResourceState.Error -> {
                val errorMessage = (deleteNotificationState as ResourceState.Error).message
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.resetDeleteNotificationState()
            }

            else -> {}
        }
    }

    // Handle delete all notifications state changes
    LaunchedEffect(deleteAllNotificationsState) {
        when (deleteAllNotificationsState) {
            is ResourceState.Success -> {
                val count = (deleteAllNotificationsState as ResourceState.Success<*>).data
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Đã xóa tất cả thông báo",
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.resetDeleteAllNotificationsState()
            }

            is ResourceState.Error -> {
                val errorMessage = (deleteAllNotificationsState as ResourceState.Error).message
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.resetDeleteAllNotificationsState()
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông báo") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    if (notificationsState is ResourceState.Success) {
                        val notifications = (notificationsState as ResourceState.Success<*>).data
                        if ((notifications as? PageDto<*>)?.content?.isNotEmpty() == true) {
                            IconButton(onClick = { showMarkAllReadDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Đánh dấu tất cả đã đọc"
                                )
                            }
                            IconButton(onClick = { showDeleteAllDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Xóa tất cả"
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (notificationsState) {
                is ResourceState.Loading -> {
                    LoadingIndicator()
                }

                is ResourceState.Error -> {
                    val errorMessage = (notificationsState as ResourceState.Error).message
                    ErrorView(
                        message = errorMessage,
                        onRetry = { viewModel.getNotifications() }
                    )
                }

                is ResourceState.Success -> {
                    val pageData =
                        (notificationsState as ResourceState.Success<PageDto<NotificationDto>>).data
                    val notifications = pageData.content ?: emptyList()

                    if (notifications.isEmpty()) {
                        EmptyNotificationsView()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(notifications) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onNotificationClick = {
                                        viewModel.markNotificationAsRead(notification.id)
                                        if (!notification.referenceType.isNullOrBlank() && !notification.referenceId.isNullOrBlank()) {
                                            onNotificationClick(
                                                notification.referenceType,
                                                notification.referenceId,
                                                notification.notificationType
                                            )
                                        }
                                    },
                                    onMarkAsRead = {
                                        if (!notification.isRead) {
                                            viewModel.markNotificationAsRead(notification.id)
                                        }
                                    },
                                    onDelete = {
                                        viewModel.deleteNotification(notification.id)
                                    }
                                )
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    // Mark all as read dialog
    if (showMarkAllReadDialog) {
        AlertDialog(
            onDismissRequest = { showMarkAllReadDialog = false },
            title = { Text("Đánh dấu tất cả đã đọc") },
            text = { Text("Bạn có chắc chắn muốn đánh dấu tất cả thông báo là đã đọc không?") },
            confirmButton = {
                AppButton(
                    onClick = {
                        viewModel.markAllNotificationsAsRead()
                        showMarkAllReadDialog = false
                    }
                ) {
                    Text("Đồng ý")
                }
            },
            dismissButton = {
                AppOutlinedButton(
                    onClick = { showMarkAllReadDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    // Delete all dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Xóa tất cả thông báo") },
            text = { Text("Bạn có chắc chắn muốn xóa tất cả thông báo không?") },
            confirmButton = {
                AppDestructiveButton(
                    onClick = {
                        viewModel.deleteAllNotifications()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Xóa tất cả")
                }
            },
            dismissButton = {
                AppOutlinedButton(
                    onClick = { showDeleteAllDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun NotificationItem(
    notification: NotificationDto,
    onNotificationClick: () -> Unit,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    var showMenu by remember { mutableStateOf(false) }

    NeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNotificationClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification icon based on type
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (!notification.isRead)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.notificationType) {
                        "EVENT_REMINDER" -> Icons.Default.Event
                        "NEW_COMMENT" -> Icons.Default.Comment
                        "TICKET_PURCHASED" -> Icons.Default.ConfirmationNumber
                        "SYSTEM" -> Icons.Default.Info
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = if (!notification.isRead)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Notification content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateFormat.format(
                        try {
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                .parse(notification.createdAt) ?: Date()
                        } catch (e: Exception) {
                            Date()
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Unread indicator
            AnimatedVisibility(
                visible = !notification.isRead,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Thêm tùy chọn"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (!notification.isRead) {
                        DropdownMenuItem(
                            text = { Text("Đánh dấu đã đọc") },
                            onClick = {
                                onMarkAsRead()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Xóa") },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Không có thông báo nào",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Các thông báo sẽ xuất hiện ở đây",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 