package com.nicha.eventticketing.ui.screens.organizer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import com.nicha.eventticketing.ui.components.app.AppButton
import com.nicha.eventticketing.ui.components.app.AppTextButton
import com.nicha.eventticketing.ui.components.app.AppOutlinedButton
import com.nicha.eventticketing.ui.components.app.AppDestructiveButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.util.FormatUtils
import com.nicha.eventticketing.viewmodel.TicketTypeManagementViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import java.util.Calendar
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.ui.theme.LocalNeumorphismStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketTypeListScreen(
    eventId: String,
    onBackClick: () -> Unit,
    viewModel: TicketTypeManagementViewModel = hiltViewModel()
) {
    val ticketTypesState by viewModel.ticketTypesState.collectAsState()
    val deleteTicketTypeState by viewModel.deleteTicketTypeState.collectAsState()
    val createTicketTypeState by viewModel.createTicketTypeState.collectAsState()
    val updateTicketTypeState by viewModel.updateTicketTypeState.collectAsState()
    
    var showDeleteConfirmDialog by remember { mutableStateOf<TicketTypeDto?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<TicketTypeDto?>(null) }
    
    // Fetch ticket types when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.getTicketTypes(eventId)
    }
    
    // Handle state changes
    LaunchedEffect(deleteTicketTypeState) {
        if (deleteTicketTypeState is ResourceState.Success) {
            viewModel.resetDeleteTicketTypeState()
            viewModel.getTicketTypes(eventId)
        }
    }
    
    LaunchedEffect(createTicketTypeState) {
        if (createTicketTypeState is ResourceState.Success) {
            viewModel.resetCreateTicketTypeState()
            viewModel.getTicketTypes(eventId)
        }
    }
    
    LaunchedEffect(updateTicketTypeState) {
        if (updateTicketTypeState is ResourceState.Success) {
            viewModel.resetUpdateTicketTypeState()
            viewModel.getTicketTypes(eventId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Quản lý loại vé",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.onSurface,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Tạo loại vé mới",
                    tint = MaterialTheme.colorScheme.surface
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (ticketTypesState) {
                is ResourceState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ResourceState.Error -> {
                    val errorMessage = (ticketTypesState as ResourceState.Error).message
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
                                text = "Không thể tải danh sách loại vé",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            AppButton(
                                onClick = { viewModel.getTicketTypes(eventId) },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Thử lại")
                            }
                        }
                    }
                }
                is ResourceState.Success -> {
                    val ticketTypes = (ticketTypesState as ResourceState.Success<List<TicketTypeDto>>).data
                    
                    if (ticketTypes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Chưa có loại vé nào",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Tạo loại vé để bắt đầu bán vé cho sự kiện của bạn",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                AppButton(onClick = { showCreateDialog = true }) {
                                                                    Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tạo loại vé mới")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(ticketTypes) { ticketType ->
                                TicketTypeItem(
                                    ticketType = ticketType,
                                    onEditClick = { showEditDialog = ticketType },
                                    onDeleteClick = { showDeleteConfirmDialog = ticketType }
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
            
            // Delete confirmation dialog
            showDeleteConfirmDialog?.let { ticketTypeToDelete ->
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = null },
                    title = { Text("Xác nhận xóa") },
                    text = { Text("Bạn có chắc chắn muốn xóa loại vé '${ticketTypeToDelete.name}' không?") },
                    confirmButton = {
                        AppDestructiveButton(
                            onClick = {
                                viewModel.deleteTicketType(ticketTypeToDelete.id, eventId)
                                showDeleteConfirmDialog = null
                            },
                        ) {
                            Text("Xóa")
                        }
                    },
                    dismissButton = {
                        AppTextButton(onClick = { showDeleteConfirmDialog = null }) {
                            Text("Hủy")
                        }
                    }
                )
            }
            
            // Create ticket type dialog
            if (showCreateDialog) {
                TicketTypeFormDialog(
                    eventId = eventId,
                    onDismiss = { showCreateDialog = false },
                    onSave = { newTicketType ->
                        viewModel.createTicketType(newTicketType)
                        showCreateDialog = false
                    }
                )
            }
            
            // Edit ticket type dialog
            showEditDialog?.let { ticketType ->
                TicketTypeFormDialog(
                    eventId = eventId,
                    ticketType = ticketType,
                    onDismiss = { showEditDialog = null },
                    onSave = { updatedTicketType ->
                        viewModel.updateTicketType(updatedTicketType)
                        showEditDialog = null
                    }
                )
            }
            
            // Loading indicator for operations
            if (deleteTicketTypeState is ResourceState.Loading || 
                createTicketTypeState is ResourceState.Loading || 
                updateTicketTypeState is ResourceState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun TicketTypeItem(
    ticketType: TicketTypeDto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ticketType.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (ticketType.isActive) 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            else 
                                MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (ticketType.isActive) "Đang bán" else "Ngừng bán",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (ticketType.isActive) 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else 
                                    MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Còn ${ticketType.availableQuantity}/${ticketType.quantity} vé",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Giá vé",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = if (ticketType.price > 0) 
                            FormatUtils.formatPrice(ticketType.price)
                        else 
                            "Miễn phí",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Đã bán",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "${ticketType.quantitySold} vé",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (ticketType.description?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Mô tả",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = ticketType.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (ticketType.salesStartDate != null || ticketType.salesEndDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Thời gian bán",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val startDate = ticketType.salesStartDate?.let { 
                    try { FormatUtils.formatDate(it) } catch (e: Exception) { null } 
                }
                val endDate = ticketType.salesEndDate?.let { 
                    try { FormatUtils.formatDate(it) } catch (e: Exception) { null } 
                }
                
                Text(
                    text = when {
                        startDate != null && endDate != null -> "Từ $startDate đến $endDate"
                        startDate != null -> "Từ $startDate"
                        endDate != null -> "Đến $endDate"
                        else -> "Không giới hạn"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketTypeFormDialog(
    eventId: String,
    ticketType: TicketTypeDto? = null,
    onDismiss: () -> Unit,
    onSave: (TicketTypeDto) -> Unit
) {
    val isEditing = ticketType != null
    
    var name by remember { mutableStateOf(ticketType?.name ?: "") }
    var description by remember { mutableStateOf(ticketType?.description ?: "") }
    var price by remember { mutableStateOf(ticketType?.price?.toString() ?: "0") }
    var quantity by remember { mutableStateOf(ticketType?.quantity?.toString() ?: "100") }
    var salesStartDate by remember { mutableStateOf(ticketType?.salesStartDate ?: "") }
    var salesEndDate by remember { mutableStateOf(ticketType?.salesEndDate ?: "") }
    var maxTicketsPerCustomer by remember { mutableStateOf(ticketType?.maxTicketsPerCustomer?.toString() ?: "5") }
    var minTicketsPerOrder by remember { mutableStateOf(ticketType?.minTicketsPerOrder?.toString() ?: "1") }
    var isEarlyBird by remember { mutableStateOf(ticketType?.isEarlyBird ?: false) }
    var isVIP by remember { mutableStateOf(ticketType?.isVIP ?: false) }
    var isActive by remember { mutableStateOf(ticketType?.isActive ?: true) }
    
    // Error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var minMaxError by remember { mutableStateOf<String?>(null) }
    
    // Date pickers
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDate = remember { Calendar.getInstance() }
    
    // Validate function
    val validateForm = {
        var isValid = true
        
        // Validate name
        if (name.isBlank()) {
            nameError = "Tên loại vé không được để trống"
            isValid = false
        } else {
            nameError = null
        }
        
        // Validate price
        val priceValue = price.toDoubleOrNull()
        if (priceValue == null) {
            priceError = "Giá vé phải là số"
            isValid = false
        } else if (priceValue < 0) {
            priceError = "Giá vé không được âm"
            isValid = false
        } else {
            priceError = null
        }
        
        // Validate quantity
        val quantityValue = quantity.toIntOrNull()
        if (quantityValue == null) {
            quantityError = "Số lượng phải là số nguyên"
            isValid = false
        } else if (quantityValue <= 0) {
            quantityError = "Số lượng phải lớn hơn 0"
            isValid = false
        } else {
            quantityError = null
        }
        
        // Validate dates
        if (salesStartDate.isNotEmpty() && salesEndDate.isNotEmpty()) {
            try {
                val startDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(salesStartDate)
                val endDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(salesEndDate)
                
                if (startDate != null && endDate != null) {
                    if (startDate.after(endDate)) {
                        dateError = "Ngày bắt đầu không thể sau ngày kết thúc"
                        isValid = false
                    } else {
                        dateError = null
                    }
                }
            } catch (e: Exception) {
                dateError = "Định dạng ngày không hợp lệ"
                isValid = false
            }
        }
        
        // Validate min/max tickets
        val minTickets = minTicketsPerOrder.toIntOrNull()
        val maxTickets = maxTicketsPerCustomer.toIntOrNull()
        
        if (minTickets != null && maxTickets != null) {
            if (minTickets > maxTickets) {
                minMaxError = "Số vé tối thiểu không thể lớn hơn số vé tối đa"
                isValid = false
            } else {
                minMaxError = null
            }
        }
        
        isValid
    }
    
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isEditing) "Chỉnh sửa loại vé" else "Tạo loại vé mới",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Tên loại vé") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập tên loại vé") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập mô tả loại vé") }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it; priceError = null },
                    label = { Text("Giá vé") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập giá vé") },
                    trailingIcon = { Text("VNĐ", modifier = Modifier.padding(end = 8.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = priceError != null,
                    supportingText = { priceError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it; quantityError = null },
                    label = { Text("Số lượng") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập số lượng vé") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = quantityError != null,
                    supportingText = { quantityError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Sales start date
                OutlinedTextField(
                    value = if (salesStartDate.isNotEmpty()) 
                        FormatUtils.formatDate(salesStartDate) 
                    else "",
                    onValueChange = { },
                    label = { Text("Ngày bắt đầu bán") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Chọn ngày"
                            )
                        }
                    },
                    isError = dateError != null
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Sales end date
                OutlinedTextField(
                    value = if (salesEndDate.isNotEmpty()) 
                        FormatUtils.formatDate(salesEndDate) 
                    else "",
                    onValueChange = { },
                    label = { Text("Ngày kết thúc bán") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Chọn ngày"
                            )
                        }
                    },
                    isError = dateError != null
                )
                
                if (dateError != null) {
                    Text(
                        text = dateError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = minTicketsPerOrder,
                        onValueChange = { minTicketsPerOrder = it; minMaxError = null },
                        label = { Text("Mua tối thiểu") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = minMaxError != null
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = maxTicketsPerCustomer,
                        onValueChange = { maxTicketsPerCustomer = it; minMaxError = null },
                        label = { Text("Mua tối đa") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = minMaxError != null
                    )
                }
                
                if (minMaxError != null) {
                    Text(
                        text = minMaxError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Checkboxes for additional options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isEarlyBird,
                        onCheckedChange = { isEarlyBird = it }
                    )
                    Text(
                        text = "Vé Early Bird",
                        modifier = Modifier.clickable { isEarlyBird = !isEarlyBird }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isVIP,
                        onCheckedChange = { isVIP = it }
                    )
                    Text(
                        text = "Vé VIP",
                        modifier = Modifier.clickable { isVIP = !isVIP }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                    Text(
                        text = "Kích hoạt",
                        modifier = Modifier.clickable { isActive = !isActive }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppTextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    AppButton(
                        onClick = {
                            if (validateForm()) {
                                val newTicketType = TicketTypeDto(
                                    id = ticketType?.id ?: "",
                                    eventId = eventId,
                                    name = name,
                                    description = description,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    quantity = quantity.toIntOrNull() ?: 100,
                                    availableQuantity = ticketType?.availableQuantity ?: quantity.toIntOrNull() ?: 100,
                                    quantitySold = ticketType?.quantitySold ?: 0,
                                    maxTicketsPerCustomer = maxTicketsPerCustomer.toIntOrNull(),
                                    minTicketsPerOrder = minTicketsPerOrder.toIntOrNull() ?: 1,
                                    salesStartDate = salesStartDate,
                                    salesEndDate = salesEndDate,
                                    isEarlyBird = isEarlyBird,
                                    isVIP = isVIP,
                                    isActive = isActive
                                )
                                onSave(newTicketType)
                            }
                        },
                        enabled = name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
                    ) {
                        Text(if (isEditing) "Cập nhật" else "Tạo loại vé")
                    }
                }
            }
        }
    }
    
    // Date picker dialogs
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val selectedDate = Calendar.getInstance().apply {
                                timeInMillis = it
                            }
                            
                            // Add validation for start date
                            if (selectedDate.before(currentDate)) {
                                dateError = "Ngày bắt đầu không thể trước ngày hiện tại"
                            } else {
                                salesStartDate = dateFormatter.format(Date(it)) + "T00:00:00"
                                dateError = null
                            }
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { showStartDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val selectedDate = Calendar.getInstance().apply {
                                timeInMillis = it
                            }
                            
                            // Add validation for end date
                            if (selectedDate.before(currentDate)) {
                                dateError = "Ngày kết thúc không thể trước ngày hiện tại"
                            } else if (salesStartDate.isNotEmpty()) {
                                try {
                                    val startDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(salesStartDate)
                                    if (startDate != null && selectedDate.time.before(startDate)) {
                                        dateError = "Ngày kết thúc không thể trước ngày bắt đầu"
                                    } else {
                                        salesEndDate = dateFormatter.format(Date(it)) + "T23:59:59"
                                        dateError = null
                                    }
                                } catch (e: Exception) {
                                    salesEndDate = dateFormatter.format(Date(it)) + "T23:59:59"
                                }
                            } else {
                                salesEndDate = dateFormatter.format(Date(it)) + "T23:59:59"
                                dateError = null
                            }
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { showEndDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
} 