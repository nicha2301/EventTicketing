package com.nicha.eventticketing.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicGradientButton
import com.nicha.eventticketing.ui.theme.CardBackground
import com.nicha.eventticketing.ui.theme.LocalNeumorphismStyle
import com.nicha.eventticketing.viewmodel.ProfileState
import com.nicha.eventticketing.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerProfileScreen(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMyEventsClick: () -> Unit,
    onCreateEventClick: () -> Unit,
    onEventDashboardClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onScanQRClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val profileState by viewModel.profileState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }
    
    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Error) {
            val errorMessage = (profileState as ProfileState.Error).message
            snackbarHostState.showSnackbar(message = errorMessage)
            viewModel.resetError()
        }
    }
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val isLoading = profileState is ProfileState.Loading
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Xác nhận đăng xuất") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogoutClick()
                    }
                ) {
                    Text("Đăng xuất")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Hồ sơ nhà tổ chức",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Chỉnh sửa"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = neumorphismStyle.darkShadowColor,
                            ambientColor = neumorphismStyle.lightShadowColor
                        )
                        .clip(CircleShape)
                        .background(CardBackground)
                        .border(
                            width = 4.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // User image or placeholder
                    AsyncImage(
                        model = userProfile?.profilePictureUrl ?: "https://picsum.photos/id/64/120/120",
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // User name
                Text(
                    text = userProfile?.fullName ?: "Đang tải...",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Email
                Text(
                    text = userProfile?.email ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                // Badge for organizer
                Surface(
                    modifier = Modifier.padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Nhà tổ chức",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Personal Information Section
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Thông tin cá nhân",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Phone number
                        ProfileInfoItem(
                            icon = Icons.Filled.Phone,
                            label = "Số điện thoại",
                            value = userProfile?.phoneNumber ?: "Chưa cập nhật"
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // Role
                        ProfileInfoItem(
                            icon = Icons.Filled.Badge,
                            label = "Vai trò",
                            value = when(userProfile?.role) {
                                "USER" -> "Người dùng"
                                "ORGANIZER" -> "Nhà tổ chức"
                                "ADMIN" -> "Quản trị viên"
                                else -> userProfile?.role ?: "Không xác định"
                            }
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // Created date
                        ProfileInfoItem(
                            icon = Icons.Filled.CalendarToday,
                            label = "Ngày tạo tài khoản",
                            value = userProfile?.createdAt ?: "Không xác định"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Organizer Tools Section
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Công cụ tổ chức sự kiện",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // My Events
                        ProfileSettingItem(
                            icon = Icons.Filled.EventNote,
                            title = "Sự kiện của tôi",
                            subtitle = "Quản lý các sự kiện đã tạo",
                            onClick = onMyEventsClick
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // Create Event
                        ProfileSettingItem(
                            icon = Icons.Filled.AddCircle,
                            title = "Tạo sự kiện mới",
                            subtitle = "Tạo và cấu hình sự kiện mới",
                            onClick = onCreateEventClick
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // Dashboard
                        ProfileSettingItem(
                            icon = Icons.Filled.Dashboard,
                            title = "Bảng điều khiển",
                            subtitle = "Xem tổng quan các sự kiện",
                            onClick = onEventDashboardClick
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // Analytics
                        ProfileSettingItem(
                            icon = Icons.Filled.Analytics,
                            title = "Thống kê & Phân tích",
                            subtitle = "Xem báo cáo và thống kê sự kiện",
                            onClick = onAnalyticsClick
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // Scan QR
                        ProfileSettingItem(
                            icon = Icons.Filled.QrCode2,
                            title = "Quét mã QR",
                            subtitle = "Quét vé và check-in người tham dự",
                            onClick = onScanQRClick
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Account Settings Section
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Cài đặt tài khoản",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Settings item
                        ProfileSettingItem(
                            icon = Icons.Filled.Settings,
                            title = "Cài đặt",
                            subtitle = "Quản lý cài đặt tài khoản",
                            onClick = onSettingsClick
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Logout button
                NeumorphicGradientButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Red,
                            Color.Red.copy(alpha = 0.8f)
                        )
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đăng xuất",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ProfileSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
} 