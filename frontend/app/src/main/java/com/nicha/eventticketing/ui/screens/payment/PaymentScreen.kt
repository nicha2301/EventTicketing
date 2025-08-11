package com.nicha.eventticketing.ui.screens.payment

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseItemDto
import com.nicha.eventticketing.domain.model.PaymentMethod
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.app.AppButton
import com.nicha.eventticketing.ui.components.app.AppOutlinedButton
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicGradientButton
import com.nicha.eventticketing.ui.theme.LocalNeumorphismStyle
import com.nicha.eventticketing.util.FormatUtils
import com.nicha.eventticketing.util.ImageUtils.getPrimaryImageUrl
import com.nicha.eventticketing.viewmodel.EventViewModel
import com.nicha.eventticketing.viewmodel.PaymentViewModel
import com.nicha.eventticketing.viewmodel.TicketViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    eventId: String,
    ticketTypeId: String,
    quantity: Int,
    existingTicketId: String? = null,
    onBackClick: () -> Unit,
    onPaymentSuccess: () -> Unit,
    eventViewModel: EventViewModel = hiltViewModel(),
    ticketViewModel: TicketViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val neumorphismStyle = LocalNeumorphismStyle.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod>(PaymentMethod.MOMO) }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var waitingForPayment by remember { mutableStateOf(false) }
    var currentPaymentId by remember { mutableStateOf<String?>(null) }
    var paymentStartTime by remember { mutableStateOf<Long?>(null) }

    // Collect states from ViewModels
    val eventState by eventViewModel.eventDetailState.collectAsState()
    val ticketTypeState by ticketViewModel.ticketTypeState.collectAsState()
    val ticketPurchaseState by ticketViewModel.purchaseState.collectAsState()
    val paymentState by paymentViewModel.paymentState.collectAsState()
    val existingUnpaidTicketState by ticketViewModel.existingUnpaidTicketState.collectAsState()

    // Load data when screen is displayed
    LaunchedEffect(eventId, ticketTypeId, existingTicketId) {
        eventViewModel.getEventById(eventId)
        
        // If no existing ticket ID provided, check for existing unpaid ticket
        if (existingTicketId == null) {
            ticketViewModel.checkExistingUnpaidTicket(eventId, ticketTypeId)
        }
    }

    // Monitor app lifecycle to check payment status when app resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            
            if (event == Lifecycle.Event.ON_RESUME && waitingForPayment && currentPaymentId != null) {
                val currentTime = System.currentTimeMillis()
                val startTime = paymentStartTime ?: currentTime
                val elapsedTime = currentTime - startTime
                val timeoutMillis = 10 * 60 * 1000L
                
                if (elapsedTime > timeoutMillis) {
                    waitingForPayment = false
                    currentPaymentId = null
                    paymentStartTime = null
                    errorMessage = "Phiên thanh toán đã hết hạn. Vui lòng thử lại."
                    showErrorDialog = true
                } else {
                    paymentViewModel.checkPaymentStatusAfterReturn(currentPaymentId!!, maxRetries = 5)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }    // Handle ticket purchase state changes
    LaunchedEffect(ticketPurchaseState) {
        when (ticketPurchaseState) {
            is ResourceState.Loading -> {
                showLoadingDialog = true
            }
            is ResourceState.Success -> {
                val purchaseResponse = (ticketPurchaseState as ResourceState.Success).data
                val orderId = purchaseResponse.orderId
                val totalAmount = purchaseResponse.totalAmount
                
                val firstTicket = purchaseResponse.tickets.firstOrNull()
                if (firstTicket != null) {
                    paymentViewModel.setSelectedPaymentMethod(selectedPaymentMethod.code)
                    paymentViewModel.createPayment(
                        ticketId = firstTicket.id,
                        amount = totalAmount,
                        description = "Thanh toán đơn hàng #$orderId"
                    )
                } else {
                    showLoadingDialog = false
                    errorMessage = "Không tìm thấy thông tin vé"
                    showErrorDialog = true
                }
                
                ticketViewModel.clearPurchaseState()
            }
            is ResourceState.Error -> {
                showLoadingDialog = false
                errorMessage = (ticketPurchaseState as ResourceState.Error).message
                showErrorDialog = true
                ticketViewModel.clearPurchaseState()
            }
            else -> {}
        }
    }

    // Handle payment state changes
    LaunchedEffect(paymentState) {
        when (paymentState) {
            is ResourceState.Loading -> {
                showLoadingDialog = true
            }
            is ResourceState.Success -> {
                showLoadingDialog = false
                val successState = paymentState as ResourceState.Success
                val payment = successState.data
                
                when (payment.status.uppercase()) {
                    "PENDING" -> {
                        payment.paymentUrl?.let { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                            
                            waitingForPayment = true
                            currentPaymentId = payment.id ?: payment.paymentId
                            paymentStartTime = System.currentTimeMillis()
                            
                            paymentViewModel.resetPaymentState()
                        }
                    }
                    "COMPLETED" -> {
                        showSuccessDialog = true
                        waitingForPayment = false
                        currentPaymentId = null
                        paymentStartTime = null
                    }
                    else -> {
                    }
                }
            }
            is ResourceState.Error -> {
                showLoadingDialog = false
                val errorState = paymentState as ResourceState.Error
                showErrorDialog = true
                errorMessage = errorState.message
                waitingForPayment = false
                currentPaymentId = null  
                paymentStartTime = null
            }
            is ResourceState.Initial -> {
            }
        }
    }
    
    // Get event and ticket type data
    val event = when (eventState) {
        is ResourceState.Success -> (eventState as ResourceState.Success).data
        else -> null
    }
    
    val ticketType = event?.ticketTypes?.find { it.id == ticketTypeId }
    
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
                                model = event.getPrimaryImageUrl(),
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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ConfirmationNumber,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = ticketType.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                ticketType.description?.let { description ->
                                    if (description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Số lượng:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$quantity vé",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Đơn giá:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = FormatUtils.formatPrice(ticketType.price),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                if (ticketType.availableQuantity > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Inventory,
                                            contentDescription = null,
                                            tint = if (ticketType.availableQuantity < 10) 
                                                MaterialTheme.colorScheme.error 
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Còn lại: ${ticketType.availableQuantity} vé",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (ticketType.availableQuantity < 10) 
                                                MaterialTheme.colorScheme.error 
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
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
                            when (eventState) {
                                is ResourceState.Loading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                                is ResourceState.Error -> {
                                    Text(
                                        text = "Không thể tải thông tin loại vé",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "Không tìm thấy loại vé",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    
                    // Calculation details
                    if (ticketType != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${ticketType.name} x ${quantity}:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FormatUtils.formatPrice(ticketType.price * quantity),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Total amount
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
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
                            fontWeight = FontWeight.Bold
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
                    if (event != null && ticketType != null) {
                        val ticketIdToUse = existingTicketId 
                            ?: (existingUnpaidTicketState as? ResourceState.Success)?.data?.id
                        
                        if (ticketIdToUse != null) {
                            paymentViewModel.setSelectedPaymentMethod(selectedPaymentMethod.code)
                            paymentViewModel.createPayment(
                                ticketId = ticketIdToUse,
                                amount = totalAmount,
                                description = "Thanh toán cho vé ${ticketType.name}"
                            )
                        } else {
                            val ticketItems = listOf(
                                TicketPurchaseItemDto(
                                    ticketTypeId = ticketType.id,
                                    quantity = quantity
                                )
                            )
                            
                            ticketViewModel.purchaseTickets(
                                eventId = event.id,
                                ticketItems = ticketItems,
                                buyerName = "Guest User", 
                                buyerEmail = "guest@example.com",  
                                buyerPhone = "0123456789"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                gradient = Brush.horizontalGradient(
                    colors = listOf(
                                        MaterialTheme.colorScheme.onSurface,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (ticketType != null) {
                            "Thanh toán ${quantity} vé ${ticketType.name}"
                        } else {
                            "Thanh toán"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = FormatUtils.formatPrice(totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
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
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Thanh toán thành công!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Cảm ơn bạn đã đặt vé. Vé của bạn đã được gửi đến email và có thể xem trong ví vé.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppOutlinedButton(
                            onClick = { 
                                showSuccessDialog = false
                                onBackClick() 
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Về trang chủ",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                        
                        AppButton(
                            onClick = { 
                                showSuccessDialog = false
                                onPaymentSuccess() 
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Xem vé",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Error Dialog
    if (showErrorDialog) {
        Dialog(onDismissRequest = { showErrorDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Thanh toán không thành công",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppOutlinedButton(
                            onClick = { 
                                showErrorDialog = false
                                onBackClick()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Hủy",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                        
                        AppButton(
                            onClick = { 
                                showErrorDialog = false
                                if (event != null && ticketType != null) {
                                    val ticketItems = listOf(
                                        TicketPurchaseItemDto(
                                            ticketTypeId = ticketType.id,
                                            quantity = quantity
                                        )
                                    )
                                    
                                    ticketViewModel.purchaseTickets(
                                        eventId = event.id,
                                        ticketItems = ticketItems,
                                        buyerName = "Guest User",
                                        buyerEmail = "guest@example.com", 
                                        buyerPhone = "0123456789"
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Thử lại",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
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
        PaymentMethod.values().forEach { method ->
            PaymentMethodItem(
                method = method,
                isSelected = selectedMethod == method,
                onClick = { 
                    if (method == PaymentMethod.MOMO) {
                        onMethodSelected(method)
                    }
                }
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
    val isSupported = method == PaymentMethod.MOMO // Chỉ MoMo được hỗ trợ
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isSupported, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected && isSupported) 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f) 
                else if (!isSupported)
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected && isSupported) 
                MaterialTheme.colorScheme.onSurface 
                else if (!isSupported)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = if (isSelected && isSupported) 2.dp else 1.dp,
            color = if (isSelected && isSupported) 
                MaterialTheme.colorScheme.onSurface 
                else if (!isSupported)
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
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
                        if (isSelected && isSupported) {
                            Modifier
                                .shadow(
                                    elevation = 2.dp,
                                    shape = CircleShape,
                                    spotColor = neumorphismStyle.darkShadowColor,
                                    ambientColor = neumorphismStyle.lightShadowColor
                                )
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface)
                        } else {
                            Modifier
                                .border(
                                    width = 2.dp,
                                    color = if (isSupported)
                                        MaterialTheme.colorScheme.outline
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected && isSupported) {
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
                    tint = if (isSupported) Color.Unspecified else Color.Unspecified.copy(alpha = 0.4f),
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
                    tint = if (isSelected && isSupported) 
                        MaterialTheme.colorScheme.onSurface 
                    else if (!isSupported)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Method name and status
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected && isSupported) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected && isSupported) 
                        MaterialTheme.colorScheme.onSurface 
                    else if (!isSupported)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                if (!isSupported) {
                    Text(
                        text = "Chưa hỗ trợ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
} 