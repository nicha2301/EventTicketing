package com.nicha.eventticketing.ui.screens.organizer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nicha.eventticketing.ui.components.LoadingDialog
import com.nicha.eventticketing.ui.components.StyledDatePicker
import com.nicha.eventticketing.ui.components.StyledDropdown
import com.nicha.eventticketing.ui.components.StyledTimePicker
import com.nicha.eventticketing.ui.components.app.AppButton
import com.nicha.eventticketing.ui.components.app.AppSwitch
import com.nicha.eventticketing.ui.components.app.AppTextButton
import com.nicha.eventticketing.ui.components.app.AppTextField
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.viewmodel.CreateEventViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    var selectedImageUris by remember { mutableStateOf(emptyList<Uri>()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImageUris = uris
    }

    // Form validation errors
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }

    // Dialog states
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    // Validate form
    val isFormValid = title.isNotBlank() && description.isNotBlank() &&
            shortDescription.isNotBlank() && address.isNotBlank() &&
            city.isNotBlank() && maxAttendees.isNotBlank() &&
            selectedCategoryId != null && selectedLocationId != null &&
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

    // Handle form submission
    fun submitForm() {
        if (isFormValid) {
            val startDateTime = combineDateTime(startDate!!, startTime!!)
            val endDateTime = combineDateTime(endDate!!, endTime!!)

            val startDateString =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(startDateTime)
            val endDateString =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(endDateTime)

            viewModel.createEventWithImages(
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
                isFree = isFree,
                imageUris = selectedImageUris
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

            else -> { /* Loading or Idle */
            }
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
                title = {
                    Text(
                        "Tạo sự kiện mới",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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

                    AppTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "Nhập tên sự kiện",
                        singleLine = true
                    )

                    AppTextField(
                        value = shortDescription,
                        onValueChange = { shortDescription = it },
                        placeholder = "Nhập mô tả ngắn",
                        singleLine = true
                    )

                    AppTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Nhập mô tả chi tiết",
                        singleLine = false
                    )

                    StyledDropdown(
                        label = "Danh mục",
                        items = categories,
                        selectedItem = categories.find { it.id == selectedCategoryId },
                        onItemSelected = { selectedCategoryId = it.id },
                        itemToString = { it.name },
                        leadingIcon = Icons.Default.Category,
                        placeholder = "Chọn danh mục"
                    )
                }
            }

            // Hình ảnh sự kiện
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
                        text = "Chọn nhiều hình ảnh cho sự kiện",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable {
                                imagePickerLauncher.launch("image/*")
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Thêm ảnh",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nhấn để chọn ảnh",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (selectedImageUris.isNotEmpty()) {
                        Text(
                            text = "Đã chọn ${selectedImageUris.size} ảnh",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedImageUris) { uri ->
                                Box(
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Selected image",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    IconButton(
                                        onClick = {
                                            selectedImageUris =
                                                selectedImageUris.filter { it != uri }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.6f),
                                                RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Xóa ảnh",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Thời gian và địa điểm
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Thời gian và địa điểm",
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
                            onDateSelected = {
                                startDate = it
                                validateDates()
                            },
                            modifier = Modifier.weight(1f),
                            isError = startDateError != null,
                            errorMessage = startDateError
                        )

                        StyledTimePicker(
                            label = "Giờ bắt đầu",
                            selectedTime = startTime,
                            onTimeSelected = {
                                startTime = it
                                validateDates()
                            },
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
                            onDateSelected = {
                                endDate = it
                                validateDates()
                            },
                            modifier = Modifier.weight(1f),
                            isError = endDateError != null,
                            errorMessage = endDateError
                        )

                        StyledTimePicker(
                            label = "Giờ kết thúc",
                            selectedTime = endTime,
                            onTimeSelected = {
                                endTime = it
                                validateDates()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    StyledDropdown(
                        label = "Địa điểm",
                        items = locations,
                        selectedItem = locations.find { it.id == selectedLocationId },
                        onItemSelected = {
                            selectedLocationId = it.id
                            address = it.address
                            city = it.city
                        },
                        itemToString = { it.name },
                        leadingIcon = Icons.Default.LocationOn,
                        placeholder = "Chọn địa điểm"
                    )

                    AppTextField(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = "Nhập địa chỉ chi tiết",
                        singleLine = true
                    )

                    AppTextField(
                        value = city,
                        onValueChange = { city = it },
                        placeholder = "Nhập tên thành phố",
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

                    AppTextField(
                        value = maxAttendees,
                        onValueChange = {
                            // Chỉ cho phép nhập số
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                maxAttendees = it
                            }
                        },
                        placeholder = "Nhập số lượng người tối đa",
                        singleLine = true
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

                        AppSwitch(
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

                        AppSwitch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lưu dưới dạng bản nháp",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )

                        AppSwitch(
                            checked = isDraft,
                            onCheckedChange = { isDraft = it }
                        )
                    }
                }
            }

            // Nút tạo sự kiện
            AppButton(
                onClick = { submitForm() },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isDraft) "Lưu bản nháp" else "Tạo sự kiện",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Thành công") },
            text = { Text("Sự kiện đã được tạo thành công!") },
            confirmButton = {
                AppTextButton(
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
                AppTextButton(
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