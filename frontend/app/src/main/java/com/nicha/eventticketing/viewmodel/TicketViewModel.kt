package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseDto
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto

/**
 * ViewModel để quản lý dữ liệu vé
 */
@HiltViewModel
class TicketViewModel @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    // State cho danh sách vé của người dùng
    private val _ticketsState = MutableStateFlow<ResourceState<List<TicketDto>>>(ResourceState.Initial)
    val ticketsState: StateFlow<ResourceState<List<TicketDto>>> = _ticketsState.asStateFlow()
    
    // State cho chi tiết vé
    private val _ticketDetailState = MutableStateFlow<ResourceState<TicketDto>>(ResourceState.Initial)
    val ticketDetailState: StateFlow<ResourceState<TicketDto>> = _ticketDetailState.asStateFlow()
    
    // State cho mua vé
    private val _purchaseState = MutableStateFlow<ResourceState<TicketPurchaseResponseDto>>(ResourceState.Initial)
    val purchaseState: StateFlow<ResourceState<TicketPurchaseResponseDto>> = _purchaseState.asStateFlow()
    
    // State cho check-in vé
    private val _checkInState = MutableStateFlow<ResourceState<TicketDto>>(ResourceState.Initial)
    val checkInState: StateFlow<ResourceState<TicketDto>> = _checkInState.asStateFlow()
    
    // State cho vé của user theo eventId
    private val _myTicketForEventState = MutableStateFlow<ResourceState<TicketDto?>>(ResourceState.Initial)
    val myTicketForEventState: StateFlow<ResourceState<TicketDto?>> = _myTicketForEventState.asStateFlow()

    // State cho loại vé
    private val _ticketTypeState = MutableStateFlow<ResourceState<TicketTypeDto>>(ResourceState.Initial)
    val ticketTypeState: StateFlow<ResourceState<TicketTypeDto>> = _ticketTypeState.asStateFlow()

    /**
     * Lấy danh sách vé của người dùng
     */
    fun getMyTickets(status: String? = null, page: Int = 0, size: Int = 10) {
        _ticketsState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy danh sách vé của người dùng với status: $status")
                val response = apiService.getMyTickets(status, page, size)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val tickets = response.body()?.data?.content
                    if (tickets != null) {
                        _ticketsState.value = ResourceState.Success(tickets)
                        Timber.d("Lấy danh sách vé thành công: ${tickets.size} vé")
                    } else {
                        Timber.e("Không thể lấy danh sách vé từ response")
                        _ticketsState.value = ResourceState.Error("Không thể lấy danh sách vé")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy danh sách vé"
                    Timber.e("Lấy danh sách vé thất bại: $errorMessage")
                    _ticketsState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy danh sách vé", _ticketsState)
            }
        }
    }
    
    /**
     * Lấy danh sách vé của người dùng với bộ lọc nâng cao
     */
    fun getMyTicketsWithFilter(tabId: String, page: Int = 0, size: Int = 10) {
        _ticketsState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy danh sách vé của người dùng với tab: $tabId")
                
                val status = when (tabId) {
                    "active" -> "PAID"
                    "expired" -> "EXPIRED"
                    "cancelled" -> "CANCELLED"
                    else -> null
                }
                
                val response = apiService.getMyTickets(status, page, size)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    var tickets = response.body()?.data?.content ?: emptyList()
                    
                    if (tabId == "active") {
                        tickets = tickets.filter { ticket ->
                            val eventEndDate = parseDate(ticket.eventEndDate)
                            val now = Date()
                            
                            val isNotExpired = eventEndDate?.after(now) ?: true
                            
                            ticket.status == "PAID" && isNotExpired
                        }
                    }
                    
                    _ticketsState.value = ResourceState.Success(tickets)
                    Timber.d("Lấy danh sách vé thành công: ${tickets.size} vé")
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy danh sách vé"
                    Timber.e("Lấy danh sách vé thất bại: $errorMessage")
                    _ticketsState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy danh sách vé", _ticketsState)
            }
        }
    }
    
    /**
     * Lấy chi tiết vé theo ID
     */
    fun getTicketById(ticketId: String) {
        _ticketDetailState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy chi tiết vé: $ticketId")
                val response = apiService.getTicketById(ticketId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val ticket = response.body()?.data
                    if (ticket != null) {
                        _ticketDetailState.value = ResourceState.Success(ticket)
                        Timber.d("Lấy chi tiết vé thành công: ${ticket.ticketNumber}")
                    } else {
                        Timber.e("Không thể lấy chi tiết vé từ response")
                        _ticketDetailState.value = ResourceState.Error("Không thể lấy chi tiết vé")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy chi tiết vé"
                    Timber.e("Lấy chi tiết vé thất bại: $errorMessage")
                    _ticketDetailState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy chi tiết vé", _ticketDetailState)
            }
        }
    }
    
    /**
     * Lấy chi tiết vé theo mã vé
     */
    fun getTicketByNumber(ticketNumber: String) {
        _ticketDetailState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy chi tiết vé theo mã: $ticketNumber")
                val response = apiService.getTicketByNumber(ticketNumber)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val ticket = response.body()?.data
                    if (ticket != null) {
                        _ticketDetailState.value = ResourceState.Success(ticket)
                        Timber.d("Lấy chi tiết vé thành công: ${ticket.ticketNumber}")
                    } else {
                        Timber.e("Không thể lấy chi tiết vé từ response")
                        _ticketDetailState.value = ResourceState.Error("Không thể lấy chi tiết vé")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy chi tiết vé"
                    Timber.e("Lấy chi tiết vé thất bại: $errorMessage")
                    _ticketDetailState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy chi tiết vé", _ticketDetailState)
            }
        }
    }
    
    /**
     * Mua vé
     */
    fun purchaseTickets(purchaseDto: TicketPurchaseDto) {
        _purchaseState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang mua vé cho sự kiện: ${purchaseDto.eventId}")
                val response = apiService.purchaseTickets(purchaseDto)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val purchaseResponse = response.body()?.data
                    if (purchaseResponse != null) {
                        _purchaseState.value = ResourceState.Success(purchaseResponse)
                        Timber.d("Mua vé thành công: ${purchaseResponse.paymentId}")
                    } else {
                        Timber.e("Không thể lấy thông tin mua vé từ response")
                        _purchaseState.value = ResourceState.Error("Không thể hoàn tất việc mua vé")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể mua vé"
                    Timber.e("Mua vé thất bại: $errorMessage")
                    _purchaseState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "mua vé", _purchaseState)
            }
        }
    }
    
    /**
     * Check-in vé
     */
    fun checkInTicket(ticketId: String) {
        _checkInState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang check-in vé: $ticketId")
                val response = apiService.checkInTicket(ticketId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val ticket = response.body()?.data
                    if (ticket != null) {
                        _checkInState.value = ResourceState.Success(ticket)
                        Timber.d("Check-in vé thành công: ${ticket.ticketNumber}")
                    } else {
                        Timber.e("Không thể lấy thông tin vé đã check-in từ response")
                        _checkInState.value = ResourceState.Error("Không thể hoàn tất việc check-in vé")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể check-in vé"
                    Timber.e("Check-in vé thất bại: $errorMessage")
                    _checkInState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "check-in vé", _checkInState)
            }
        }
    }
    
    /**
     * Check-in vé bằng mã vé
     */
    fun checkInTicketByNumber(ticketNumber: String) {
        _checkInState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang check-in vé theo mã: $ticketNumber")
                val response = apiService.checkInTicketByNumber(ticketNumber)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val ticket = response.body()?.data
                    if (ticket != null) {
                        _checkInState.value = ResourceState.Success(ticket)
                        Timber.d("Check-in vé thành công: ${ticket.ticketNumber}")
                    } else {
                        Timber.e("Không thể lấy thông tin vé đã check-in từ response")
                        _checkInState.value = ResourceState.Error("Không thể hoàn tất việc check-in vé")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể check-in vé"
                    Timber.e("Check-in vé thất bại: $errorMessage")
                    _checkInState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "check-in vé", _checkInState)
            }
        }
    }
    
    /**
     * Hủy vé
     */
    fun cancelTicket(ticketId: String) {
        _ticketDetailState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang hủy vé: $ticketId")
                val response = apiService.cancelTicket(ticketId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val ticket = response.body()?.data
                    if (ticket != null) {
                        _ticketDetailState.value = ResourceState.Success(ticket)
                        Timber.d("Hủy vé thành công: ${ticket.ticketNumber}")
                    } else {
                        Timber.e("Không thể lấy thông tin vé đã hủy từ response")
                        _ticketDetailState.value = ResourceState.Error("Không thể hoàn tất việc hủy vé")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể hủy vé"
                    Timber.e("Hủy vé thất bại: $errorMessage")
                    _ticketDetailState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "hủy vé", _ticketDetailState)
            }
        }
    }
    
    /**
     * Lấy vé hợp lệ của user cho một event cụ thể
     */
    fun getMyTicketsByEventId(eventId: String) {
        _myTicketForEventState.value = ResourceState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getMyTickets(null, 0, 100) // lấy nhiều vé để lọc
                if (response.isSuccessful && response.body()?.success == true) {
                    val tickets = response.body()?.data?.content ?: emptyList()
                    val now = Date()
                    val ticket = tickets.firstOrNull { t ->
                        t.eventId == eventId &&
                        t.status.equals("PAID", ignoreCase = true) &&
                        (parseDate(t.eventEndDate)?.after(now) ?: true) &&
                        !t.status.equals("CANCELLED", ignoreCase = true)
                    }
                    _myTicketForEventState.value = ResourceState.Success(ticket)
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy vé"
                    _myTicketForEventState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _myTicketForEventState.value = ResourceState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    /**
     * Lấy thông tin loại vé theo ID (từ API hoặc local)
     */
    fun getTicketTypeById(ticketTypeId: String) {
        _ticketTypeState.value = ResourceState.Loading
        viewModelScope.launch {
            try {
                // Nếu có API riêng cho ticket type, gọi ở đây. Nếu không, lấy từ vé của user hoặc event.
                // Tạm thời lấy từ vé của user (nếu đã có trong cache)
                val response = apiService.getMyTickets(null, 0, 100)
                if (response.isSuccessful && response.body()?.success == true) {
                    val tickets = response.body()?.data?.content ?: emptyList()
                    val ticketType = tickets.mapNotNull { it.ticketTypeId to it.ticketTypeName }
                        .distinctBy { it.first }
                        .find { it.first == ticketTypeId }
                    if (ticketType != null) {
                        // Tạo TicketTypeDto tạm thời (chỉ có id và name)
                        _ticketTypeState.value = ResourceState.Success(
                            TicketTypeDto(
                                id = ticketType.first,
                                eventId = "",
                                name = ticketType.second,
                                description = null,
                                price = 0.0,
                                quantity = 0,
                                quantitySold = 0,
                                maxPerOrder = null,
                                minPerOrder = 1,
                                saleStartDate = null,
                                saleEndDate = null
                            )
                        )
                    } else {
                        _ticketTypeState.value = ResourceState.Error("Không tìm thấy loại vé")
                    }
                } else {
                    _ticketTypeState.value = ResourceState.Error("Không thể lấy loại vé")
                }
            } catch (e: Exception) {
                _ticketTypeState.value = ResourceState.Error(e.message ?: "Lỗi không xác định")
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
                Timber.e(exception, "Lỗi kết nối: IOException")
                stateFlow.value = ResourceState.Error("Lỗi kết nối: ${exception.message ?: "Không xác định"}")
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
    fun resetTicketsError() {
        if (_ticketsState.value is ResourceState.Error) {
            _ticketsState.value = ResourceState.Initial
        }
    }
    
    fun resetTicketDetailError() {
        if (_ticketDetailState.value is ResourceState.Error) {
            _ticketDetailState.value = ResourceState.Initial
        }
    }
    
    fun resetPurchaseError() {
        if (_purchaseState.value is ResourceState.Error) {
            _purchaseState.value = ResourceState.Initial
        }
    }
    
    fun resetCheckInError() {
        if (_checkInState.value is ResourceState.Error) {
            _checkInState.value = ResourceState.Initial
        }
    }

    // Hàm tiện ích để phân tích chuỗi ngày thành đối tượng Date
    private fun parseDate(dateString: String): Date? {
        return try {
            val format = if (dateString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            }
            format.parse(dateString)
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi phân tích chuỗi ngày: $dateString")
            null
        }
    }
} 