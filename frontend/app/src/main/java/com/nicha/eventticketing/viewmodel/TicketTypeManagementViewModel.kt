package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto
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

@HiltViewModel
class TicketTypeManagementViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    
    // State cho danh sách loại vé của sự kiện
    private val _ticketTypesState = MutableStateFlow<ResourceState<List<TicketTypeDto>>>(ResourceState.Initial)
    val ticketTypesState: StateFlow<ResourceState<List<TicketTypeDto>>> = _ticketTypesState.asStateFlow()
    
    // State cho việc tạo loại vé mới
    private val _createTicketTypeState = MutableStateFlow<ResourceState<TicketTypeDto>>(ResourceState.Initial)
    val createTicketTypeState: StateFlow<ResourceState<TicketTypeDto>> = _createTicketTypeState.asStateFlow()
    
    // State cho việc cập nhật loại vé
    private val _updateTicketTypeState = MutableStateFlow<ResourceState<TicketTypeDto>>(ResourceState.Initial)
    val updateTicketTypeState: StateFlow<ResourceState<TicketTypeDto>> = _updateTicketTypeState.asStateFlow()
    
    // State cho việc xóa loại vé
    private val _deleteTicketTypeState = MutableStateFlow<ResourceState<Boolean>>(ResourceState.Initial)
    val deleteTicketTypeState: StateFlow<ResourceState<Boolean>> = _deleteTicketTypeState.asStateFlow()
    
    /**
     * Lấy danh sách loại vé của sự kiện
     */
    fun getTicketTypes(eventId: String) {
        _ticketTypesState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy danh sách loại vé cho sự kiện: $eventId")
                val response = apiService.getTicketTypes(eventId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val pageResponse = response.body()?.data
                    if (pageResponse != null) {
                        val ticketTypes = pageResponse.content
                        _ticketTypesState.value = ResourceState.Success(ticketTypes)
                        Timber.d("Lấy danh sách loại vé thành công: ${ticketTypes.size} loại vé")
                    } else {
                        Timber.e("Không thể lấy danh sách loại vé từ response")
                        _ticketTypesState.value = ResourceState.Error("Không thể lấy danh sách loại vé")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy danh sách loại vé"
                    Timber.e("Lấy danh sách loại vé thất bại: $errorMessage")
                    _ticketTypesState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy danh sách loại vé", _ticketTypesState)
            }
        }
    }
    
    /**
     * Tạo loại vé mới
     */
    fun createTicketType(ticketType: TicketTypeDto) {
        _createTicketTypeState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang tạo loại vé mới: ${ticketType.name}")
                val response = apiService.createTicketType(ticketType.eventId, ticketType)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val createdTicketType = response.body()?.data
                    if (createdTicketType != null) {
                        _createTicketTypeState.value = ResourceState.Success(createdTicketType)
                        Timber.d("Tạo loại vé thành công: ${createdTicketType.id}")
                    } else {
                        Timber.e("Không thể lấy thông tin loại vé đã tạo từ response")
                        _createTicketTypeState.value = ResourceState.Error("Không thể tạo loại vé")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể tạo loại vé"
                    Timber.e("Tạo loại vé thất bại: $errorMessage")
                    _createTicketTypeState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "tạo loại vé", _createTicketTypeState)
            }
        }
    }
    
    /**
     * Cập nhật loại vé
     */
    fun updateTicketType(ticketType: TicketTypeDto) {
        _updateTicketTypeState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang cập nhật loại vé: ${ticketType.id}")
                val response = apiService.updateTicketType(ticketType.id, ticketType)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedTicketType = response.body()?.data
                    if (updatedTicketType != null) {
                        _updateTicketTypeState.value = ResourceState.Success(updatedTicketType)
                        Timber.d("Cập nhật loại vé thành công: ${updatedTicketType.id}")
                    } else {
                        Timber.e("Không thể lấy thông tin loại vé đã cập nhật từ response")
                        _updateTicketTypeState.value = ResourceState.Error("Không thể cập nhật loại vé")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể cập nhật loại vé"
                    Timber.e("Cập nhật loại vé thất bại: $errorMessage")
                    _updateTicketTypeState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "cập nhật loại vé", _updateTicketTypeState)
            }
        }
    }
    
    /**
     * Xóa loại vé
     */
    fun deleteTicketType(ticketTypeId: String, eventId: String) {
        _deleteTicketTypeState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang xóa loại vé: $ticketTypeId")
                val response = apiService.deleteTicketType(ticketTypeId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val result = response.body()?.data
                    if (result != null && result) {
                        _deleteTicketTypeState.value = ResourceState.Success(true)
                        Timber.d("Xóa loại vé thành công: $ticketTypeId")
                        
                        // Cập nhật lại danh sách loại vé
                        getTicketTypes(eventId)
                    } else {
                        Timber.e("Không thể xóa loại vé")
                        _deleteTicketTypeState.value = ResourceState.Error("Không thể xóa loại vé")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể xóa loại vé"
                    Timber.e("Xóa loại vé thất bại: $errorMessage")
                    _deleteTicketTypeState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "xóa loại vé", _deleteTicketTypeState)
            }
        }
    }
    
    /**
     * Reset states
     */
    fun resetCreateTicketTypeState() {
        _createTicketTypeState.value = ResourceState.Initial
    }
    
    fun resetUpdateTicketTypeState() {
        _updateTicketTypeState.value = ResourceState.Initial
    }
    
    fun resetDeleteTicketTypeState() {
        _deleteTicketTypeState.value = ResourceState.Initial
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
                Timber.e(exception, "Lỗi kết nối: Lỗi IO")
                stateFlow.value = ResourceState.Error("Lỗi kết nối mạng. Vui lòng kiểm tra kết nối mạng của bạn.")
            }
            is HttpException -> {
                val errorMessage = exception.message ?: "Đã xảy ra lỗi không xác định"
                Timber.e(exception, "Lỗi HTTP khi $action: $errorMessage")
                stateFlow.value = ResourceState.Error("Đã xảy ra lỗi khi $action. Vui lòng thử lại sau.")
            }
            else -> {
                Timber.e(exception, "Lỗi không xác định khi $action")
                stateFlow.value = ResourceState.Error("Đã xảy ra lỗi không xác định. Vui lòng thử lại sau.")
            }
        }
    }
} 