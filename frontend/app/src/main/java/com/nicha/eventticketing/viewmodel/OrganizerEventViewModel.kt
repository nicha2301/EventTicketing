package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * ViewModel để quản lý sự kiện của người tổ chức
 */
@HiltViewModel
class OrganizerEventViewModel @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    // State cho danh sách sự kiện của organizer
    private val _organizerEventsState = MutableStateFlow<ResourceState<PageDto<EventDto>>>(ResourceState.Initial)
    val organizerEventsState: StateFlow<ResourceState<PageDto<EventDto>>> = _organizerEventsState.asStateFlow()
    
    // State cho chi tiết sự kiện
    private val _eventDetailState = MutableStateFlow<ResourceState<EventDto>>(ResourceState.Initial)
    val eventDetailState: StateFlow<ResourceState<EventDto>> = _eventDetailState.asStateFlow()
    
    // State cho việc tạo sự kiện
    private val _createEventState = MutableStateFlow<ResourceState<EventDto>>(ResourceState.Initial)
    val createEventState: StateFlow<ResourceState<EventDto>> = _createEventState.asStateFlow()
    
    // State cho việc cập nhật sự kiện
    private val _updateEventState = MutableStateFlow<ResourceState<EventDto>>(ResourceState.Initial)
    val updateEventState: StateFlow<ResourceState<EventDto>> = _updateEventState.asStateFlow()
    
    // State cho việc xóa sự kiện
    private val _deleteEventState = MutableStateFlow<ResourceState<Boolean>>(ResourceState.Initial)
    val deleteEventState: StateFlow<ResourceState<Boolean>> = _deleteEventState.asStateFlow()
    
    // State cho việc công bố sự kiện
    private val _publishEventState = MutableStateFlow<ResourceState<EventDto>>(ResourceState.Initial)
    val publishEventState: StateFlow<ResourceState<EventDto>> = _publishEventState.asStateFlow()
    
    // State cho việc hủy sự kiện
    private val _cancelEventState = MutableStateFlow<ResourceState<EventDto>>(ResourceState.Initial)
    val cancelEventState: StateFlow<ResourceState<EventDto>> = _cancelEventState.asStateFlow()
    
    /**
     * Lấy danh sách sự kiện của người tổ chức
     */
    fun getOrganizerEvents(page: Int = 0, size: Int = 20) {
        _organizerEventsState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                val userId = preferencesManager.getUserId()
                if (userId.isEmpty()) {
                    _organizerEventsState.value = ResourceState.Error("Không tìm thấy ID người dùng")
                    return@launch
                }
                
                Timber.d("Đang lấy danh sách sự kiện của người tổ chức: $userId")
                val response = apiService.getOrganizerEvents(userId, page, size)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val events = response.body()?.data
                    if (events != null) {
                        _organizerEventsState.value = ResourceState.Success(events)
                        Timber.d("Lấy danh sách sự kiện của người tổ chức thành công: ${events.content.size} sự kiện")
                    } else {
                        Timber.e("Không thể lấy danh sách sự kiện từ response")
                        _organizerEventsState.value = ResourceState.Error("Không thể lấy danh sách sự kiện")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy danh sách sự kiện"
                    Timber.e("Lấy danh sách sự kiện thất bại: $errorMessage")
                    _organizerEventsState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy danh sách sự kiện", _organizerEventsState)
            }
        }
    }
    
    /**
     * Lấy chi tiết sự kiện theo ID
     */
    fun getEventById(eventId: String) {
        _eventDetailState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy chi tiết sự kiện: $eventId")
                val response = apiService.getEventById(eventId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val event = response.body()?.data
                    if (event != null) {
                        _eventDetailState.value = ResourceState.Success(event)
                        Timber.d("Lấy chi tiết sự kiện thành công: ${event.title}")
                    } else {
                        Timber.e("Không thể lấy chi tiết sự kiện từ response")
                        _eventDetailState.value = ResourceState.Error("Không thể lấy chi tiết sự kiện")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy chi tiết sự kiện"
                    Timber.e("Lấy chi tiết sự kiện thất bại: $errorMessage")
                    _eventDetailState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy chi tiết sự kiện", _eventDetailState)
            }
        }
    }
    
    /**
     * Tạo sự kiện mới
     */
    fun createEvent(event: EventDto) {
        _createEventState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang tạo sự kiện mới: ${event.title}")
                val response = apiService.createEvent(event)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val createdEvent = response.body()?.data
                    if (createdEvent != null) {
                        _createEventState.value = ResourceState.Success(createdEvent)
                        Timber.d("Tạo sự kiện thành công: ${createdEvent.id}")
                    } else {
                        Timber.e("Không thể lấy thông tin sự kiện đã tạo từ response")
                        _createEventState.value = ResourceState.Error("Không thể tạo sự kiện")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể tạo sự kiện"
                    Timber.e("Tạo sự kiện thất bại: $errorMessage")
                    _createEventState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "tạo sự kiện", _createEventState)
            }
        }
    }
    
    /**
     * Cập nhật sự kiện
     */
    fun updateEvent(eventId: String, event: EventDto) {
        _updateEventState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang cập nhật sự kiện: $eventId")
                val response = apiService.updateEvent(eventId, event)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedEvent = response.body()?.data
                    if (updatedEvent != null) {
                        _updateEventState.value = ResourceState.Success(updatedEvent)
                        Timber.d("Cập nhật sự kiện thành công: ${updatedEvent.id}")
                    } else {
                        Timber.e("Không thể lấy thông tin sự kiện đã cập nhật từ response")
                        _updateEventState.value = ResourceState.Error("Không thể cập nhật sự kiện")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể cập nhật sự kiện"
                    Timber.e("Cập nhật sự kiện thất bại: $errorMessage")
                    _updateEventState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "cập nhật sự kiện", _updateEventState)
            }
        }
    }
    
    /**
     * Cập nhật sự kiện với các tham số riêng lẻ
     */
    fun updateEvent(
        eventId: String,
        title: String,
        description: String,
        locationName: String,
        address: String,
        city: String,
        categoryId: String
    ) {
        _updateEventState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                // Lấy thông tin sự kiện hiện tại
                val currentEventResponse = apiService.getEventById(eventId)
                
                if (currentEventResponse.isSuccessful && currentEventResponse.body()?.success == true) {
                    val currentEvent = currentEventResponse.body()?.data
                    
                    if (currentEvent != null) {
                        // Tạo event mới với các thông tin được cập nhật
                        val updatedEvent = currentEvent.copy(
                            title = title,
                            description = description,
                            locationName = locationName,
                            address = address,
                            city = city,
                            categoryId = categoryId
                        )
                        
                        // Gọi API cập nhật
                        val response = apiService.updateEvent(eventId, updatedEvent)
                        
                        if (response.isSuccessful && response.body()?.success == true) {
                            val updatedEventResponse = response.body()?.data
                            if (updatedEventResponse != null) {
                                _updateEventState.value = ResourceState.Success(updatedEventResponse)
                                Timber.d("Cập nhật sự kiện thành công: ${updatedEventResponse.id}")
                            } else {
                                Timber.e("Không thể lấy thông tin sự kiện đã cập nhật từ response")
                                _updateEventState.value = ResourceState.Error("Không thể cập nhật sự kiện")
                            }
                        } else {
                            val errorMessage = response.body()?.message ?: "Không thể cập nhật sự kiện"
                            Timber.e("Cập nhật sự kiện thất bại: $errorMessage")
                            _updateEventState.value = ResourceState.Error(errorMessage)
                        }
                    } else {
                        Timber.e("Không thể lấy thông tin sự kiện hiện tại")
                        _updateEventState.value = ResourceState.Error("Không thể lấy thông tin sự kiện hiện tại")
                    }
                } else {
                    val errorMessage = currentEventResponse.body()?.message ?: "Không thể lấy thông tin sự kiện hiện tại"
                    Timber.e("Lấy thông tin sự kiện hiện tại thất bại: $errorMessage")
                    _updateEventState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "cập nhật sự kiện", _updateEventState)
            }
        }
    }
    
    /**
     * Xóa sự kiện
     */
    fun deleteEvent(eventId: String) {
        _deleteEventState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang xóa sự kiện: $eventId")
                val response = apiService.deleteEvent(eventId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val result = response.body()?.data
                    if (result != null && result) {
                        _deleteEventState.value = ResourceState.Success(true)
                        Timber.d("Xóa sự kiện thành công: $eventId")
                    } else {
                        Timber.e("Không thể xóa sự kiện")
                        _deleteEventState.value = ResourceState.Error("Không thể xóa sự kiện")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể xóa sự kiện"
                    Timber.e("Xóa sự kiện thất bại: $errorMessage")
                    _deleteEventState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "xóa sự kiện", _deleteEventState)
            }
        }
    }
    
    /**
     * Công bố sự kiện
     */
    fun publishEvent(eventId: String) {
        _publishEventState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang công bố sự kiện: $eventId")
                val response = apiService.publishEvent(eventId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val publishedEvent = response.body()?.data
                    if (publishedEvent != null) {
                        _publishEventState.value = ResourceState.Success(publishedEvent)
                        Timber.d("Công bố sự kiện thành công: ${publishedEvent.id}")
                    } else {
                        Timber.e("Không thể lấy thông tin sự kiện đã công bố từ response")
                        _publishEventState.value = ResourceState.Error("Không thể công bố sự kiện")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể công bố sự kiện"
                    Timber.e("Công bố sự kiện thất bại: $errorMessage")
                    _publishEventState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "công bố sự kiện", _publishEventState)
            }
        }
    }
    
    /**
     * Hủy sự kiện
     */
    fun cancelEvent(eventId: String, reason: String) {
        _cancelEventState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang hủy sự kiện: $eventId, Lý do: $reason")
                val response = apiService.cancelEvent(eventId, mapOf("reason" to reason))
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val cancelledEvent = response.body()?.data
                    if (cancelledEvent != null) {
                        _cancelEventState.value = ResourceState.Success(cancelledEvent)
                        Timber.d("Hủy sự kiện thành công: ${cancelledEvent.id}")
                    } else {
                        Timber.e("Không thể lấy thông tin sự kiện đã hủy từ response")
                        _cancelEventState.value = ResourceState.Error("Không thể hủy sự kiện")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể hủy sự kiện"
                    Timber.e("Hủy sự kiện thất bại: $errorMessage")
                    _cancelEventState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "hủy sự kiện", _cancelEventState)
            }
        }
    }
    
    /**
     * Reset states
     */
    fun resetCreateEventState() {
        _createEventState.value = ResourceState.Initial
    }
    
    fun resetUpdateEventState() {
        _updateEventState.value = ResourceState.Initial
    }
    
    fun resetDeleteEventState() {
        _deleteEventState.value = ResourceState.Initial
    }
    
    fun resetPublishEventState() {
        _publishEventState.value = ResourceState.Initial
    }
    
    fun resetCancelEventState() {
        _cancelEventState.value = ResourceState.Initial
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

    /**
     * Lấy ID người dùng hiện tại từ PreferencesManager
     */
    private suspend fun getUserId(): String {
        return preferencesManager.getUserId()
    }
} 