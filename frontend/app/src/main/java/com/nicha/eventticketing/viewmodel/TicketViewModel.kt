package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseResponseDto
import com.nicha.eventticketing.data.remote.dto.ticket.CheckInRequestDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.domain.repository.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel để quản lý dữ liệu vé
 */
@HiltViewModel
class TicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
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
            ticketRepository.getMyTickets(status, page, size).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val pageDto = result.data
                        if (pageDto != null) {
                            val tickets = pageDto.content
                            _ticketsState.value = ResourceState.Success(tickets)
                            Timber.d("Lấy danh sách vé thành công: ${tickets.size} vé")
                        } else {
                            Timber.e("Không tìm thấy vé")
                            _ticketsState.value = ResourceState.Error("Không tìm thấy vé")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy danh sách vé thất bại: ${result.message}")
                        _ticketsState.value = ResourceState.Error(result.message ?: "Không thể lấy danh sách vé")
                    }
                    is Resource.Loading -> {
                        _ticketsState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Lấy danh sách vé của người dùng với bộ lọc nâng cao
     */
    fun getMyTicketsWithFilter(tabId: String, page: Int = 0, size: Int = 10) {
        _ticketsState.value = ResourceState.Loading
        
        viewModelScope.launch {
            val status = when (tabId) {
                "active" -> "PAID"
                "expired" -> "EXPIRED"
                "cancelled" -> "CANCELLED"
                else -> null
            }
            
            ticketRepository.getMyTickets(status, page, size).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val pageDto = result.data
                        if (pageDto != null) {
                            var tickets = pageDto.content
                            
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
                            Timber.e("Không tìm thấy vé")
                            _ticketsState.value = ResourceState.Error("Không tìm thấy vé")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy danh sách vé thất bại: ${result.message}")
                        _ticketsState.value = ResourceState.Error(result.message ?: "Không thể lấy danh sách vé")
                    }
                    is Resource.Loading -> {
                        _ticketsState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Lấy chi tiết vé theo ID
     */
    fun getTicketById(ticketId: String) {
        _ticketDetailState.value = ResourceState.Loading
        
        viewModelScope.launch {
            ticketRepository.getTicketById(ticketId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val ticket = result.data
                        if (ticket != null) {
                            _ticketDetailState.value = ResourceState.Success(ticket)
                            Timber.d("Lấy chi tiết vé thành công: ${ticket.ticketNumber}")
                        } else {
                            Timber.e("Không tìm thấy vé")
                            _ticketDetailState.value = ResourceState.Error("Không tìm thấy vé")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy chi tiết vé thất bại: ${result.message}")
                        _ticketDetailState.value = ResourceState.Error(result.message ?: "Không thể lấy chi tiết vé")
                    }
                    is Resource.Loading -> {
                        _ticketDetailState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Lấy chi tiết vé theo mã vé
     */
    fun getTicketByNumber(ticketNumber: String) {
        _ticketDetailState.value = ResourceState.Loading
        
        viewModelScope.launch {
            ticketRepository.getTicketByNumber(ticketNumber).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val ticket = result.data
                        if (ticket != null) {
                            _ticketDetailState.value = ResourceState.Success(ticket)
                            Timber.d("Lấy chi tiết vé thành công: ${ticket.ticketNumber}")
                        } else {
                            Timber.e("Không tìm thấy vé")
                            _ticketDetailState.value = ResourceState.Error("Không tìm thấy vé")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy chi tiết vé thất bại: ${result.message}")
                        _ticketDetailState.value = ResourceState.Error(result.message ?: "Không thể lấy chi tiết vé")
                    }
                    is Resource.Loading -> {
                        _ticketDetailState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Mua vé
     */
    fun purchaseTickets(purchaseDto: TicketPurchaseDto) {
        _purchaseState.value = ResourceState.Loading
        
        viewModelScope.launch {
            ticketRepository.purchaseTickets(purchaseDto).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val purchaseResponse = result.data
                        if (purchaseResponse != null) {
                            _purchaseState.value = ResourceState.Success(purchaseResponse)
                            Timber.d("Mua vé thành công: ${purchaseResponse.paymentId}")
                        } else {
                            Timber.e("Không thể hoàn tất việc mua vé")
                            _purchaseState.value = ResourceState.Error("Không thể hoàn tất việc mua vé")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Mua vé thất bại: ${result.message}")
                        _purchaseState.value = ResourceState.Error(result.message ?: "Không thể mua vé")
                    }
                    is Resource.Loading -> {
                        _purchaseState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Check-in vé
     */
    fun checkInTicket(ticketId: String, eventId: String, userId: String) {
        _checkInState.value = ResourceState.Loading
        
        viewModelScope.launch {
            val request = CheckInRequestDto(
                ticketId = ticketId,
                eventId = eventId,
                userId = userId
            )
            
            ticketRepository.checkInTicket(request).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val ticket = result.data
                        if (ticket != null) {
                            _checkInState.value = ResourceState.Success(ticket)
                            Timber.d("Check-in vé thành công: ${ticket.ticketNumber}")
                        } else {
                            Timber.e("Không thể hoàn tất việc check-in vé")
                            _checkInState.value = ResourceState.Error("Không thể hoàn tất việc check-in vé")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Check-in vé thất bại: ${result.message}")
                        _checkInState.value = ResourceState.Error(result.message ?: "Không thể check-in vé")
                    }
                    is Resource.Loading -> {
                        _checkInState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Hủy vé
     */
    fun cancelTicket(ticketId: String) {
        _ticketDetailState.value = ResourceState.Loading
        
        viewModelScope.launch {
            ticketRepository.cancelTicket(ticketId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val ticket = result.data
                        if (ticket != null) {
                            _ticketDetailState.value = ResourceState.Success(ticket)
                            Timber.d("Hủy vé thành công: ${ticket.ticketNumber}")
                        } else {
                            Timber.e("Không thể hoàn tất việc hủy vé")
                            _ticketDetailState.value = ResourceState.Error("Không thể hoàn tất việc hủy vé")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Hủy vé thất bại: ${result.message}")
                        _ticketDetailState.value = ResourceState.Error(result.message ?: "Không thể hủy vé")
                    }
                    is Resource.Loading -> {
                        _ticketDetailState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Lấy vé hợp lệ của user cho một event cụ thể
     */
    fun getMyTicketsByEventId(eventId: String) {
        _myTicketForEventState.value = ResourceState.Loading
        viewModelScope.launch {
            ticketRepository.getMyTickets(null, 0, 100).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val pageDto = result.data
                        if (pageDto != null) {
                            val tickets = pageDto.content
                            val now = Date()
                            val ticket = tickets.firstOrNull { t ->
                                t.eventId == eventId &&
                                t.status.equals("PAID", ignoreCase = true) &&
                                (parseDate(t.eventEndDate)?.after(now) ?: true) &&
                                !t.status.equals("CANCELLED", ignoreCase = true)
                            }
                            _myTicketForEventState.value = ResourceState.Success(ticket)
                        } else {
                            _myTicketForEventState.value = ResourceState.Error("Không tìm thấy vé")
                        }
                    }
                    is Resource.Error -> {
                        _myTicketForEventState.value = ResourceState.Error(result.message ?: "Không thể lấy vé")
                    }
                    is Resource.Loading -> {
                        _myTicketForEventState.value = ResourceState.Loading
                    }
                }
            }
        }
    }

    /**
     * Lấy thông tin loại vé theo ID (từ API hoặc local)
     */
    fun getTicketTypeById(ticketTypeId: String) {
        _ticketTypeState.value = ResourceState.Loading
        viewModelScope.launch {
            ticketRepository.getMyTickets(null, 0, 100).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val pageDto = result.data
                        if (pageDto != null) {
                            val tickets = pageDto.content
                            val ticketType = tickets.mapNotNull { it.ticketTypeId to it.ticketTypeName }
                                .distinctBy { it.first }
                                .find { it.first == ticketTypeId }
                            if (ticketType != null) {
                                _ticketTypeState.value = ResourceState.Success(
                                    TicketTypeDto(
                                        id = ticketType.first,
                                        eventId = "",
                                        name = ticketType.second,
                                        description = null,
                                        price = 0.0,
                                        quantity = 0,
                                        quantitySold = 0,
                                        maxTicketsPerCustomer = null,
                                        minTicketsPerOrder = 1,
                                        salesStartDate = null,
                                        salesEndDate = null
                                    )
                                )
                            } else {
                                _ticketTypeState.value = ResourceState.Error("Không tìm thấy loại vé")
                            }
                        } else {
                            _ticketTypeState.value = ResourceState.Error("Không thể lấy loại vé")
                        }
                    }
                    is Resource.Error -> {
                        _ticketTypeState.value = ResourceState.Error(result.message ?: "Không thể lấy loại vé")
                    }
                    is Resource.Loading -> {
                        _ticketTypeState.value = ResourceState.Loading
                    }
                }
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