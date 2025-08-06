package com.nicha.eventticketing.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nicha.eventticketing.domain.model.EventType
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
    onManageImagesClick: (String) -> Unit,
    viewModel: OrganizerEventViewModel = hiltViewModel()
) {
    val eventDetailState by viewModel.eventDetailState.collectAsState()
    val updateEventState by viewModel.updateEventState.collectAsState()
    
    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var shortDescription by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var startTime by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var endTime by remember { mutableStateOf<Date?>(null) }
    var selectedCategory by remember { mutableStateOf<EventType?>(null) }
    var categoryId by remember { mutableStateOf("") }
    var maxAttendees by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var isFree by remember { mutableStateOf(false) }
    
    // Dialog states
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Date and time validation
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }
    
    // Form validation
    val isFormValid = title.isNotBlank() && description.isNotBlank() && 
                     shortDescription.isNotBlank() && address.isNotBlank() && 
                     city.isNotBlank() && maxAttendees.isNotBlank() && 
                     startDate != null && startTime != null && 
                     endDate != null && endTime != null &&
                     startDateError == null && endDateError == null
    
    fun combineDateTime(date: Date, time: Date): Date {
        val calendar1 = Calendar.getInstance()
        calendar1.time = date
        
        val calendar2 = Calendar.getInstance()
        calendar2.time = time
        
        calendar1.set(Calendar.HOUR_OF_DAY, calendar2.get(Calendar.HOUR_OF_DAY))
        calendar1.set(Calendar.MINUTE, calendar2.get(Calendar.MINUTE))
        calendar1.set(Calendar.SECOND, 0)
        
        return calendar1.time
    }
    
    fun validateDates() {
        val now = Calendar.getInstance().time
        
        startDate?.let { sDate ->
            startTime?.let { sTime ->
                val startDateTime = combineDateTime(sDate, sTime)
                if (startDateTime.before(now)) {
                    startDateError = "Ngày bắt đầu phải là ngày trong tương lai"
                } else {
                    startDateError = null
                    
                    endDate?.let { eDate ->
                        endTime?.let { eTime ->
                            val endDateTime = combineDateTime(eDate, eTime)
                            if (endDateTime.before(startDateTime) || endDateTime == startDateTime) {
                                endDateError = "Ngày kết thúc phải sau ngày bắt đầu"
                            } else {
                                endDateError = null
                            }
                        }
                    }
                }
            }
        }
    }
    
    LaunchedEffect(startDate, startTime, endDate, endTime) {
        validateDates()
    }
    
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
            shortDescription = event.shortDescription ?: ""
            location = event.locationName ?: ""
            address = event.address ?: ""
            city = event.city ?: ""
            organizer = event.organizerName ?: ""
            imageUrl = event.featuredImageUrl ?: ""
            categoryId = event.categoryId ?: ""
            maxAttendees = event.maxAttendees.toString()
            isPrivate = event.isPrivate
            isFree = event.isFree
            
            try {
                val startDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(event.startDate)
                startDate = startDateTime
                startTime = startDateTime
            } catch (e: Exception) {
                // Handle error
            }
            
            try {
                val endDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(event.endDate)
                endDate = endDateTime
                endTime = endDateTime
            } catch (e: Exception) {
                // Handle error
            }
            
            val categoryName = event.categoryName ?: ""
            selectedCategory = when {
                categoryName.contains("âm nhạc", ignoreCase = true) -> EventType.MUSIC
                categoryName.contains("thể thao", ignoreCase = true) -> EventType.SPORT
                categoryName.contains("giáo dục", ignoreCase = true) -> EventType.EDUCATION
                categoryName.contains("công nghệ", ignoreCase = true) -> EventType.TECHNOLOGY
                categoryName.contains("nghệ thuật", ignoreCase = true) -> EventType.ART
                else -> EventType.MUSIC
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
    
    // Handle form submission
    fun submitForm() {
        if (isFormValid && startDate != null && startTime != null && endDate != null && endTime != null) {
            val startDateTime = combineDateTime(startDate!!, startTime!!)
            val endDateTime = combineDateTime(endDate!!, endTime!!)
            
            val startDateString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(startDateTime)
            val endDateString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(endDateTime)
            
            val updatedEvent = EventDto(
                id = eventId,
                title = title,
                description = description,
                shortDescription = shortDescription,
                organizerId = "",
                organizerName = organizer,
                categoryId = categoryId,
                categoryName = selectedCategory?.name ?: "",
                locationId = "",
                locationName = location,
                address = address,
                city = city,
                latitude = null,
                longitude = null,
                status = "DRAFT",
                maxAttendees = maxAttendees.toIntOrNull() ?: 0,
                currentAttendees = 0,
                featuredImageUrl = imageUrl,
                imageUrls = emptyList(),
                minTicketPrice = if (isFree) 0.0 else null,
                maxTicketPrice = if (isFree) 0.0 else null,
                startDate = startDateString,
                endDate = endDateString,
                createdAt = "",
                updatedAt = "",
                isPrivate = isPrivate,
                isFeatured = false,
                isFree = isFree,
                ticketTypes = null
            )
            
            viewModel.updateEvent(eventId, updatedEvent)
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
                },
                actions = {
                    IconButton(onClick = { onManageImagesClick(eventId) }) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Quản lý hình ảnh"
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
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Thông tin cơ bản",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Tên sự kiện") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Event, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                
                                OutlinedTextField(
                                    value = shortDescription,
                                    onValueChange = { shortDescription = it },
                                    label = { Text("Mô tả ngắn") },
                                    leadingIcon = {
                                        Icon(Icons.AutoMirrored.Filled.ShortText, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2
                                )
                                
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Mô tả chi tiết") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Description, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 5
                                )
                            }
                        }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Thời gian tổ chức",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = startDate?.let { dateFormatter.format(it) } ?: "",
                                        onValueChange = { },
                                        label = { Text("Ngày bắt đầu") },
                                        leadingIcon = {
                                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                                        },
                                        modifier = Modifier.weight(1f),
                                        readOnly = true,
                                        isError = startDateError != null,
                                        supportingText = startDateError?.let { { Text(it) } },
                                        trailingIcon = {
                                            IconButton(onClick = { showDatePicker = true }) {
                                                Icon(Icons.Default.Edit, contentDescription = null)
                                            }
                                        }
                                    )
                                    
                                    OutlinedTextField(
                                        value = startTime?.let { timeFormatter.format(it) } ?: "",
                                        onValueChange = { },
                                        label = { Text("Giờ bắt đầu") },
                                        leadingIcon = {
                                            Icon(Icons.Default.AccessTime, contentDescription = null)
                                        },
                                        modifier = Modifier.weight(1f),
                                        readOnly = true,
                                        trailingIcon = {
                                            IconButton(onClick = { showTimePicker = true }) {
                                                Icon(Icons.Default.Edit, contentDescription = null)
                                            }
                                        }
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = endDate?.let { dateFormatter.format(it) } ?: "",
                                        onValueChange = { },
                                        label = { Text("Ngày kết thúc") },
                                        leadingIcon = {
                                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                                        },
                                        modifier = Modifier.weight(1f),
                                        readOnly = true,
                                        isError = endDateError != null,
                                        supportingText = endDateError?.let { { Text(it) } },
                                        trailingIcon = {
                                            IconButton(onClick = { showDatePicker = true }) {
                                                Icon(Icons.Default.Edit, contentDescription = null)
                                            }
                                        }
                                    )
                                    
                                    OutlinedTextField(
                                        value = endTime?.let { timeFormatter.format(it) } ?: "",
                                        onValueChange = { },
                                        label = { Text("Giờ kết thúc") },
                                        leadingIcon = {
                                            Icon(Icons.Default.AccessTime, contentDescription = null)
                                        },
                                        modifier = Modifier.weight(1f),
                                        readOnly = true,
                                        trailingIcon = {
                                            IconButton(onClick = { showTimePicker = true }) {
                                                Icon(Icons.Default.Edit, contentDescription = null)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Địa điểm",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                OutlinedTextField(
                                    value = location,
                                    onValueChange = { location = it },
                                    label = { Text("Tên địa điểm") },
                                    leadingIcon = {
                                        Icon(Icons.Default.LocationOn, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                
                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    label = { Text("Địa chỉ chi tiết") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Home, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = { Text("Thành phố") },
                                    leadingIcon = {
                                        Icon(Icons.Default.LocationCity, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Cấu hình bổ sung",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                OutlinedTextField(
                                    value = maxAttendees,
                                    onValueChange = { 
                                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                            maxAttendees = it
                                        }
                                    },
                                    label = { Text("Số lượng người tham dự tối đa") },
                                    leadingIcon = {
                                        Icon(Icons.Default.PeopleAlt, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sự kiện miễn phí",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Switch(
                                        checked = isFree,
                                        onCheckedChange = { isFree = it }
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sự kiện riêng tư",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Switch(
                                        checked = isPrivate,
                                        onCheckedChange = { isPrivate = it }
                                    )
                                }
                            }
                        }
                        
                        // Event image
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Hình ảnh sự kiện",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                // Nút quản lý hình ảnh
                                Button(
                                    onClick = { onManageImagesClick(eventId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Quản lý hình ảnh sự kiện")
                                }
                                
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable { onManageImagesClick(eventId) },
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
                                        contentDescription = "Manage images",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Nhấn để quản lý hình ảnh",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                            }
                        }
                        
                        // Nút cập nhật sự kiện
                        Button(
                            onClick = { submitForm() },
                            enabled = isFormValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Cập nhật sự kiện",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
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