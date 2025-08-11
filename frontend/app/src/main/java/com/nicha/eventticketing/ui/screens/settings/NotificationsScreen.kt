package com.nicha.eventticketing.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import com.nicha.eventticketing.ui.components.app.AppButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicha.eventticketing.R
import com.nicha.eventticketing.data.remote.dto.notification.NotificationPreferencesDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.ErrorView
import com.nicha.eventticketing.ui.components.LoadingIndicator
import com.nicha.eventticketing.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val preferencesState by viewModel.preferencesState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    var emailNotifications by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }
    var inAppNotifications by remember { mutableStateOf(true) }
    var eventReminders by remember { mutableStateOf(true) }
    var commentNotifications by remember { mutableStateOf(true) }
    var ratingNotifications by remember { mutableStateOf(true) }
    var ticketUpdates by remember { mutableStateOf(true) }
    var marketingNotifications by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.getNotificationPreferences()
    }
    
    LaunchedEffect(preferencesState) {
        if (preferencesState is ResourceState.Success) {
            val preferences = (preferencesState as ResourceState.Success<NotificationPreferencesDto>).data
            emailNotifications = preferences.emailNotifications
            pushNotifications = preferences.pushNotifications
            inAppNotifications = preferences.inAppNotifications
            eventReminders = preferences.eventReminders
            commentNotifications = preferences.commentNotifications
            ratingNotifications = preferences.ratingNotifications
            ticketUpdates = preferences.ticketUpdates
            marketingNotifications = preferences.marketingNotifications
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt thông báo") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (preferencesState) {
                is ResourceState.Loading -> {
                    LoadingIndicator()
                }
                is ResourceState.Error -> {
                    val errorMessage = (preferencesState as ResourceState.Error).message
                    ErrorView(
                        message = errorMessage,
                        onRetry = { viewModel.getNotificationPreferences() }
                    )
                }
                is ResourceState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Kênh thông báo
                        Text(
                            text = "Kênh thông báo",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        NotificationChannelItem(
                            title = "Email",
                            description = "Nhận thông báo qua email",
                            checked = emailNotifications,
                            onCheckedChange = { emailNotifications = it }
                        )
                        
                        NotificationChannelItem(
                            title = "Push Notification",
                            description = "Nhận thông báo trên thiết bị",
                            checked = pushNotifications,
                            onCheckedChange = { pushNotifications = it }
                        )
                        
                        NotificationChannelItem(
                            title = "Trong ứng dụng",
                            description = "Hiển thị thông báo trong ứng dụng",
                            checked = inAppNotifications,
                            onCheckedChange = { inAppNotifications = it }
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Loại thông báo
                        Text(
                            text = "Loại thông báo",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        NotificationTypeItem(
                            title = "Nhắc nhở sự kiện",
                            description = "Thông báo về các sự kiện sắp diễn ra",
                            checked = eventReminders,
                            onCheckedChange = { eventReminders = it }
                        )
                        
                        NotificationTypeItem(
                            title = "Bình luận",
                            description = "Thông báo khi có bình luận mới",
                            checked = commentNotifications,
                            onCheckedChange = { commentNotifications = it }
                        )
                        
                        NotificationTypeItem(
                            title = "Đánh giá",
                            description = "Thông báo khi có đánh giá mới",
                            checked = ratingNotifications,
                            onCheckedChange = { ratingNotifications = it }
                        )
                        
                        NotificationTypeItem(
                            title = "Cập nhật vé",
                            description = "Thông báo khi có thay đổi về vé",
                            checked = ticketUpdates,
                            onCheckedChange = { ticketUpdates = it }
                        )
                        
                        NotificationTypeItem(
                            title = "Tiếp thị",
                            description = "Thông báo về khuyến mãi và sự kiện đặc biệt",
                            checked = marketingNotifications,
                            onCheckedChange = { marketingNotifications = it }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        AppButton(
                            onClick = {
                                val updatedPreferences = NotificationPreferencesDto(
                                    emailNotifications = emailNotifications,
                                    pushNotifications = pushNotifications,
                                    inAppNotifications = inAppNotifications,
                                    eventReminders = eventReminders,
                                    commentNotifications = commentNotifications,
                                    ratingNotifications = ratingNotifications,
                                    ticketUpdates = ticketUpdates,
                                    marketingNotifications = marketingNotifications
                                )
                                viewModel.updateNotificationPreferences(updatedPreferences)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Đã lưu cài đặt thông báo",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Lưu cài đặt")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun NotificationChannelItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun NotificationTypeItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
} 