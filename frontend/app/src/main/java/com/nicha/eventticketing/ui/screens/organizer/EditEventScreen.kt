package com.nicha.eventticketing.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.EventType
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.StyledDatePicker
import com.nicha.eventticketing.ui.components.StyledTextField
import com.nicha.eventticketing.ui.components.StyledTimePicker
import com.nicha.eventticketing.ui.components.app.AppButton
import com.nicha.eventticketing.ui.components.app.AppOutlinedButton
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.util.ImageUtils.getAllImageUrls
import com.nicha.eventticketing.viewmodel.OrganizerEventViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    var showSaveChangesDialog by remember { mutableStateOf(false) }
    
    // State to track original values for change detection
    var originalTitle by remember { mutableStateOf("") }
    var originalDescription by remember { mutableStateOf("") }
    var originalShortDescription by remember { mutableStateOf("") }
    var originalLocation by remember { mutableStateOf("") }
    var originalAddress by remember { mutableStateOf("") }
    var originalCity by remember { mutableStateOf("") }
    var originalMaxAttendees by remember { mutableStateOf("") }
    var originalIsPrivate by remember { mutableStateOf(false) }
    var originalIsFree by remember { mutableStateOf(false) }
    var originalStartDate by remember { mutableStateOf<Date?>(null) }
    var originalStartTime by remember { mutableStateOf<Date?>(null) }
    var originalEndDate by remember { mutableStateOf<Date?>(null) }
    var originalEndTime by remember { mutableStateOf<Date?>(null) }
    
    // Function to check if form has changes
    fun hasChanges(): Boolean {
        return title != originalTitle ||
                description != originalDescription ||
                shortDescription != originalShortDescription ||
                location != originalLocation ||
                address != originalAddress ||
                city != originalCity ||
                maxAttendees != originalMaxAttendees ||
                isPrivate != originalIsPrivate ||
                isFree != originalIsFree ||
                startDate != originalStartDate ||
                startTime != originalStartTime ||
                endDate != originalEndDate ||
                endTime != originalEndTime
    }
    
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
            
            // Save original values for change detection
            originalTitle = event.title
            originalDescription = event.description ?: ""
            originalShortDescription = event.shortDescription ?: ""
            originalLocation = event.locationName ?: ""
            originalAddress = event.address ?: ""
            originalCity = event.city ?: ""
            originalMaxAttendees = event.maxAttendees.toString()
            originalIsPrivate = event.isPrivate
            originalIsFree = event.isFree
            
            try {
                val formats = listOf(
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                )
                
                var startDateTime: Date? = null
                run breaking@ {
                    formats.forEach { format ->
                        try {
                            startDateTime = format.parse(event.startDate)
                            startDateTime?.let {
                                return@breaking
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
                
                if (startDateTime == null) {
                    try {
                        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                        startDateTime = isoFormat.parse(event.startDate)
                    } catch (e: Exception) {
                    }
                }
                
                startDateTime?.let {
                    startDate = it
                    startTime = it
                    originalStartDate = it
                    originalStartTime = it
                }
            } catch (e: Exception) {
                println("Error parsing start date: ${event.startDate}, error: ${e.message}")
            }
            
            try {
                val formats = listOf(
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                )
                
                var endDateTime: Date? = null
                run breaking@ {
                    formats.forEach { format ->
                        try {
                            endDateTime = format.parse(event.endDate)
                            endDateTime?.let {
                                return@breaking
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
                
                if (endDateTime == null) {
                    try {
                        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                        endDateTime = isoFormat.parse(event.endDate)
                    } catch (e: Exception) {
                    }
                }
                
                endDateTime?.let {
                    endDate = it
                    endTime = it
                    originalEndDate = it
                    originalEndTime = it
                }
            } catch (e: Exception) {
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
            
            val startDateString = try {
                val format1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                format1.format(startDateTime)
            } catch (e: Exception) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    "${dateFormat.format(startDateTime)} ${timeFormat.format(startDateTime)}"
                } catch (e2: Exception) {
                    val format3 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    format3.format(startDateTime)
                }
            }
            
            val endDateString = try {
                val format1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                format1.format(endDateTime)
            } catch (e: Exception) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    "${dateFormat.format(endDateTime)} ${timeFormat.format(endDateTime)}"
                } catch (e2: Exception) {
                    val format3 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    format3.format(endDateTime)
                }
            }
            
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
                title = { 
                    Text(
                        "Chỉnh sửa sự kiện",
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
                    IconButton(onClick = { onManageImagesClick(eventId) }) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Quản lý hình ảnh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                            AppButton(onClick = { viewModel.getEventById(eventId) }) {
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
                        NeumorphicCard(
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
                                
                                StyledTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = "Tên sự kiện",
                                    placeholder = "Nhập tên sự kiện",
                                    leadingIcon = Icons.Default.Event,
                                    singleLine = true
                                )
                                
                                StyledTextField(
                                    value = shortDescription,
                                    onValueChange = { shortDescription = it },
                                    label = "Mô tả ngắn",
                                    placeholder = "Nhập mô tả ngắn",
                                    leadingIcon = Icons.AutoMirrored.Filled.ShortText,
                                    maxLines = 2
                                )
                                
                                StyledTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = "Mô tả chi tiết",
                                    placeholder = "Nhập mô tả chi tiết",
                                    leadingIcon = Icons.Default.Description,
                                    maxLines = 5
                                )
                            }
                        }
                        
                        NeumorphicCard(
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
                                    StyledDatePicker(
                                        label = "Ngày bắt đầu",
                                        selectedDate = startDate,
                                        onDateSelected = { startDate = it },
                                        modifier = Modifier.weight(1f),
                                        isError = startDateError != null,
                                        errorMessage = startDateError
                                    )
                                    
                                    StyledTimePicker(
                                        label = "Giờ bắt đầu",
                                        selectedTime = startTime,
                                        onTimeSelected = { startTime = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StyledDatePicker(
                                        label = "Ngày kết thúc",
                                        selectedDate = endDate,
                                        onDateSelected = { endDate = it },
                                        modifier = Modifier.weight(1f),
                                        isError = endDateError != null,
                                        errorMessage = endDateError
                                    )
                                    
                                    StyledTimePicker(
                                        label = "Giờ kết thúc",
                                        selectedTime = endTime,
                                        onTimeSelected = { endTime = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        
                        // Địa điểm
                        NeumorphicCard(
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
                                
                                StyledTextField(
                                    value = location,
                                    onValueChange = { location = it },
                                    label = "Tên địa điểm",
                                    placeholder = "Nhập tên địa điểm",
                                    leadingIcon = Icons.Default.LocationOn,
                                    singleLine = true
                                )
                                
                                StyledTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    label = "Địa chỉ chi tiết",
                                    placeholder = "Nhập địa chỉ chi tiết",
                                    leadingIcon = Icons.Default.Home,
                                    singleLine = true
                                )
                                
                                StyledTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = "Thành phố",
                                    placeholder = "Nhập thành phố",
                                    leadingIcon = Icons.Default.LocationCity,
                                    singleLine = true
                                )
                            }
                        }
                        
                        // Cấu hình bổ sung
                        NeumorphicCard(
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
                                
                                StyledTextField(
                                    value = maxAttendees,
                                    onValueChange = { 
                                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                            maxAttendees = it
                                        }
                                    },
                                    label = "Số lượng người tham dự tối đa",
                                    placeholder = "Nhập số lượng tối đa",
                                    leadingIcon = Icons.Default.PeopleAlt,
                                    singleLine = true
                                )
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoneyOff,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Sự kiện miễn phí",
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.weight(1f)
                                            )
                                            com.nicha.eventticketing.ui.components.app.AppSwitch(
                                                checked = isFree,
                                                onCheckedChange = { isFree = it }
                                            )
                                        }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Sự kiện riêng tư",
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.weight(1f)
                                            )
                                            com.nicha.eventticketing.ui.components.app.AppSwitch(
                                                checked = isPrivate,
                                                onCheckedChange = { isPrivate = it }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        NeumorphicCard(
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
                                
                                Text(
                                    text = "Quản lý hình ảnh cho sự kiện của bạn",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                            AppButton(
                                    onClick = { 
                                        if (hasChanges()) {
                                            showSaveChangesDialog = true
                                        } else {
                                            onManageImagesClick(eventId)
                                        }
                                    },
                                modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Quản lý thư viện ảnh",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Get image URL from current event
                                        val imageUrls = remember(eventDetailState) {
                                            if (eventDetailState is ResourceState.Success) {
                                                val event = (eventDetailState as ResourceState.Success<EventDto>).data
                                                event.getAllImageUrls()
                                            } else {
                                                emptyList()
                                            }
                                        }
                                        
                                        if (imageUrls.isNotEmpty()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(imageUrls.first())
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Event banner",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddPhotoAlternate,
                                                    contentDescription = "Add image",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                    text = "Chưa có ảnh banner",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Nút cập nhật sự kiện
                        AppButton(
                            onClick = { submitForm() },
                            enabled = isFormValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Cập nhật sự kiện",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
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
                AppButton(
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
    
    if (showSaveChangesDialog) {
        AlertDialog(
            onDismissRequest = {
                showSaveChangesDialog = false
            },
            title = { 
                Text(
                    "Lưu thay đổi?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text(
                    "Bạn có những thay đổi chưa được lưu. Bạn có muốn lưu trước khi chuyển sang quản lý hình ảnh?",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                AppButton(
                    onClick = {
                        showSaveChangesDialog = false
                        submitForm()
                    },
                ) {
                    Text("Lưu và tiếp tục")
                }
            },
            dismissButton = {
                AppOutlinedButton(
                    onClick = {
                        showSaveChangesDialog = false
                        onManageImagesClick(eventId)
                    }
                ) {
                    Text("Bỏ qua thay đổi")
                }
            }
        )
    }
} 