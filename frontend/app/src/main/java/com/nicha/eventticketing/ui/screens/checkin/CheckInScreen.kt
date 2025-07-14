package com.nicha.eventticketing.ui.screens.checkin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.nicha.eventticketing.data.model.EventType
import com.nicha.eventticketing.data.model.TicketStatus
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    onBackClick: () -> Unit,
    onScanComplete: () -> Unit = {}
) {
    // State for scanner animation
    var scannerLinePosition by remember { mutableStateOf(0f) }
    var scanningActive by remember { mutableStateOf(true) }
    var showScanResult by remember { mutableStateOf(false) }
    var scanSuccess by remember { mutableStateOf(false) }
    
    // Ticket info after scanning
    var ticketInfo by remember { mutableStateOf<TicketInfo?>(null) }
    
    // Animate scanner line
    LaunchedEffect(scanningActive) {
        if (scanningActive) {
            while (true) {
                // Animate from top to bottom
                for (i in 0..100) {
                    scannerLinePosition = i / 100f
                    delay(10)
                }
                // Animate from bottom to top
                for (i in 100 downTo 0) {
                    scannerLinePosition = i / 100f
                    delay(10)
                }
            }
        }
    }
    
    // Simulate scan result after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        scanningActive = false
        scanSuccess = true
        ticketInfo = TicketInfo(
            id = "TICKET-12345",
            eventTitle = "Lễ hội âm nhạc Hà Nội",
            eventDate = "15/12/2023",
            eventTime = "19:00",
            eventImageUrl = "https://picsum.photos/id/1/300/200",
            ticketType = "VIP",
            attendeeName = "Nguyễn Văn A"
        )
        showScanResult = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quét mã QR") },
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
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Camera preview (simulated)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Scanner overlay
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                ) {
                    // Scanner line
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(230.dp)
                            .background(MaterialTheme.colorScheme.primary)
                            .align(
                                when {
                                    scannerLinePosition < 0.33f -> Alignment.TopCenter
                                    scannerLinePosition < 0.66f -> Alignment.Center
                                    else -> Alignment.BottomCenter
                                }
                            )
                    )
                }
                
                // Instructions
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Đặt mã QR vào khung để quét",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Giữ điện thoại cách mã QR khoảng 15-20cm",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    
    // Hiển thị kết quả quét
    if (showScanResult && ticketInfo != null) {
        Dialog(onDismissRequest = { showScanResult = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                if (scanSuccess) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.error,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (scanSuccess) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = if (scanSuccess) "Thành công" else "Thất bại",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Status text
                    Text(
                        text = if (scanSuccess) "Check-in thành công" else "Check-in thất bại",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (scanSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Ticket info
                    if (scanSuccess) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = ticketInfo?.eventImageUrl,
                                        contentDescription = ticketInfo?.eventTitle,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Column {
                                        Text(
                                            text = ticketInfo?.eventTitle ?: "",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        Text(
                                            text = "${ticketInfo?.eventDate} | ${ticketInfo?.eventTime}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Loại vé",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        Text(
                                            text = ticketInfo?.ticketType ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Mã vé",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        Text(
                                            text = ticketInfo?.id ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Check-in lúc: ${java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(java.util.Date())}",
                                        modifier = Modifier.padding(8.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Vé không hợp lệ hoặc đã được sử dụng",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            showScanResult = false
                            onScanComplete()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Đóng")
                    }
                }
            }
        }
    }
}

// Data class for ticket info after scanning
data class TicketInfo(
    val id: String,
    val eventTitle: String,
    val eventDate: String,
    val eventTime: String,
    val eventImageUrl: String,
    val ticketType: String,
    val attendeeName: String
) 