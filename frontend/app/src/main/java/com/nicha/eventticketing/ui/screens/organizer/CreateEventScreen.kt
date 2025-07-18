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
import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.data.remote.dto.location.LocationDto
import com.nicha.eventticketing.ui.components.LoadingDialog
import com.nicha.eventticketing.util.FormatUtils
import com.nicha.eventticketing.viewmodel.CreateEventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onBackClick: () -> Unit,
    onEventCreated: (String) -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var shortDescription by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var maxAttendees by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedLocationId by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var startTime by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var endTime by remember { mutableStateOf<Date?>(null) }
    var isPrivate by remember { mutableStateOf(false) }
    var isDraft by remember { mutableStateOf(true) }
    var isFree by remember { mutableStateOf(false) }
    
    // Form validation errors
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }
    
    // Dialog states
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showLocationDropdown by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    
    // Date pickers
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()
    
    // Validate form
    val isFormValid = title.isNotBlank() && description.isNotBlank() && 
                     shortDescription.isNotBlank() && address.isNotBlank() && 
                     city.isNotBlank() && maxAttendees.isNotBlank() && 
                     selectedCategoryId != null && selectedLocationId != null && 
                     startDate != null && startTime != null && 
                     endDate != null && endTime != null &&
                     startDateError == null && endDateError == null
    
    // Hàm kết hợp ngày và giờ
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
    
    // Kiểm tra ngày tháng
    fun validateDates() {
        val now = Calendar.getInstance().time
        
        startDate?.let { sDate ->
            startTime?.let { sTime ->
                val startDateTime = combineDateTime(sDate, sTime)
                if (startDateTime.before(now)) {
                    startDateError = "Ngày bắt đầu phải là ngày trong tương lai"
                } else {
                    startDateError = null
                    
                    // Kiểm tra ngày kết thúc
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
    
    // Validate ngày mỗi khi thay đổi
    LaunchedEffect(startDate, startTime, endDate, endTime) {
        validateDates()
    }
    
    // Handle form submission
    fun submitForm() {
        if (isFormValid) {
            // Kết hợp ngày và giờ bắt đầu
            val startDateTime = combineDateTime(startDate!!, startTime!!)
            val endDateTime = combineDateTime(endDate!!, endTime!!)
            
            // Định dạng ngày tháng theo yêu cầu của server (không có 'T')
            val startDateString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(startDateTime)
            val endDateString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(endDateTime)
            
            viewModel.createEvent(
                title = title,
                description = description,
                shortDescription = shortDescription,
                categoryId = selectedCategoryId!!,
                locationId = selectedLocationId!!,
                address = address,
                city = city,
                maxAttendees = maxAttendees.toInt(),
                startDate = startDateString,
                endDate = endDateString,
                isPrivate = isPrivate,
                isDraft = isDraft,
                isFree = isFree
            )
        }
    }

    // Observe UI state for navigation
    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateEventViewModel.UiState.Success -> {
                val eventId = (uiState as CreateEventViewModel.UiState.Success).eventId
                showSuccessDialog = true
                onEventCreated(eventId)
            }
            is CreateEventViewModel.UiState.Error -> {
                showErrorDialog = true
            }
            else -> { /* Loading or Idle */ }
        }
    }

    // Load categories and locations
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
        viewModel.loadLocations()
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
            // Event image (placeholder until image upload is implemented)
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
            
            // Short description
            OutlinedTextField(
                value = shortDescription,
                onValueChange = { shortDescription = it },
                label = { Text("Mô tả ngắn") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ShortText,
                        contentDescription = null
                    )
                },
                placeholder = { Text("Nhập mô tả ngắn gọn về sự kiện") }
            )
            
            // Event description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả chi tiết") },
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
            
            // Category selection
            OutlinedTextField(
                value = selectedCategoryId?.let { categoryId ->
                    categories.find { it.id == categoryId }?.name ?: ""
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
                placeholder = { Text("Chọn danh mục sự kiện") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showCategoryDropdown = true }) {
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
                    .heightIn(max = 300.dp)
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            selectedCategoryId = category.id
                            showCategoryDropdown = false
                        }
                    )
                }
            }
            
            // Location selection
            OutlinedTextField(
                value = selectedLocationId?.let { locationId ->
                    locations.find { it.id.toString() == locationId }?.name ?: ""
                } ?: "",
                onValueChange = { },
                label = { Text("Địa điểm") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null
                    )
                },
                placeholder = { Text("Chọn địa điểm tổ chức") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showLocationDropdown = true }) {
                        Icon(
                            imageVector = if (showLocationDropdown) 
                                Icons.Default.KeyboardArrowUp 
                            else 
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = "Chọn địa điểm"
                        )
                    }
                }
            )
            
            // Location dropdown
            DropdownMenu(
                expanded = showLocationDropdown,
                onDismissRequest = { showLocationDropdown = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 300.dp)
            ) {
                locations.forEach { location ->
                    DropdownMenuItem(
                        text = { 
                            Column {
                                Text(location.name)
                                Text(
                                    text = location.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            selectedLocationId = location.id.toString()
                            address = location.address
                            location.city?.let { city = it }
                            showLocationDropdown = false
                        }
                    )
                }
            }
            
            // Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Địa chỉ cụ thể") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null
                    )
                },
                placeholder = { Text("Nhập địa chỉ chi tiết") }
            )
            
            // City
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

            // Max attendees
            OutlinedTextField(
                value = maxAttendees,
                onValueChange = { maxAttendees = it },
                label = { Text("Số lượng người tối đa") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null
                    )
                },
                placeholder = { Text("Nhập số lượng người tham gia tối đa") }
            )
            
            // Event date and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start date
                OutlinedTextField(
                    value = startDate?.let { FormatUtils.formatDate(it) } ?: "",
                    onValueChange = { },
                    label = { Text("Ngày bắt đầu") },
                    modifier = Modifier.weight(1f),
                    isError = startDateError != null,
                    supportingText = startDateError?.let { 
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null
                        )
                    },
                    placeholder = { Text("Chọn ngày") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Chọn ngày"
                            )
                        }
                    }
                )
                
                // Start time
                OutlinedTextField(
                    value = startTime?.let { FormatUtils.formatDate(it, "HH:mm") } ?: "",
                    onValueChange = { },
                    label = { Text("Giờ bắt đầu") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null
                        )
                    },
                    placeholder = { Text("Chọn giờ") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showStartTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Chọn giờ"
                            )
                        }
                    }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // End date
                OutlinedTextField(
                    value = endDate?.let { FormatUtils.formatDate(it) } ?: "",
                    onValueChange = { },
                    label = { Text("Ngày kết thúc") },
                    modifier = Modifier.weight(1f),
                    isError = endDateError != null,
                    supportingText = endDateError?.let { 
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null
                        )
                    },
                    placeholder = { Text("Chọn ngày") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Chọn ngày"
                            )
                        }
                    }
                )
                
                // End time
                OutlinedTextField(
                    value = endTime?.let { FormatUtils.formatDate(it, "HH:mm") } ?: "",
                    onValueChange = { },
                    label = { Text("Giờ kết thúc") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null
                        )
                    },
                    placeholder = { Text("Chọn giờ") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showEndTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Chọn giờ"
                            )
                        }
                    }
                )
            }
            
            // Event options
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tùy chọn sự kiện",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sự kiện riêng tư")
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Lưu nháp (Không công khai)")
                        Switch(
                            checked = isDraft,
                            onCheckedChange = { isDraft = it }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sự kiện miễn phí")
                        Switch(
                            checked = isFree,
                            onCheckedChange = { isFree = it }
                        )
                    }
                }
            }
            
            // Submit button
            Button(
                onClick = { submitForm() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = isFormValid && !isLoading
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
    
    // Date picker dialogs
    if (showStartDatePicker) {
        val today = Calendar.getInstance()
        
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val selectedDate = Date(it)
                            // Kiểm tra ngày đã chọn
                            val calendar = Calendar.getInstance()
                            calendar.time = selectedDate
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            
                            val todayCal = Calendar.getInstance()
                            todayCal.set(Calendar.HOUR_OF_DAY, 0)
                            todayCal.set(Calendar.MINUTE, 0)
                            todayCal.set(Calendar.SECOND, 0)
                            todayCal.set(Calendar.MILLISECOND, 0)
                            
                            if (calendar.before(todayCal)) {
                                startDateError = "Ngày bắt đầu phải là ngày trong tương lai"
                            } else {
                                startDate = selectedDate
                                startDateError = null
                            }
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time picker dialog
    if (showStartTimePicker) {
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text("Chọn giờ bắt đầu") },
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
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        calendar.set(Calendar.SECOND, 0)
                        startTime = calendar.time
                       
                        // Kiểm tra thời gian bắt đầu phải trong tương lai
                        startDate?.let { sDate ->
                            val now = Calendar.getInstance()
                            val startCal = Calendar.getInstance()
                            startCal.time = sDate
                            startCal.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                            startCal.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                            
                            // Nếu là ngày hôm nay, kiểm tra giờ
                            val todayCal = Calendar.getInstance()
                            todayCal.set(Calendar.HOUR_OF_DAY, 0)
                            todayCal.set(Calendar.MINUTE, 0)
                            todayCal.set(Calendar.SECOND, 0)
                            todayCal.set(Calendar.MILLISECOND, 0)
                            
                            if (FormatUtils.formatDate(sDate) == FormatUtils.formatDate(now.time) && 
                                startCal.before(now)) {
                                startDateError = "Thời gian bắt đầu phải là thời gian trong tương lai"
                            } else {
                                startDateError = null
                                
                                // Kiểm tra thời gian kết thúc nếu cùng ngày
                                endDate?.let { eDate ->
                                    endTime?.let { eTime ->
                                        if (FormatUtils.formatDate(sDate) == FormatUtils.formatDate(eDate)) {
                                            val endCal = Calendar.getInstance()
                                            endCal.time = eTime
                                            
                                            if (endCal.before(startCal) || endCal.timeInMillis == startCal.timeInMillis) {
                                                endDateError = "Thời gian kết thúc phải sau thời gian bắt đầu"
                                            } else {
                                                endDateError = null
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        showStartTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    
    // End date picker dialog
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val selectedDate = Date(it)
                            // Kiểm tra ngày đã chọn với ngày bắt đầu
                            startDate?.let { sDate ->
                                val startCal = Calendar.getInstance()
                                startCal.time = sDate
                                startCal.set(Calendar.HOUR_OF_DAY, 0)
                                startCal.set(Calendar.MINUTE, 0)
                                startCal.set(Calendar.SECOND, 0)
                                startCal.set(Calendar.MILLISECOND, 0)
                                
                                val selectedCal = Calendar.getInstance()
                                selectedCal.time = selectedDate
                                selectedCal.set(Calendar.HOUR_OF_DAY, 0)
                                selectedCal.set(Calendar.MINUTE, 0)
                                selectedCal.set(Calendar.SECOND, 0)
                                selectedCal.set(Calendar.MILLISECOND, 0)
                                
                                if (selectedCal.before(startCal)) {
                                    endDateError = "Ngày kết thúc phải từ ngày bắt đầu trở đi"
                                } else {
                                    endDate = selectedDate
                                    endDateError = null
                                }
                            } ?: run {
                                endDate = selectedDate
                            }
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // End time picker dialog
    if (showEndTimePicker) {
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text("Chọn giờ kết thúc") },
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
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        calendar.set(Calendar.SECOND, 0)
                        endTime = calendar.time
                        
                        // Kiểm tra thời gian kết thúc phải sau thời gian bắt đầu nếu cùng ngày
                        startDate?.let { sDate ->
                            startTime?.let { sTime ->
                                endDate?.let { eDate ->
                                    // Nếu cùng ngày, kiểm tra giờ
                                    if (FormatUtils.formatDate(sDate) == FormatUtils.formatDate(eDate)) {
                                        val startCal = Calendar.getInstance()
                                        startCal.time = sTime
                                        
                                        val endCal = Calendar.getInstance()
                                        endCal.time = calendar.time
                                        
                                        if (endCal.before(startCal) || endCal.timeInMillis == startCal.timeInMillis) {
                                            endDateError = "Thời gian kết thúc phải sau thời gian bắt đầu"
                                        } else {
                                            endDateError = null
                                        }
                                    }
                                }
                            }
                        }
                        
                        showEndTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    
    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Thành công") },
            text = { Text("Sự kiện đã được tạo thành công!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Lỗi") },
            text = { 
                val errorMessage = error ?: "Không thể tạo sự kiện. Vui lòng thử lại sau."
                val errorText = when {
                    errorMessage.contains("Ngày bắt đầu phải là ngày trong tương lai") -> 
                        "Ngày bắt đầu phải là ngày trong tương lai"
                    errorMessage.contains("rejected value") && errorMessage.contains("startDate") -> 
                        "Ngày bắt đầu phải là ngày trong tương lai"
                    errorMessage.contains("endDate") -> 
                        "Ngày kết thúc phải sau ngày bắt đầu"
                    else -> 
                        errorMessage
                }
                Text(errorText) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Loading dialog
    if (isLoading) {
        LoadingDialog(message = "Đang tạo sự kiện...")
    }
} 