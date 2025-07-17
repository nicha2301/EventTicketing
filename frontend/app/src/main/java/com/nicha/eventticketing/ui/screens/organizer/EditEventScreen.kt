package com.nicha.eventticketing.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nicha.eventticketing.data.model.EventType
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.viewmodel.OrganizerEventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: String,
    onBackClick: () -> Unit,
    onEventUpdated: (String) -> Unit,
    viewModel: OrganizerEventViewModel = hiltViewModel()
) {
    val eventDetailState by viewModel.eventDetailState.collectAsState()
    val updateEventState by viewModel.updateEventState.collectAsState()
    
    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<EventType?>(null) }
    var categoryId by remember { mutableStateOf("") }
    
    // Dialog states
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Date and time pickers
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()
    
    // Format date and time
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // Category options
    val categories = listOf(
        EventType.MUSIC to "Âm nhạc",
        EventType.SPORT to "Thể thao",
        EventType.EDUCATION to "Giáo dục",
        EventType.TECHNOLOGY to "Công nghệ",
        EventType.ART to "Nghệ thuật"
    )
    
    // Fetch event data when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.getEventById(eventId)
    }
    
    // Update form fields when event data is loaded
    LaunchedEffect(eventDetailState) {
        if (eventDetailState is ResourceState.Success) {
            val event = (eventDetailState as ResourceState.Success<EventDto>).data
            title = event.title
            description = event.description ?: ""
            location = event.locationName ?: ""
            address = event.address ?: ""
            city = event.city ?: ""
            organizer = event.organizerName ?: ""
            imageUrl = event.featuredImageUrl ?: ""
            categoryId = event.categoryId ?: ""
            
            // Tìm EventType tương ứng với categoryId
            val categoryName = event.categoryName ?: ""
            selectedCategory = when {
                categoryName.contains("âm nhạc", ignoreCase = true) -> EventType.MUSIC
                categoryName.contains("thể thao", ignoreCase = true) -> EventType.SPORT
                categoryName.contains("giáo dục", ignoreCase = true) -> EventType.EDUCATION
                categoryName.contains("công nghệ", ignoreCase = true) -> EventType.TECHNOLOGY
                categoryName.contains("nghệ thuật", ignoreCase = true) -> EventType.ART
                else -> null
            }
        }
    }
    
    // Handle update event state changes
    LaunchedEffect(updateEventState) {
        if (updateEventState is ResourceState.Success) {
            showSuccessDialog = true
            viewModel.resetUpdateEventState()
        }
    }
    
    // Validate form
    val isFormValid = title.isNotBlank() && description.isNotBlank() && 
                     location.isNotBlank() && address.isNotBlank()
    
    // Handle form submission
    fun submitForm() {
        if (isFormValid) {
            // Gọi API để cập nhật sự kiện
            viewModel.updateEvent(
                eventId = eventId,
                title = title,
                description = description,
                locationName = location,
                address = address,
                city = city,
                categoryId = categoryId
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa sự kiện") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (eventDetailState) {
                is ResourceState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ResourceState.Error -> {
                    val errorMessage = (eventDetailState as ResourceState.Error).message
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.getEventById(eventId) }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                is ResourceState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Event image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable { /* Open image picker */ },
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Event image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Add image",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Thay đổi ảnh sự kiện",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                        
                        // Event title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Tên sự kiện") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Title,
                                    contentDescription = null
                                )
                            },
                            placeholder = { Text("Nhập tên sự kiện") }
                        )
                        
                        // Event description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Mô tả") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null
                                )
                            },
                            placeholder = { Text("Nhập mô tả chi tiết về sự kiện") }
                        )
                        
                        // Event location
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Địa điểm") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null
                                )
                            },
                            placeholder = { Text("Nhập tên địa điểm tổ chức") }
                        )
                        
                        // Event address
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Địa chỉ") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null
                                )
                            },
                            placeholder = { Text("Nhập địa chỉ chi tiết") }
                        )
                        
                        // Event city
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("Thành phố") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null
                                )
                            },
                            placeholder = { Text("Nhập tên thành phố") }
                        )
                        
                        // Submit button
                        Button(
                            onClick = { submitForm() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            enabled = isFormValid
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lưu thay đổi")
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Không có dữ liệu",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Loading indicator for update operation
            if (updateEventState is ResourceState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Success dialog
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showSuccessDialog = false
                        onEventUpdated(eventId)
                    },
                    title = { Text("Thành công") },
                    text = { Text("Sự kiện đã được cập nhật thành công!") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                onEventUpdated(eventId)
                            }
                        ) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
} 