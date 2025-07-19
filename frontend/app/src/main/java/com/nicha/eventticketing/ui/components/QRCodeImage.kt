package com.nicha.eventticketing.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nicha.eventticketing.util.QRCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Composable để hiển thị mã QR từ dữ liệu vé
 */
@Composable
fun QRCodeImage(
    ticketId: String,
    ticketNumber: String,
    eventId: String,
    userId: String,
    qrCodeUrl: String? = null,
    modifier: Modifier = Modifier,
    size: Int = 512
) {
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    LaunchedEffect(ticketId, ticketNumber, eventId, userId) {
        isLoading = true
        isError = false

        withContext(Dispatchers.IO) {
            runCatching {
                QRCodeGenerator.generateTicketQRCode(ticketId, ticketNumber, eventId, userId)
            }.onSuccess { bitmap ->
                qrBitmap = bitmap
                isError = bitmap == null
            }.onFailure {
                isError = true
            }.also {
                isLoading = false
            }
        }
    }
    
    Box(
        modifier = modifier.padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (isError || qrBitmap == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Không thể tạo mã QR",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Image(
                bitmap = qrBitmap!!.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .size((size / 2).dp)
                    .background(Color.White)
                    .padding(8.dp)
            )
        }
    }
} 