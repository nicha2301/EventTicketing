package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.ticket.CheckInRequestDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.domain.repository.TicketRepository
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
    private val ticketRepository: TicketRepository
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
            val request = CheckInRequestDto(
                ticketId = ticketId,
                eventId = eventId,
                userId = userId
            )
            Timber.d("Đang gọi API check-in: $request")
            
            ticketRepository.checkInTicket(request).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val ticket = result.data
                        if (ticket != null) {
                            Timber.d("Check-in thành công: ${ticket.ticketNumber}")
                            _checkInState.value = ResourceState.Success(ticket)
                            _scanningState.value = ScanningState.Success(ticket, "Check-in thành công")
                        } else {
                            val errorMsg = "Không thể lấy thông tin vé đã check-in từ response"
                            Timber.e(errorMsg)
                            _checkInState.value = ResourceState.Error(errorMsg)
                            _scanningState.value = ScanningState.Error(errorMsg)
                        }
                    }
                    is Resource.Error -> {
                        val errorMessage = result.message ?: "Không thể check-in vé"
                        Timber.e("Check-in vé thất bại: $errorMessage")
                        _checkInState.value = ResourceState.Error(errorMessage)
                        _scanningState.value = ScanningState.Error(errorMessage)
                    }
                    is Resource.Loading -> {
                        _checkInState.value = ResourceState.Loading
                        _scanningState.value = ScanningState.Processing
                    }
                }
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