package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentRequestDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentResponseDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentMethodDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentStatusUpdateDto
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.domain.repository.EventRepository
import com.nicha.eventticketing.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * ViewModel để quản lý dữ liệu thanh toán
 */
@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val eventRepository: EventRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    // State cho thông tin sự kiện
    private val _eventState = MutableStateFlow<ResourceState<EventDto>>(ResourceState.Initial)
    val eventState: StateFlow<ResourceState<EventDto>> = _eventState.asStateFlow()
    
    // State cho danh sách phương thức thanh toán
    private val _paymentMethodsState = MutableStateFlow<ResourceState<List<PaymentMethodDto>>>(ResourceState.Initial)
    val paymentMethodsState: StateFlow<ResourceState<List<PaymentMethodDto>>> = _paymentMethodsState.asStateFlow()
    
    // State cho quá trình thanh toán
    private val _paymentState = MutableStateFlow<ResourceState<PaymentResponseDto>>(ResourceState.Initial)
    val paymentState: StateFlow<ResourceState<PaymentResponseDto>> = _paymentState.asStateFlow()
    
    // State cho kết quả thanh toán
    private val _paymentResultState = MutableStateFlow<ResourceState<PaymentResponseDto>>(ResourceState.Initial)
    val paymentResultState: StateFlow<ResourceState<PaymentResponseDto>> = _paymentResultState.asStateFlow()
    
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
                            Timber.d("Lấy thông tin sự kiện thành công: ${event.title}")
                        } ?: run {
                            _eventState.value = ResourceState.Error("Không tìm thấy sự kiện")
                        }
                    }
                    is Resource.Error -> {
                        _eventState.value = ResourceState.Error(result.message ?: "Không thể lấy thông tin sự kiện")
                        Timber.e("Lấy thông tin sự kiện thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _eventState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Lấy danh sách phương thức thanh toán
     */
    fun getPaymentMethods() {
        _paymentMethodsState.value = ResourceState.Loading
        
        viewModelScope.launch {
            paymentRepository.getPaymentMethods().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { methods ->
                            _paymentMethodsState.value = ResourceState.Success(methods)
                            Timber.d("Lấy danh sách phương thức thanh toán thành công: ${methods.size} phương thức")
                        } ?: run {
                            _paymentMethodsState.value = ResourceState.Error("Không tìm thấy phương thức thanh toán")
                        }
                    }
                    is Resource.Error -> {
                        _paymentMethodsState.value = ResourceState.Error(result.message ?: "Không thể lấy danh sách phương thức thanh toán")
                        Timber.e("Lấy danh sách phương thức thanh toán thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _paymentMethodsState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Tạo thanh toán mới
     */
    fun createPayment(
        ticketId: String,
        amount: Double,
        paymentMethod: String,
        returnUrl: String,
        description: String? = null
    ) {
        _paymentState.value = ResourceState.Loading
        
        viewModelScope.launch {
            Timber.d("Đang tạo thanh toán với phương thức: $paymentMethod")
            
            val paymentRequest = PaymentRequestDto(
                ticketId = ticketId,
                amount = amount,
                paymentMethod = paymentMethod,
                returnUrl = returnUrl,
                description = description,
                metadata = mapOf("platform" to "android", "app_version" to "1.0.0")
            )
            
            paymentRepository.createPayment(paymentRequest).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { payment ->
                            _paymentState.value = ResourceState.Success(payment)
                            Timber.d("Tạo thanh toán thành công: ${payment.id}")
                        } ?: run {
                            _paymentState.value = ResourceState.Error("Không tạo được thanh toán")
                        }
                    }
                    is Resource.Error -> {
                        _paymentState.value = ResourceState.Error(result.message ?: "Không thể tạo thanh toán")
                        Timber.e("Tạo thanh toán thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _paymentState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Xử lý kết quả thanh toán từ VNPay
     */
    fun processVnPayReturn(params: Map<String, String>) {
        _paymentResultState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang xử lý kết quả thanh toán từ VNPay")
                // Trong thực tế, cần gửi params này lên server để xác thực
                // Ở đây giả định thanh toán thành công nếu có vnp_ResponseCode=00
                
                if (params["vnp_ResponseCode"] == "00") {
                    val paymentId = params["vnp_TxnRef"] ?: ""
                    
                    paymentRepository.getPaymentById(paymentId).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                result.data?.let { payment ->
                                    _paymentResultState.value = ResourceState.Success(PaymentResponseDto(
                                        id = payment.id,
                                        userId = payment.userId,
                                        userName = "Current User", // Giả định
                                        ticketId = payment.orderId,
                                        eventId = "event-id", // Giả định
                                        eventTitle = "Event Title", // Giả định
                                        ticketTypeName = "Ticket Type", // Giả định
                                        amount = payment.amount,
                                        paymentMethod = payment.paymentMethod,
                                        transactionId = payment.transactionId,
                                        status = payment.status,
                                        paymentUrl = null,
                                        createdAt = payment.createdAt,
                                        updatedAt = payment.updatedAt
                                    ))
                                    Timber.d("Xử lý kết quả thanh toán thành công")
                                } ?: run {
                                    _paymentResultState.value = ResourceState.Error("Không tìm thấy thông tin thanh toán")
                                }
                            }
                            is Resource.Error -> {
                                _paymentResultState.value = ResourceState.Error(result.message ?: "Không thể xử lý kết quả thanh toán")
                                Timber.e("Xử lý kết quả thanh toán thất bại: ${result.message}")
                            }
                            is Resource.Loading -> {
                                _paymentResultState.value = ResourceState.Loading
                            }
                        }
                    }
                } else {
                    val errorCode = params["vnp_ResponseCode"] ?: "unknown"
                    Timber.e("Thanh toán thất bại với mã lỗi: $errorCode")
                    _paymentResultState.value = ResourceState.Error("Thanh toán thất bại với mã lỗi: $errorCode")
                }
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi xử lý kết quả thanh toán VNPay")
                _paymentResultState.value = ResourceState.Error(e.message ?: "Đã xảy ra lỗi không xác định")
            }
        }
    }
    
    /**
     * Xử lý kết quả thanh toán từ MoMo
     */
    fun processMomoReturn(params: Map<String, String>) {
        _paymentResultState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang xử lý kết quả thanh toán từ MoMo")
                // Trong thực tế, cần gửi params này lên server để xác thực
                // Ở đây giả định thanh toán thành công nếu có status=0
                
                if (params["status"] == "0") {
                    val paymentId = params["orderId"] ?: ""
                    
                    paymentRepository.getPaymentById(paymentId).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                result.data?.let { payment ->
                                    _paymentResultState.value = ResourceState.Success(PaymentResponseDto(
                                        id = payment.id,
                                        userId = payment.userId,
                                        userName = "Current User", // Giả định
                                        ticketId = payment.orderId,
                                        eventId = "event-id", // Giả định
                                        eventTitle = "Event Title", // Giả định
                                        ticketTypeName = "Ticket Type", // Giả định
                                        amount = payment.amount,
                                        paymentMethod = payment.paymentMethod,
                                        transactionId = payment.transactionId,
                                        status = payment.status,
                                        paymentUrl = null,
                                        createdAt = payment.createdAt,
                                        updatedAt = payment.updatedAt
                                    ))
                                    Timber.d("Xử lý kết quả thanh toán thành công")
                                } ?: run {
                                    _paymentResultState.value = ResourceState.Error("Không tìm thấy thông tin thanh toán")
                                }
                            }
                            is Resource.Error -> {
                                _paymentResultState.value = ResourceState.Error(result.message ?: "Không thể xử lý kết quả thanh toán")
                                Timber.e("Xử lý kết quả thanh toán thất bại: ${result.message}")
                            }
                            is Resource.Loading -> {
                                _paymentResultState.value = ResourceState.Loading
                            }
                        }
                    }
                } else {
                    val errorCode = params["status"] ?: "unknown"
                    Timber.e("Thanh toán thất bại với mã lỗi: $errorCode")
                    _paymentResultState.value = ResourceState.Error("Thanh toán thất bại với mã lỗi: $errorCode")
                }
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi xử lý kết quả thanh toán MoMo")
                _paymentResultState.value = ResourceState.Error(e.message ?: "Đã xảy ra lỗi không xác định")
            }
        }
    }
    
    /**
     * Reset trạng thái lỗi
     */
    fun resetEventState() {
        if (_eventState.value is ResourceState.Error) {
            _eventState.value = ResourceState.Initial
        }
    }
    
    fun resetPaymentMethodsState() {
        if (_paymentMethodsState.value is ResourceState.Error) {
            _paymentMethodsState.value = ResourceState.Initial
        }
    }
    
    fun resetPaymentState() {
        if (_paymentState.value is ResourceState.Error) {
            _paymentState.value = ResourceState.Initial
        }
    }
    
    fun resetPaymentResultState() {
        if (_paymentResultState.value is ResourceState.Error) {
            _paymentResultState.value = ResourceState.Initial
        }
    }
} 