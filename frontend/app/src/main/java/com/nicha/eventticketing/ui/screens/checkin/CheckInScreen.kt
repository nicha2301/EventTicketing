package com.nicha.eventticketing.ui.screens.checkin

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.ui.components.app.AppButton
import com.nicha.eventticketing.ui.components.neumorphic.NeumorphicCard
import com.nicha.eventticketing.util.FormatUtils
import com.nicha.eventticketing.viewmodel.CheckInViewModel
import com.nicha.eventticketing.viewmodel.ScanningState
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CheckInScreen(
    onBackClick: () -> Unit,
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State for QR scanner
    var isScannerInitialized by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(true) }

    // Observe view model states
    val scanningState by viewModel.scanningState.collectAsState()

    // Camera permission state
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    // Check if camera is available
    val hasCameraHardware = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    // Scanner view reference
    val scannerViewRef = remember { mutableStateOf<DecoratedBarcodeView?>(null) }

    // Handle scanning result
    val barcodeCallback = remember {
        object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.let {
                    if (isScanning) {
                        isScanning = false
                        Timber.d("Barcode detected: ${it.text}")
                        viewModel.processQrCode(it.text)
                    }
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
        }
    }

    // Initialize scanner when component is first displayed
    LaunchedEffect(Unit) {
        if (!isScannerInitialized) {
            scannerViewRef.value?.let {
                try {
                    val formats = listOf(BarcodeFormat.QR_CODE)
                    it.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
                    it.resume()
                    isScannerInitialized = true
                    Timber.d("Scanner initialized successfully")
                } catch (e: Exception) {
                    Timber.e(e, "Error initializing scanner")
                }
            }
        }
    }

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    scannerViewRef.value?.resume()
                    isScanning = true
                    Timber.d("Scanner resumed")
                }

                Lifecycle.Event.ON_PAUSE -> {
                    scannerViewRef.value?.pause()
                    Timber.d("Scanner paused")
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            scannerViewRef.value?.pause()
            Timber.d("Scanner disposed")
        }
    }

    // Reset scanning state when needed
    LaunchedEffect(scanningState) {
        when (scanningState) {
            is ScanningState.Success, is ScanningState.Error -> {
                scannerViewRef.value?.pause()
                Timber.d("Scanner paused due to result")
            }

            is ScanningState.Ready -> {
                scannerViewRef.value?.resume()
                isScanning = true
                Timber.d("Scanner resumed and ready")
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CheckInTopAppBar(onBackClick)
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                !hasCameraHardware -> NoHardwareErrorMessage()
                !cameraPermissionState.status.isGranted -> CameraPermissionRequest(
                    cameraPermissionState
                )

                else -> QrScannerView(
                    isScanning = isScanning,
                    scanningState = scanningState,
                    barcodeCallback = barcodeCallback,
                    scannerViewRef = scannerViewRef
                )
            }
        }
    }

    // Show scan result dialog
    when (val state = scanningState) {
        is ScanningState.Success -> {
            ModernCheckInResultDialog(
                ticket = state.ticket,
                message = state.message,
                success = true,
                onDismiss = {
                    viewModel.resetState()
                }
            )
        }

        is ScanningState.Error -> {
            ModernCheckInResultDialog(
                errorMessage = state.message,
                success = false,
                onDismiss = {
                    viewModel.resetState()
                    isScanning = true
                    scannerViewRef.value?.resume()
                }
            )
        }

        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckInTopAppBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text("Quét vé") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black,
            titleContentColor = Color.White
        )
    )
}

