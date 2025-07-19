package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.ticket.CheckInRequestDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
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
import retrofit2.Response

private typealias TicketResponse = Response<ApiResponse<TicketDto>>

/**
 * ViewModel để quản lý việc check-in vé thông qua mã QR
 */
@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    
    // State cho check-in vé
    private val _checkInState = MutableStateFlow<ResourceState<TicketDto>>(ResourceState.Initial)
    val checkInState: StateFlow<ResourceState<TicketDto>> = _checkInState.asStateFlow()
    
    // State cho việc quét QR
    private val _scanningState = MutableStateFlow<ScanningState>(ScanningState.Ready)
    val scanningState: StateFlow<ScanningState> = _scanningState.asStateFlow()
    
    /**
     * Check-in vé bằng API mới
     */
    fun checkInTicket(ticketId: String, eventId: String, userId: String) {
        prepareCheckIn()
        Timber.d("Bắt đầu check-in vé: ticketId=$ticketId, eventId=$eventId, userId=$userId")
        
        viewModelScope.launch {
            try {
                val request = CheckInRequestDto(
                    ticketId = ticketId,
                    eventId = eventId,
                    userId = userId
                )
                Timber.d("Đang gọi API check-in: $request")
                val response = apiService.checkInTicket(request)
                handleCheckInResponse(response)
            } catch (e: Exception) {
                handleCheckInError(e)
            }
        }
    }
    
    /**
     * Chuẩn bị trạng thái cho việc check-in
     */
    private fun prepareCheckIn() {
        _checkInState.value = ResourceState.Loading
        _scanningState.value = ScanningState.Processing
    }
    
    /**
     * Xử lý response từ API check-in
     */
    private fun handleCheckInResponse(response: TicketResponse) {
        if (response.isSuccessful && response.body()?.success == true) {
            val ticket = response.body()?.data
            if (ticket != null) {
                val message = response.body()?.message ?: "Check-in thành công"
                Timber.d("Check-in thành công: ${ticket.ticketNumber}, message: $message")
                _checkInState.value = ResourceState.Success(ticket)
                _scanningState.value = ScanningState.Success(ticket, message)
            } else {
                val errorMsg = "Không thể lấy thông tin vé đã check-in từ response"
                Timber.e(errorMsg)
                _checkInState.value = ResourceState.Error(errorMsg)
                _scanningState.value = ScanningState.Error(errorMsg)
            }
        } else {
            val errorMessage = response.body()?.message ?: "Không thể check-in vé"
            Timber.e("Check-in vé thất bại: $errorMessage")
            _checkInState.value = ResourceState.Error(errorMessage)
            _scanningState.value = ScanningState.Error(errorMessage)
        }
    }
    
    /**
     * Xử lý lỗi khi check-in
     */
    private fun handleCheckInError(e: Exception) {
        val errorMsg = handleNetworkError(e, "check-in vé")
        _checkInState.value = ResourceState.Error(errorMsg)
        _scanningState.value = ScanningState.Error(errorMsg)
    }
    
    /**
     * Xử lý mã QR được quét
     * Định dạng mã QR: TICKET:$ticketId:$ticketNumber:$eventId:$userId
     */
    fun processQrCode(qrContent: String) {
        Timber.d("Xử lý mã QR: $qrContent")
        
        if (qrContent.isBlank()) {
            val errorMsg = "Mã QR trống hoặc không hợp lệ"
            Timber.e(errorMsg)
            _scanningState.value = ScanningState.Error(errorMsg)
            return
        }
        
        // Kiểm tra định dạng mã QR mới
        if (qrContent.startsWith("TICKET:")) {
            try {
                val parts = qrContent.split(":")
                if (parts.size >= 5) {
                    val ticketId = parts[1]
                    val eventId = parts[3]
                    val userId = parts[4]
                    Timber.d("Phát hiện mã QR: ticketId=$ticketId, eventId=$eventId, userId=$userId")
                    checkInTicket(ticketId, eventId, userId)
                } else {
                    val errorMsg = "Định dạng mã QR không hợp lệ"
                    Timber.e(errorMsg)
                    _scanningState.value = ScanningState.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Lỗi khi xử lý mã QR: ${e.message}"
                Timber.e(e, errorMsg)
                _scanningState.value = ScanningState.Error(errorMsg)
            }
        } else {
            val errorMsg = "Định dạng mã QR không được hỗ trợ"
            Timber.e(errorMsg)
            _scanningState.value = ScanningState.Error(errorMsg)
        }
    }
    
    /**
     * Reset trạng thái
     */
    fun resetState() {
        Timber.d("Reset trạng thái")
        _checkInState.value = ResourceState.Initial
        _scanningState.value = ScanningState.Ready
    }
    
    /**
     * Xử lý lỗi mạng chung cho tất cả các API call
     * @return Thông báo lỗi để hiển thị
     */
    private fun handleNetworkError(exception: Exception, action: String): String {
        val errorMessage = when (exception) {
            is UnknownHostException -> {
                val msg = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng của bạn."
                Timber.e(exception, "Lỗi kết nối: Không thể kết nối đến máy chủ")
                msg
            }
            is SocketTimeoutException -> {
                val msg = "Kết nối bị timeout. Vui lòng thử lại sau."
                Timber.e(exception, "Lỗi kết nối: Kết nối bị timeout")
                msg
            }
            is IOException -> {
                val msg = "Lỗi kết nối: ${exception.message ?: "Không xác định"}"
                Timber.e(exception, "Lỗi kết nối: IOException")
                msg
            }
            is HttpException -> {
                val msg = "Lỗi máy chủ: ${exception.message()}"
                Timber.e(exception, "Lỗi HTTP: ${exception.code()}")
                msg
            }
            else -> {
                val msg = "Lỗi không xác định: ${exception.message ?: "Unknown"}"
                Timber.e(exception, "Lỗi không xác định khi $action")
                msg
            }
        }
        
        return errorMessage
    }
}

/**
 * Trạng thái quét QR
 */
sealed class ScanningState {
    object Ready : ScanningState()
    object Processing : ScanningState()
    data class Success(val ticket: TicketDto, val message: String = "Check-in thành công") : ScanningState()
    data class Error(val message: String) : ScanningState()
} 