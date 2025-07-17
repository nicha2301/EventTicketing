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
                title = { Text("Quản lý loại vé") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Tạo loại vé mới",
                    tint = MaterialTheme.colorScheme.onPrimary
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
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.getTicketTypes(eventId) }) {
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
                                
                                Button(onClick = { showCreateDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
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
                        Button(
                            onClick = {
                                viewModel.deleteTicketType(ticketTypeToDelete.id, eventId)
                                showDeleteConfirmDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Xóa")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = null }) {
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
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticketType.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa",
                            tint = MaterialTheme.colorScheme.primary
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
            
            if (!ticketType.description.isNullOrBlank()) {
                Text(
                    text = ticketType.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Giá",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${ticketType.price} VND",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Số lượng",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${ticketType.availableQuantity}/${ticketType.quantity}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Thêm thanh tiến trình hiển thị số lượng vé đã bán
            LinearProgressIndicator(
                progress = { 
                    if (ticketType.quantity > 0) 
                        ticketType.quantitySold.toFloat() / ticketType.quantity
                    else 0f
                },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = if (ticketType.availableQuantity < ticketType.quantity * 0.2)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
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
    
    // Date pickers
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
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
            ) {
                Text(
                    text = if (isEditing) "Chỉnh sửa loại vé" else "Tạo loại vé mới",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên loại vé") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập tên loại vé") }
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
                    onValueChange = { price = it },
                    label = { Text("Giá vé") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập giá vé") },
                    trailingIcon = { Text("VND", modifier = Modifier.padding(end = 8.dp)) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Số lượng") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập số lượng vé") }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Sales start date
                OutlinedTextField(
                    value = salesStartDate,
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
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Sales end date
                OutlinedTextField(
                    value = salesEndDate,
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
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = minTicketsPerOrder,
                        onValueChange = { minTicketsPerOrder = it },
                        label = { Text("Mua tối thiểu") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = maxTicketsPerCustomer,
                        onValueChange = { maxTicketsPerCustomer = it },
                        label = { Text("Mua tối đa") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val newTicketType = TicketTypeDto(
                                id = ticketType?.id ?: "",
                                eventId = eventId,
                                name = name,
                                description = description,
                                price = price.toDoubleOrNull() ?: 0.0,
                                quantity = quantity.toIntOrNull() ?: 100,
                                quantitySold = ticketType?.quantitySold ?: 0,
                                maxTicketsPerCustomer = maxTicketsPerCustomer.toIntOrNull(),
                                minTicketsPerOrder = minTicketsPerOrder.toIntOrNull() ?: 1,
                                salesStartDate = salesStartDate,
                                salesEndDate = salesEndDate
                            )
                            onSave(newTicketType)
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
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            salesStartDate = dateFormatter.format(Date(it))
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
    
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            salesEndDate = dateFormatter.format(Date(it))
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
} 