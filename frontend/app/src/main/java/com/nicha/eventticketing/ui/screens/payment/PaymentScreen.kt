package com.nicha.eventticketing.ui.screens.payment

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nicha.eventticketing.R
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicGradientButton
import com.nicha.eventticketing.ui.theme.CardBackground
import com.nicha.eventticketing.ui.theme.LocalNeumorphismStyle
import com.nicha.eventticketing.util.FormatUtils
import com.nicha.eventticketing.viewmodel.EventViewModel
import com.nicha.eventticketing.viewmodel.PaymentViewModel
import com.nicha.eventticketing.viewmodel.TicketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import com.nicha.eventticketing.data.model.EventType
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke

// Data classes for PaymentScreen
data class EventEntity(
    val id: String,
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val address: String,
    val organizerId: String,
    val organizerName: String,
    val featuredImageUrl: String,
    val bannerImageUrl: String,
    val status: EventStatus,
    val type: EventType,
    val tags: List<String>,
    val createdAt: Date,
    val updatedAt: Date
)

enum class EventStatus {
    ACTIVE, INACTIVE, CANCELLED, COMPLETED
}

enum class PaymentMethod(val displayName: String, val code: String, val iconRes: Int? = null) {
    VNPAY("VNPAY", "vnpay", R.drawable.ic_vnpay),
    MOMO("MoMo", "momo", R.drawable.ic_momo),
    ZALOPAY("ZaloPay", "zalopay", R.drawable.ic_zalopay),
    BANK_TRANSFER("Chuyển khoản ngân hàng", "bank_transfer"),
    CASH("Tiền mặt", "cash")
}

// TicketType data class for PaymentScreen
data class TicketType(
    val id: String,
    val eventId: String,
    val name: String,
    val description: String,
    val price: Double,
    val quantity: Int,
    val quantitySold: Int,
    val maxPerOrder: Int,
    val minPerOrder: Int,
    val saleStartDate: String,
    val saleEndDate: String
)

// Temporary data class for payment screen
data class PaymentEventInfo(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val date: String,
    val time: String,
    val location: String,
    val organizer: String,
    val price: Double,
    val type: EventType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    eventId: String,
    ticketTypeId: String,
    quantity: Int,
    onBackClick: () -> Unit,
    onPaymentSuccess: () -> Unit,
    eventViewModel: EventViewModel = hiltViewModel(),
    ticketViewModel: TicketViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod>(PaymentMethod.VNPAY) }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Collect states from ViewModels
    val eventState by eventViewModel.eventDetailState.collectAsState()
    val ticketTypeState by ticketViewModel.ticketTypeState.collectAsState()
    val paymentState by paymentViewModel.paymentState.collectAsState()
    
    // Load data when screen is displayed
    LaunchedEffect(eventId, ticketTypeId) {
        eventViewModel.getEventById(eventId)
        ticketViewModel.getTicketTypeById(ticketTypeId)
    }
    
    // Handle payment state changes
    LaunchedEffect(paymentState) {
        when (paymentState) {
            is ResourceState.Loading -> {
                showLoadingDialog = true
            }
            is ResourceState.Success -> {
                showLoadingDialog = false
                val paymentResponse = (paymentState as ResourceState.Success).data
                
                // Mở URL thanh toán trong trình duyệt
                paymentResponse.paymentUrl?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
                
                // Trong thực tế, cần chờ callback từ cổng thanh toán
                // Ở đây giả định thanh toán thành công sau 5 giây
                delay(5000)
                showSuccessDialog = true
            }
            is ResourceState.Error -> {
                showLoadingDialog = false
                errorMessage = (paymentState as ResourceState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }
    
    // Get event and ticket type data
    val event = when (eventState) {
        is ResourceState.Success -> (eventState as ResourceState.Success).data
        else -> null
    }
    
    val ticketType = when (ticketTypeState) {
        is ResourceState.Success -> (ticketTypeState as ResourceState.Success).data
        else -> null
    }
    
    val totalAmount = ticketType?.price?.times(quantity) ?: 0.0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Thanh toán", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(8.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                spotColor = neumorphismStyle.darkShadowColor,
                                ambientColor = neumorphismStyle.lightShadowColor
                            )
                            .clip(CircleShape)
                            .background(CardBackground)
                    ) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Thông tin đơn hàng
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = "Biên lai"
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Thông tin đơn hàng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Event info
                    if (event != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            // Event image
                            AsyncImage(
                                model = event.featuredImageUrl,
                                contentDescription = event.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Event details
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = FormatUtils.formatDate(event.startDate),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = event.locationName.ifEmpty { event.address },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        // Loading skeleton or error message
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (eventState is ResourceState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else if (eventState is ResourceState.Error) {
                                Text(
                                    text = (eventState as ResourceState.Error).message,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    
                    // Ticket details
                    if (ticketType != null) {
                        Column(
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Loại vé:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = ticketType.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Số lượng:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = quantity.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Đơn giá:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = FormatUtils.formatPrice(ticketType.price),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // Loading skeleton or error message
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (ticketTypeState is ResourceState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else if (ticketTypeState is ResourceState.Error) {
                                Text(
                                    text = (ticketTypeState as ResourceState.Error).message,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    
                    // Total amount
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tổng cộng:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = FormatUtils.formatPrice(totalAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Payment methods
            Text(
                text = "Phương thức thanh toán",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            PaymentMethodSelector(
                selectedMethod = selectedPaymentMethod,
                onMethodSelected = { selectedPaymentMethod = it }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Pay button
            NeumorphicGradientButton(
                onClick = {
                    if (ticketType != null) {
                        // Tạo returnUrl dựa trên scheme của ứng dụng
                        val returnUrl = "eventticketing://payment/callback"
                        // Gọi API tạo thanh toán
                        paymentViewModel.createPayment(
                            ticketId = ticketType.id,
                            amount = totalAmount,
                            paymentMethod = selectedPaymentMethod.code,
                            returnUrl = returnUrl,
                            description = "Thanh toán vé sự kiện: ${event?.title ?: ""}"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                gradient = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                )
            ) {
                Text(
                    text = "Thanh toán ${FormatUtils.formatPrice(totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
    
    // Loading Dialog
    if (showLoadingDialog) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Đang xử lý thanh toán...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Thanh toán thành công!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cảm ơn bạn đã đặt vé. Vé của bạn đã được gửi đến email.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { onPaymentSuccess() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Xem vé của tôi")
                    }
                }
            }
        }
    }
    
    // Error Dialog
    if (showErrorDialog) {
        Dialog(onDismissRequest = { showErrorDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Thanh toán thất bại",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showErrorDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Thử lại")
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodSelector(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PaymentMethod.values().take(2).forEach { method ->
            PaymentMethodItem(
                method = method,
                isSelected = selectedMethod == method,
                onClick = { onMethodSelected(method) }
            )
        }
    }
}

@Composable
fun PaymentMethodItem(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) 
                MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio button
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .then(
                        if (isSelected) {
                            Modifier
                                .shadow(
                                    elevation = 2.dp,
                                    shape = CircleShape,
                                    spotColor = neumorphismStyle.darkShadowColor,
                                    ambientColor = neumorphismStyle.lightShadowColor
                                )
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        } else {
                            Modifier
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Method icon
            if (method.iconRes != null) {
                Icon(
                    painter = painterResource(id = method.iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(
                    imageVector = when (method) {
                        PaymentMethod.BANK_TRANSFER -> Icons.Filled.AccountBalance
                        PaymentMethod.CASH -> Icons.Filled.Money
                        else -> Icons.Filled.Payment
                    },
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Method name
            Text(
                text = method.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
} 