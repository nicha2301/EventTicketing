package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentRequestDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentResponseDto
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.domain.repository.EventRepository
import com.nicha.eventticketing.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * ViewModel để quản lý dữ liệu thanh toán
 */
@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val paymentRepository: PaymentRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    // State cho thông tin sự kiện
    private val _eventState = MutableStateFlow<ResourceState<EventDto>>(ResourceState.Initial)
    val eventState: StateFlow<ResourceState<EventDto>> = _eventState.asStateFlow()
    
    // State cho payment response
    private val _paymentState = MutableStateFlow<ResourceState<PaymentResponseDto>>(ResourceState.Initial)
    val paymentState: StateFlow<ResourceState<PaymentResponseDto>> = _paymentState.asStateFlow()
    
    // State cho UI loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // State cho selected payment method
    private val _selectedPaymentMethod = MutableStateFlow<String?>("momo")
    val selectedPaymentMethod: StateFlow<String?> = _selectedPaymentMethod.asStateFlow()
    
    // State cho amount (UI only)
    private val _paymentAmount = MutableStateFlow(0.0)
    val paymentAmount: StateFlow<Double> = _paymentAmount.asStateFlow()

    /**
     * Lấy thông tin sự kiện
     */
    fun getEventById(eventId: String) {
        _eventState.value = ResourceState.Loading
        
        viewModelScope.launch {
            eventRepository.getEventById(eventId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { event ->
                            _eventState.value = ResourceState.Success(event)
                        } ?: run {
                            _eventState.value = ResourceState.Error("Không tìm thấy sự kiện")
                        }
                    }
                    is Resource.Error -> {
                        _eventState.value = ResourceState.Error(result.message ?: "Lỗi khi lấy thông tin sự kiện")
                    }
                    is Resource.Loading -> {
                        _eventState.value = ResourceState.Loading
                    }
                }
            }
        }
    }

    /**
     * Tạo thanh toán
     */
    fun createPayment(
        ticketId: String,
        amount: Double,
        description: String? = null
    ) {
        _paymentState.value = ResourceState.Loading
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val selectedMethod = _selectedPaymentMethod.value ?: "momo"
                
                val paymentRequest = PaymentRequestDto(
                    ticketId = ticketId,
                    amount = amount,
                    paymentMethod = selectedMethod,
                    description = description,
                    returnUrl = "eventticketing://payment/callback",
                    metadata = null
                )
                
                
                paymentRepository.createPayment(paymentRequest).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { paymentResponse ->
                                _paymentState.value = ResourceState.Success(paymentResponse)
                                _isLoading.value = false
                            } ?: run {
                                _paymentState.value = ResourceState.Error("Không nhận được phản hồi từ server")
                                _isLoading.value = false
                            }
                        }
                        is Resource.Error -> {
                            _paymentState.value = ResourceState.Error(result.message ?: "Lỗi khi tạo thanh toán")
                            _isLoading.value = false
                        }
                        is Resource.Loading -> {
                            _paymentState.value = ResourceState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                _paymentState.value = ResourceState.Error("Lỗi không mong muốn: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán theo ID
     */
    fun getPaymentById(paymentId: String) {
        viewModelScope.launch {
            try {
                
                paymentRepository.getPaymentById(paymentId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { paymentResponse ->
                                
                                when (paymentResponse.status.uppercase()) {
                                    "COMPLETED" -> {
                                        _paymentState.value = ResourceState.Success(paymentResponse)
                                    }
                                    "FAILED", "CANCELLED" -> {
                                        _paymentState.value = ResourceState.Error("Thanh toán thất bại hoặc bị hủy")
                                    }
                                    else -> {
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                        }
                        is Resource.Loading -> {
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
    
    /**
     * Kiểm tra trạng thái thanh toán sau khi return từ MoMo với timeout
     */
    fun checkPaymentStatusAfterReturn(paymentId: String, maxRetries: Int = 5) {
        viewModelScope.launch {
            try {
                
                var retryCount = 0
                var isCompleted = false
                
                while (retryCount < maxRetries && !isCompleted) {
                    
                    try {
                        // Dùng getUserPayments để tìm payment
                        paymentRepository.getUserPayments(0, 100).collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    result.data?.let { paymentsList ->
                                        val payment = paymentsList.find { it.id == paymentId }
                                        
                                        if (payment != null) {
                                            
                                            when (payment.status.uppercase()) {
                                                "COMPLETED" -> {
                                                    _paymentState.value = ResourceState.Success(payment)
                                                    isCompleted = true
                                                }
                                                "FAILED", "CANCELLED" -> {
                                                    _paymentState.value = ResourceState.Error("Thanh toán thất bại hoặc bị hủy")
                                                    isCompleted = true
                                                }
                                                "PENDING" -> {
                                                }
                                                else -> {
                                                }
                                            }
                                        } else {
                                            paymentsList.take(3).forEach { p ->
                                            }
                                            
                                            if (retryCount == maxRetries - 1) {
                                                _paymentState.value = ResourceState.Error("Không tìm thấy thông tin thanh toán")
                                                isCompleted = true
                                            }
                                        }
                                    } ?: run {
                                        if (retryCount == maxRetries - 1) {
                                            _paymentState.value = ResourceState.Error("Không nhận được dữ liệu từ server")
                                            isCompleted = true
                                        }
                                    }
                                }
                                is Resource.Error -> {
                                    if (retryCount == maxRetries - 1) {
                                        _paymentState.value = ResourceState.Error("Lỗi khi kiểm tra trạng thái thanh toán: ${result.message}")
                                        isCompleted = true
                                    }
                                }
                                is Resource.Loading -> {
                                }
                            }
                        }
                    } catch (e: Exception) {
                        if (retryCount == maxRetries - 1) {
                            _paymentState.value = ResourceState.Error("Có lỗi xảy ra khi kiểm tra thanh toán")
                            isCompleted = true
                        }
                    }
                    
                    if (!isCompleted) {
                        retryCount++
                        if (retryCount < maxRetries) {
                            val delayTime = 2000L + (retryCount * 1000L) // Increasing delay: 2s, 3s, 4s...
                            delay(delayTime)
                        }
                    }
                }
                
                if (!isCompleted) {
                    _paymentState.value = ResourceState.Error("Thanh toán mất quá nhiều thời gian. Vui lòng kiểm tra lại trong ví vé hoặc thử thanh toán lại.")
                }
                
            } catch (e: Exception) {
                _paymentState.value = ResourceState.Error("Có lỗi nghiêm trọng khi kiểm tra thanh toán")
            }
        }
    }

    /**
     * Set phương thức thanh toán
     */
    fun setSelectedPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    /**
     * Set số tiền thanh toán
     */
    fun setPaymentAmount(amount: Double) {
        _paymentAmount.value = amount
    }
    
    /**
     * Reset states
     */
    fun resetEventState() {
        if (_eventState.value is ResourceState.Error) {
            _eventState.value = ResourceState.Initial
        }
    }
    
    fun resetPaymentState() {
        _paymentState.value = ResourceState.Initial
    }

    fun resetPaymentData() {
        _selectedPaymentMethod.value = "momo"
        _paymentAmount.value = 0.0
        _isLoading.value = false
        _paymentState.value = ResourceState.Initial
    }
    
    /**  
     * Clear payment error state để có thể thử lại
     */
    fun clearPaymentError() {
        if (_paymentState.value is ResourceState.Error) {
            _paymentState.value = ResourceState.Initial
        }
    }
}