package com.nicha.eventticketing.ui.screens.payment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.theme.EventTicketingTheme
import com.nicha.eventticketing.viewmodel.PaymentViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PaymentResultActivity : ComponentActivity() {
    
    private val viewModel: PaymentViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Xử lý dữ liệu từ intent
        handleIntent(intent)
        
        setContent {
            EventTicketingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PaymentResultScreen(
                        onFinish = { finish() }
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent) {
        when {
            // Xử lý kết quả từ VNPAY
            intent.data?.toString()?.startsWith("eventticketing://payment/callback") == true -> {
                val data = intent.data
                val params = mutableMapOf<String, String>()
                
                data?.let {
                    val query = it.query
                    query?.split("&")?.forEach { param ->
                        val keyValue = param.split("=")
                        if (keyValue.size == 2) {
                            params[keyValue[0]] = keyValue[1]
                        }
                    }
                }
                
                Timber.d("Nhận kết quả thanh toán từ VNPAY: $params")
                viewModel.processVnPayReturn(params)
            }
            
            // Xử lý kết quả từ MoMo
            intent.getStringExtra("status") != null -> {
                val params = mutableMapOf<String, String>()
                intent.extras?.keySet()?.forEach { key ->
                    intent.extras?.getString(key)?.let { value ->
                        params[key] = value
                    }
                }
                
                Timber.d("Nhận kết quả thanh toán từ MoMo: $params")
                viewModel.processMomoReturn(params)
            }
        }
    }
}

@Composable
fun PaymentResultScreen(
    viewModel: PaymentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onFinish: () -> Unit
) {
    val paymentResultState by viewModel.paymentResultState.collectAsState()
    
    LaunchedEffect(paymentResultState) {
        if (paymentResultState is ResourceState.Success) {
            // Tự động chuyển về màn hình chính sau 3 giây
            kotlinx.coroutines.delay(3000)
            onFinish()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (paymentResultState) {
            is ResourceState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Đang xử lý kết quả thanh toán...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            is ResourceState.Success -> {
                val payment = (paymentResultState as ResourceState.Success).data
                
                Text(
                    text = "Thanh toán thành công!",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Cảm ơn bạn đã đặt vé. Vé của bạn đã được gửi đến email.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Xem vé của tôi")
                }
            }
            is ResourceState.Error -> {
                val errorMessage = (paymentResultState as ResourceState.Error).message
                
                Text(
                    text = "Thanh toán thất bại",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = errorMessage,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quay lại")
                }
            }
            else -> {
                Text(
                    text = "Đang xử lý kết quả thanh toán...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
} 