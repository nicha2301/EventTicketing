package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentRequestDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentResponseDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentMethodDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseResponseDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import retrofit2.HttpException
import kotlinx.coroutines.delay

/**
 * ViewModel để quản lý dữ liệu thanh toán
 */
@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val apiService: ApiService,
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
            try {
                Timber.d("Đang lấy thông tin sự kiện: $eventId")
                val response = apiService.getEventById(eventId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val event = response.body()?.data
                    if (event != null) {
                        _eventState.value = ResourceState.Success(event)
                        Timber.d("Lấy thông tin sự kiện thành công: ${event.title}")
                    } else {
                        Timber.e("Không thể lấy thông tin sự kiện từ response")
                        _eventState.value = ResourceState.Error("Không thể lấy thông tin sự kiện")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy thông tin sự kiện"
                    Timber.e("Lấy thông tin sự kiện thất bại: $errorMessage")
                    _eventState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy thông tin sự kiện", _eventState)
            }
        }
    }
    
    /**
     * Lấy danh sách phương thức thanh toán
     */
    fun getPaymentMethods() {
        _paymentMethodsState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy danh sách phương thức thanh toán")
                val response = apiService.getPaymentMethods()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val methods = response.body()?.data
                    if (methods != null) {
                        _paymentMethodsState.value = ResourceState.Success(methods)
                        Timber.d("Lấy danh sách phương thức thanh toán thành công: ${methods.size} phương thức")
                    } else {
                        Timber.e("Không thể lấy danh sách phương thức thanh toán từ response")
                        _paymentMethodsState.value = ResourceState.Error("Không thể lấy danh sách phương thức thanh toán")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy danh sách phương thức thanh toán"
                    Timber.e("Lấy danh sách phương thức thanh toán thất bại: $errorMessage")
                    _paymentMethodsState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy danh sách phương thức thanh toán", _paymentMethodsState)
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
            try {
                Timber.d("Đang tạo thanh toán với phương thức: $paymentMethod")
                
                val paymentRequest = PaymentRequestDto(
                    ticketId = ticketId,
                    amount = amount,
                    paymentMethod = paymentMethod,
                    returnUrl = returnUrl,
                    description = description,
                    metadata = mapOf("platform" to "android", "app_version" to "1.0.0")
                )
                
                val response = apiService.createPayment(paymentRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val paymentResponse = response.body()?.data
                    if (paymentResponse != null) {
                        _paymentState.value = ResourceState.Success(paymentResponse)
                        Timber.d("Tạo thanh toán thành công: ${paymentResponse.id}")
                    } else {
                        Timber.e("Không thể lấy thông tin thanh toán từ response")
                        _paymentState.value = ResourceState.Error("Không thể hoàn tất việc tạo thanh toán")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể tạo thanh toán"
                    Timber.e("Tạo thanh toán thất bại: $errorMessage")
                    _paymentState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "tạo thanh toán", _paymentState)
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
                    val response = apiService.getPaymentById(paymentId)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val payment = response.body()?.data
                        if (payment != null) {
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
                        } else {
                            Timber.e("Không thể lấy thông tin thanh toán từ response")
                            _paymentResultState.value = ResourceState.Error("Không thể hoàn tất việc xử lý thanh toán")
                        }
                    } else {
                        val errorMessage = response.body()?.message ?: "Không thể xử lý kết quả thanh toán"
                        Timber.e("Xử lý kết quả thanh toán thất bại: $errorMessage")
                        _paymentResultState.value = ResourceState.Error(errorMessage)
                    }
                } else {
                    val errorCode = params["vnp_ResponseCode"] ?: "unknown"
                    Timber.e("Thanh toán thất bại với mã lỗi: $errorCode")
                    _paymentResultState.value = ResourceState.Error("Thanh toán thất bại với mã lỗi: $errorCode")
                }
            } catch (e: Exception) {
                handleNetworkError(e, "xử lý kết quả thanh toán", _paymentResultState)
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
                    val response = apiService.getPaymentById(paymentId)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val payment = response.body()?.data
                        if (payment != null) {
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
                        } else {
                            Timber.e("Không thể lấy thông tin thanh toán từ response")
                            _paymentResultState.value = ResourceState.Error("Không thể hoàn tất việc xử lý thanh toán")
                        }
                    } else {
                        val errorMessage = response.body()?.message ?: "Không thể xử lý kết quả thanh toán"
                        Timber.e("Xử lý kết quả thanh toán thất bại: $errorMessage")
                        _paymentResultState.value = ResourceState.Error(errorMessage)
                    }
                } else {
                    val errorCode = params["status"] ?: "unknown"
                    Timber.e("Thanh toán thất bại với mã lỗi: $errorCode")
                    _paymentResultState.value = ResourceState.Error("Thanh toán thất bại với mã lỗi: $errorCode")
                }
            } catch (e: Exception) {
                handleNetworkError(e, "xử lý kết quả thanh toán", _paymentResultState)
            }
        }
    }
    
    /**
     * Xử lý lỗi mạng chung cho tất cả các API call
     */
    private fun <T> handleNetworkError(exception: Exception, action: String, stateFlow: MutableStateFlow<ResourceState<T>>) {
        when (exception) {
            is UnknownHostException -> {
                Timber.e(exception, "Lỗi kết nối: Không thể kết nối đến máy chủ")
                stateFlow.value = ResourceState.Error("Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng của bạn.")
            }
            is SocketTimeoutException -> {
                Timber.e(exception, "Lỗi kết nối: Kết nối bị timeout")
                stateFlow.value = ResourceState.Error("Kết nối bị timeout. Vui lòng thử lại sau.")
            }
            is IOException -> {
                // Xử lý riêng lỗi "closed"
                if (exception.message?.contains("closed", ignoreCase = true) == true) {
                    Timber.e(exception, "Lỗi kết nối: Kết nối đã đóng")
                    // Thử lại yêu cầu nếu là lỗi closed
                    if (stateFlow == _paymentState) {
                        // Đặt trạng thái về Initial để tránh hiển thị lỗi
                        stateFlow.value = ResourceState.Initial
                        // Đợi 1 giây trước khi thử lại
                        viewModelScope.launch {
                            delay(1000)
                            // Thông báo người dùng
                            stateFlow.value = ResourceState.Loading
                            stateFlow.value = ResourceState.Error("Đang xử lý thanh toán, vui lòng đợi trong giây lát...")
                        }
                    } else {
                        stateFlow.value = ResourceState.Error("Lỗi kết nối: Kết nối đã đóng. Vui lòng thử lại.")
                    }
                } else {
                    Timber.e(exception, "Lỗi kết nối: IOException")
                    stateFlow.value = ResourceState.Error("Lỗi kết nối: ${exception.message ?: "Không xác định"}")
                }
            }
            is HttpException -> {
                Timber.e(exception, "Lỗi HTTP: ${exception.code()}")
                stateFlow.value = ResourceState.Error("Lỗi máy chủ: ${exception.message()}")
            }
            else -> {
                Timber.e(exception, "Lỗi không xác định khi $action")
                stateFlow.value = ResourceState.Error("Lỗi không xác định: ${exception.message ?: "Unknown"}")
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