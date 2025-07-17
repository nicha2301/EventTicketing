package com.nicha.eventticketing.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicOutlinedTextField
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onBackClick: () -> Unit
) {
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showTwoFactorDialog by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(false) }
    var autoLockEnabled by remember { mutableStateOf(true) }
    var sessionTimeout by remember { mutableStateOf("30 phút") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Bảo mật",
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
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Cài đặt bảo mật",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Xác thực",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Change password
                        SecuritySettingItem(
                            icon = Icons.Filled.Lock,
                            title = "Đổi mật khẩu",
                            subtitle = "Cập nhật mật khẩu tài khoản",
                            onClick = { showChangePasswordDialog = true }
                        )
                        
                        // Two-factor authentication
                        SecuritySettingItem(
                            icon = Icons.Filled.Security,
                            title = "Xác thực 2 yếu tố",
                            subtitle = "Bảo mật tài khoản với 2FA",
                            onClick = { showTwoFactorDialog = true }
                        )
                        
                        // Biometric authentication
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Xác thực sinh trắc học",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Đăng nhập bằng vân tay hoặc Face ID",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { biometricEnabled = it }
                            )
                        }
                    }
                }
            }
            
            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Phiên đăng nhập",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Auto lock
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Tự động khóa",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Tự động đăng xuất khi không sử dụng",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = autoLockEnabled,
                                onCheckedChange = { autoLockEnabled = it }
                            )
                        }
                        
                        // Session timeout
                        SecuritySettingItem(
                            icon = Icons.Filled.Schedule,
                            title = "Thời gian phiên",
                            subtitle = sessionTimeout,
                            onClick = { /* Show timeout options */ }
                        )
                    }
                }
            }
            
            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Hoạt động đăng nhập",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        SecuritySettingItem(
                            icon = Icons.Filled.History,
                            title = "Lịch sử đăng nhập",
                            subtitle = "Xem các phiên đăng nhập gần đây",
                            onClick = { /* Navigate to login history */ }
                        )
                        
                        SecuritySettingItem(
                            icon = Icons.Filled.Devices,
                            title = "Thiết bị đã đăng nhập",
                            subtitle = "Quản lý các thiết bị đang đăng nhập",
                            onClick = { /* Navigate to devices */ }
                        )
                    }
                }
            }
        }
    }
    
    // Change Password Dialog
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { oldPassword, newPassword, confirmPassword ->
                // Handle password change
                showChangePasswordDialog = false
            }
        )
    }
    
    // Two-Factor Authentication Dialog
    if (showTwoFactorDialog) {
        TwoFactorDialog(
            onDismiss = { showTwoFactorDialog = false },
            onConfirm = { /* Handle 2FA setup */ }
        )
    }
}

@Composable
fun SecuritySettingItem(
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

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showOldPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Đổi mật khẩu")
        },
        text = {
            Column {
                NeumorphicOutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = "Mật khẩu hiện tại",
                    placeholder = "Mật khẩu hiện tại",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                NeumorphicOutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Mật khẩu mới",
                    placeholder = "Mật khẩu mới",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                NeumorphicOutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Xác nhận mật khẩu mới",
                    placeholder = "Xác nhận mật khẩu mới",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(oldPassword, newPassword, confirmPassword) }
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun TwoFactorDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Xác thực 2 yếu tố")
        },
        text = {
            Text("Bạn có muốn bật xác thực 2 yếu tố để tăng cường bảo mật cho tài khoản?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Bật")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
} 