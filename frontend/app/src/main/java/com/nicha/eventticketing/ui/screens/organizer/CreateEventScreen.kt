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
import coil.compose.AsyncImage
import com.nicha.eventticketing.data.model.EventType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onBackClick: () -> Unit,
    onEventCreated: (String) -> Unit
) {
    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("https://picsum.photos/id/1/500/300") } // Placeholder image
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<EventType?>(null) }
    
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
    
    // Validate form
    val isFormValid = title.isNotBlank() && description.isNotBlank() && 
                     location.isNotBlank() && organizer.isNotBlank() && 
                     price.isNotBlank() && selectedDate != null && 
                     selectedTime != null && selectedCategory != null
    
    // Handle form submission
    fun submitForm() {
        if (isFormValid) {
            // Trong thực tế, sẽ gọi API để tạo sự kiện ở đây
            showSuccessDialog = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo sự kiện mới") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                            text = "Tải lên ảnh sự kiện",
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
            
            // Event date and time
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Date
                OutlinedTextField(
                    value = selectedDate?.let { dateFormatter.format(it) } ?: "",
                    onValueChange = { },
                    label = { Text("Ngày") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null
                        )
                    },
                    placeholder = { Text("DD/MM/YYYY") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Chọn ngày"
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Time
                OutlinedTextField(
                    value = selectedTime ?: "",
                    onValueChange = { },
                    label = { Text("Giờ") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null
                        )
                    },
                    placeholder = { Text("HH:MM") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Chọn giờ"
                            )
                        }
                    }
                )
            }
            
            // Event category
            OutlinedTextField(
                value = selectedCategory?.let { category ->
                    categories.find { it.first == category }?.second ?: ""
                } ?: "",
                onValueChange = { },
                label = { Text("Danh mục") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null
                    )
                },
                placeholder = { Text("Chọn danh mục") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showCategoryDropdown = !showCategoryDropdown }) {
                        Icon(
                            imageVector = if (showCategoryDropdown) 
                                Icons.Default.KeyboardArrowUp 
                            else 
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = "Chọn danh mục"
                        )
                    }
                }
            )
            
            // Category dropdown
            DropdownMenu(
                expanded = showCategoryDropdown,
                onDismissRequest = { showCategoryDropdown = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 16.dp)
            ) {
                categories.forEach { (type, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            selectedCategory = type
                            showCategoryDropdown = false
                        }
                    )
                }
            }
            
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
                placeholder = { Text("Nhập địa điểm tổ chức") }
            )
            
            // Event organizer
            OutlinedTextField(
                value = organizer,
                onValueChange = { organizer = it },
                label = { Text("Ban tổ chức") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null
                    )
                },
                placeholder = { Text("Nhập tên ban tổ chức") }
            )
            
            // Event price
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Giá vé") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null
                    )
                },
                placeholder = { Text("Nhập giá vé (VND)") },
                trailingIcon = {
                    Text(
                        text = "VND",
                        modifier = Modifier.padding(end = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tạo sự kiện")
            }
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = Date(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time picker dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Chọn thời gian") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        selectedTime = String.format("%02d:%02d", hour, minute)
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    
    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Thành công") },
            text = { Text("Sự kiện đã được tạo thành công!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onEventCreated("new_event_id") // Giả định ID sự kiện mới
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
} 