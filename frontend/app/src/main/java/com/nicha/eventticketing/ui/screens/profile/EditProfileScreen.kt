package com.nicha.eventticketing.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicOutlinedTextField
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.viewmodel.ProfileState
import com.nicha.eventticketing.viewmodel.ProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // User data from ViewModel
    val profileState by viewModel.profileState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    
    // Local state for form fields
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    
    // Track if form has been modified
    var hasChanges by remember { mutableStateOf(false) }
    
    // Validation states
    var fullNameError by remember { mutableStateOf<String?>(null) }
    
    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize form fields with user data
    LaunchedEffect(userProfile) {
        userProfile?.let {
            fullName = it.fullName
            phoneNumber = it.phoneNumber ?: ""
            // Reset change tracking when profile loads
            hasChanges = false
        }
    }
    
    // Handle profile state changes
    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileState.Success -> {
                // Only navigate back if we've actually saved changes
                // This prevents the immediate return when the screen loads
                if (hasChanges) {
                    onSaveClick()
                }
            }
            is ProfileState.Error -> {
                snackbarHostState.showSnackbar((profileState as ProfileState.Error).message)
                viewModel.resetError()
            }
            else -> {}
        }
    }
    
    // Loading indicator
    if (profileState is ProfileState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    // Function to save changes
    val saveChanges: () -> Unit = {
        // Validate input
        fullNameError = if (fullName.isBlank()) "Họ tên không được để trống" else null
        
        // Submit if valid
        if (fullNameError == null) {
            viewModel.updateProfile(
                fullName = fullName,
                phoneNumber = phoneNumber.takeIf { it.isNotBlank() }
            )
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Vui lòng kiểm tra lại thông tin")
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Chỉnh sửa hồ sơ",
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
                    IconButton(
                        onClick = saveChanges
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Lưu"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar section
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ảnh đại diện",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { /* Handle avatar change */ }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thay đổi ảnh")
                    }
                }
            }
            
            // Personal info section
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Thông tin cá nhân",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Full name
                    NeumorphicOutlinedTextField(
                        value = fullName,
                        onValueChange = { 
                            fullName = it 
                            fullNameError = null
                            hasChanges = true
                        },
                        label = "Họ và tên",
                        placeholder = "Nhập họ và tên",
                        modifier = Modifier.fillMaxWidth(),
                        isError = fullNameError != null,
                        errorMessage = fullNameError
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Email
                    NeumorphicOutlinedTextField(
                        value = userProfile?.email ?: "",
                        onValueChange = { /* Email is read-only */ },
                        label = "Email",
                        placeholder = "Email không thể thay đổi",
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Phone
                    NeumorphicOutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { 
                            phoneNumber = it
                            hasChanges = true
                        },
                        label = "Số điện thoại",
                        placeholder = "Nhập số điện thoại",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save button
            Button(
                onClick = saveChanges,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lưu thay đổi")
            }
        }
    }
} 