@Composable
private fun QrScannerView(
    isScanning: Boolean,
    scanningState: ScanningState,
    barcodeCallback: BarcodeCallback,
    scannerViewRef: MutableState<DecoratedBarcodeView?>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        QrCameraPreview(
            isScanning = isScanning,
            barcodeCallback = barcodeCallback,
            scannerViewRef = scannerViewRef
        )

        // Scanner Overlay
        ScannerOverlay(isScanning = isScanning)

        // Instruction Text
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Đặt mã QR vào khung để quét",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        // Processing Indicator
        if (scanningState is ScanningState.Processing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onSurface,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@Composable
private fun QrCameraPreview(
    isScanning: Boolean,
    barcodeCallback: BarcodeCallback,
    scannerViewRef: MutableState<DecoratedBarcodeView?>
) {
    AndroidView(
        factory = { ctx ->
            try {
                DecoratedBarcodeView(ctx).apply {
                    val formats = listOf(BarcodeFormat.QR_CODE)
                    barcodeView.decoderFactory = DefaultDecoderFactory(formats)
                    this.setStatusText("")

                    val intent = Intent().apply {
                        putExtra("SCAN_FORMATS", "QR_CODE")
                        putExtra("SCAN_MODE", "QR_CODE_MODE")
                        putExtra("SCAN_CAMERA_ID", 0)
                        putExtra("SCAN_ORIENTATION_LOCKED", false)
                        putExtra("SCAN_BEEP_ENABLED", true)
                    }

                    this.initializeFromIntent(intent)
                    this.decodeContinuous(barcodeCallback)

                    barcodeView.cameraSettings.apply {
                        isContinuousFocusEnabled = true
                        isAutoFocusEnabled = true
                        isExposureEnabled = true
                    }

                    scannerViewRef.value = this
                    Timber.d("DecoratedBarcodeView created successfully")
                    this
                }
            } catch (e: Exception) {
                Timber.e(e, "Error creating DecoratedBarcodeView")
                android.widget.TextView(ctx).apply {
                    text = "Không thể khởi tạo camera: ${e.message}"
                    setTextColor(android.graphics.Color.WHITE)
                    textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            try {
                if (view is DecoratedBarcodeView) {
                    if (isScanning) {
                        view.resume()
                    } else {
                        view.pause()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error updating scanner view")
            }
        }
    )
}

@Composable
private fun ScannerOverlay(isScanning: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            ScannerAnimationLine(isScanning = isScanning)
        }
    }
}

@Composable
private fun BoxScope.ScannerAnimationLine(isScanning: Boolean) {
    val scannerLinePosition = remember { Animatable(0f) }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            while (true) {
                scannerLinePosition.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1500)
                )
                scannerLinePosition.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(1500)
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .offset(y = (260 * scannerLinePosition.value).dp)
            .background(MaterialTheme.colorScheme.onSurface)
    )
}

@Composable
fun NoHardwareErrorMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.BrokenImage,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Thiết bị của bạn không hỗ trợ camera",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bạn cần thiết bị có camera để sử dụng tính năng này",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionRequest(cameraPermissionState: PermissionState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.Black.copy(alpha = 0.7f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cần quyền truy cập camera để quét mã QR",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppButton(
            onClick = { cameraPermissionState.launchPermissionRequest() }
        ) {
            Text("Cấp quyền camera")
        }
    }
}

@Composable
fun ModernCheckInResultDialog(
    ticket: TicketDto? = null,
    message: String = "Check-in thành công",
    errorMessage: String? = null,
    success: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Icon
                StatusIcon(success = success)

                Spacer(modifier = Modifier.height(16.dp))

                // Status Text
                Text(
                    text = if (success) message else "Check-in thất bại",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (success) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Content
                if (success && ticket != null) {
                    SuccessContent(ticket = ticket)
                } else {
                    ErrorContent(errorMessage = errorMessage)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Continue Button
                AppButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Tiếp tục quét",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusIcon(success: Boolean) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn()
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    if (success) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.error,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = if (success) "Thành công" else "Thất bại",
                tint = Color.White,
                modifier = Modifier.size(45.dp)
            )
        }
    }
}

@Composable
private fun SuccessContent(ticket: TicketDto) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically()
    ) {
        Column {
            // Event Info Card
            EventInfoCard(ticket = ticket)

            Spacer(modifier = Modifier.height(16.dp))

            // Ticket Details Card
            TicketDetailsCard(ticket = ticket)
        }
    }
}

@Composable
private fun EventInfoCard(ticket: TicketDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ticket.eventImageUrl,
                contentDescription = ticket.eventTitle,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = ticket.eventTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = FormatUtils.formatDate(ticket.eventStartDate, "dd/MM/yyyy HH:mm"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = ticket.eventLocation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TicketDetailsCard(ticket: TicketDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Ticket Type and Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Loại vé",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = ticket.ticketTypeName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = ticket.ticketNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(12.dp))

            // User Info
            InfoRow(
                icon = Icons.Default.Person,
                text = ticket.userName
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Check-in Time
            InfoRow(
                icon = Icons.Default.AccessTime,
                text = "Check-in: ${
                    FormatUtils.formatDate(
                        ticket.checkedInAt,
                        "HH:mm:ss dd/MM/yyyy"
                    )
                }"
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ErrorContent(errorMessage: String?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = errorMessage ?: "Vé không hợp lệ hoặc đã được sử dụng",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
} 