package com.nicha.eventticketing.ui.screens.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.buttons.PrimaryButton
import com.nicha.eventticketing.ui.theme.LocalNeumorphismStyle
import com.nicha.eventticketing.ui.theme.NeumorphicCard
import com.nicha.eventticketing.util.FormatUtils
import com.nicha.eventticketing.viewmodel.TicketViewModel
import kotlinx.coroutines.launch
import android.content.Intent
import android.content.Context
import android.provider.CalendarContract
import androidx.compose.foundation.text.selection.SelectionContainer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import com.nicha.eventticketing.ui.components.QRCodeImage
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import com.nicha.eventticketing.util.ImageUtils
import com.nicha.eventticketing.util.NetworkStatusObserver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: String,
    onBackClick: () -> Unit,
    viewModel: TicketViewModel = hiltViewModel()
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    val isDarkTheme = isSystemInDarkTheme()
    val isOnline by NetworkStatusObserver.observe(context).collectAsState(initial = true)
    
    // Update repository network status whenever it changes
    LaunchedEffect(isOnline) {
        viewModel.setNetworkStatus(isOnline)
    }
    
    // Dialog states
    var showCancelDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    // Collect state from ViewModel
    val ticketDetailState by viewModel.ticketDetailState.collectAsState()
    
    // Load ticket details when screen is displayed or network state changes
    LaunchedEffect(ticketId, isOnline) {
        viewModel.getTicketById(ticketId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Chi tiết vé",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Chia sẻ")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!isOnline) {
            Box(modifier = Modifier.fillMaxWidth().background(Color.Red).padding(8.dp)) {
                Text("Bạn đang offline. Dữ liệu vé có thể chưa mới nhất.", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }
        when (ticketDetailState) {
            is ResourceState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ResourceState.Success -> {
                val ticket = (ticketDetailState as ResourceState.Success<TicketDto>).data
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ticket Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = if (isDarkTheme) 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
                                else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Status Badge
                            TicketStatusBadge(status = ticket.status)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Event Image
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(ImageUtils.getFullImageUrl(ticket.eventImageUrl))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = ticket.eventTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                
                                // Gradient overlay for better text visibility
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.1f),
                                                    Color.Black.copy(alpha = 0.7f)
                                                ),
                                                startY = 0f,
                                                endY = 500f
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                )
                                
                                // Event title overlay at bottom
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = ticket.eventTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Date and Time
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                        text = "Thời gian",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    
                                    Text(
                                        text = FormatUtils.formatDate(ticket.eventStartDate, "dd/MM/yyyy HH:mm"),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            // Location
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                        text = "Địa điểm",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    
                                    Text(
                                        text = ticket.eventLocation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                            
                            // QR Code
                            Text(
                                text = "Mã QR vé",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .size(300.dp)
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                QRCodeImage(
                                    ticketId = ticket.id,
                                    ticketNumber = ticket.ticketNumber,
                                    eventId = ticket.eventId,
                                    userId = ticket.userId,
                                    qrCodeUrl = ticket.qrCodeUrl,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))                            
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                            
                            // Ticket Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Loại vé",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    
                                    Text(
                                        text = ticket.ticketTypeName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Giá vé",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    
                                    Text(
                                        text = FormatUtils.formatPrice(ticket.price),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Add to Calendar Button
                        Button(
                            onClick = {
                                addToCalendar(context, ticket)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text("Thêm vào lịch")
                        }
                        
                        // Cancel Ticket Button (only show if ticket is active)
                        if (ticket.status == "RESERVED" || ticket.status == "PAID") {
                            Button(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text("Hủy vé")
                            }
                        }
                    }
                }
                
                // Cancel Ticket Dialog
                if (showCancelDialog) {
                    AlertDialog(
                        onDismissRequest = { showCancelDialog = false },
                        title = { Text("Xác nhận hủy vé") },
                        text = { Text("Bạn có chắc chắn muốn hủy vé này không? Hành động này không thể hoàn tác.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.cancelTicket(ticketId)
                                        showCancelDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Hủy vé")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCancelDialog = false }) {
                                Text("Đóng")
                            }
                        }
                    )
                }
                
                // Share Ticket Dialog
                if (showShareDialog) {
                    AlertDialog(
                        onDismissRequest = { showShareDialog = false },
                        title = { Text("Chia sẻ vé") },
                        text = { Text("Chọn cách chia sẻ thông tin vé của bạn:") },
                        confirmButton = {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val shareText = "Tôi sẽ tham dự sự kiện ${ticket.eventTitle} vào ngày ${FormatUtils.formatDate(ticket.eventStartDate)}. Địa điểm: ${ticket.eventLocation}"
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ vé"))
                                        showShareDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Chia sẻ thông tin sự kiện")
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                TextButton(
                                    onClick = { showShareDialog = false },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Đóng")
                                }
                            }
                        },
                        dismissButton = null
                    )
                }
            }
            is ResourceState.Error -> {
                val errorMessage = (ticketDetailState as ResourceState.Error).message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.resetTicketDetailError()
                                    viewModel.getTicketById(ticketId)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh"
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text("Thử lại")
                        }
                    }
                }
            }
            else -> {
                // Initial state, do nothing
            }
        }
    }
}

@Composable
fun TicketStatusBadge(status: String) {
    val statusInfo = when (status) {
        "PAID" -> StatusInfo(
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            textColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.CheckCircle,
            text = "Đã thanh toán"
        )
        "RESERVED" -> StatusInfo(
            backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
            textColor = MaterialTheme.colorScheme.secondary,
            icon = Icons.Default.Pending,
            text = "Đã đặt chỗ"
        )
        "CHECKED_IN" -> StatusInfo(
            backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
            textColor = Color(0xFF4CAF50),
            icon = Icons.Default.Done,
            text = "Đã sử dụng"
        )
        "EXPIRED" -> StatusInfo(
            backgroundColor = Color(0xFFFF9800).copy(alpha = 0.1f),
            textColor = Color(0xFFFF9800),
            icon = Icons.Default.Schedule,
            text = "Đã hết hạn"
        )
        "CANCELLED" -> StatusInfo(
            backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
            textColor = MaterialTheme.colorScheme.error,
            icon = Icons.Default.Cancel,
            text = "Đã hủy"
        )
        else -> StatusInfo(
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Default.Info,
            text = status
        )
    }
    
    Surface(
        shape = RoundedCornerShape(50),
        color = statusInfo.backgroundColor,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = statusInfo.icon,
                contentDescription = null,
                tint = statusInfo.textColor,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = statusInfo.text,
                style = MaterialTheme.typography.labelMedium,
                color = statusInfo.textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Class to hold status information
data class StatusInfo(
    val backgroundColor: Color,
    val textColor: Color,
    val icon: ImageVector,
    val text: String
)

// Function to add event to calendar
private fun addToCalendar(context: Context, ticket: TicketDto) {
    try {
        val startMillis = parseDate(ticket.eventStartDate)?.time ?: System.currentTimeMillis()
        val endMillis = parseDate(ticket.eventEndDate)?.time ?: (startMillis + 3600000) // Default to 1 hour if end date not available
        
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, ticket.eventTitle)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, ticket.eventLocation)
            .putExtra(CalendarContract.Events.DESCRIPTION, "Mã vé: ${ticket.ticketNumber}")
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            .putExtra(CalendarContract.Events.HAS_ALARM, 1)
        
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle exception
    }
}

private fun parseDate(dateString: String): Date? {
    return try {
        val format = if (dateString.contains("T")) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        } else {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        }
        format.parse(dateString)
    } catch (e: Exception) {
        null
    }
}