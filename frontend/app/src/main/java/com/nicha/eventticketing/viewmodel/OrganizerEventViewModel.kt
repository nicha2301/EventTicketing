package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.domain.repository.EventRepository
import com.nicha.eventticketing.domain.repository.OrganizerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel để quản lý sự kiện của người tổ chức
 */
@HiltViewModel
class OrganizerEventViewModel @Inject constructor(
    private val organizerRepository: OrganizerRepository,
    private val eventRepository: EventRepository,
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
                
                organizerRepository.getOrganizerEvents(userId, page, size).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { events ->
                                _organizerEventsState.value = ResourceState.Success(events)
                                Timber.d("Lấy danh sách sự kiện của người tổ chức thành công: ${events.content?.size ?: 0} sự kiện")
                            } ?: run {
                                _organizerEventsState.value = ResourceState.Error("Không tìm thấy sự kiện")
                            }
                        }
                        is Resource.Error -> {
                            _organizerEventsState.value = ResourceState.Error(result.message ?: "Không thể lấy danh sách sự kiện")
                            Timber.e("Lấy danh sách sự kiện thất bại: ${result.message}")
                        }
                        is Resource.Loading -> {
                            _organizerEventsState.value = ResourceState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi lấy danh sách sự kiện")
                _organizerEventsState.value = ResourceState.Error(e.message ?: "Đã xảy ra lỗi không xác định")
            }
        }
    }
    
    /**
     * Lấy chi tiết sự kiện theo ID
     */
    fun getEventById(eventId: String) {
        _eventDetailState.value = ResourceState.Loading
        
        viewModelScope.launch {
            eventRepository.getEventById(eventId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { event ->
                            _eventDetailState.value = ResourceState.Success(event)
                            Timber.d("Lấy chi tiết sự kiện thành công: ${event.title}")
                        } ?: run {
                            _eventDetailState.value = ResourceState.Error("Không tìm thấy sự kiện")
                        }
                    }
                    is Resource.Error -> {
                        _eventDetailState.value = ResourceState.Error(result.message ?: "Không thể lấy chi tiết sự kiện")
                        Timber.e("Lấy chi tiết sự kiện thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _eventDetailState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Tạo sự kiện mới
     */
    fun createEvent(event: EventDto) {
        _createEventState.value = ResourceState.Loading
        
        viewModelScope.launch {
            eventRepository.createEvent(event).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { createdEvent ->
                            _createEventState.value = ResourceState.Success(createdEvent)
                            Timber.d("Tạo sự kiện thành công: ${createdEvent.id}")
                        } ?: run {
                            _createEventState.value = ResourceState.Error("Không tạo được sự kiện")
                        }
                    }
                    is Resource.Error -> {
                        _createEventState.value = ResourceState.Error(result.message ?: "Không thể tạo sự kiện")
                        Timber.e("Tạo sự kiện thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _createEventState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Cập nhật sự kiện
     */
    fun updateEvent(eventId: String, event: EventDto) {
        _updateEventState.value = ResourceState.Loading
        
        viewModelScope.launch {
            eventRepository.updateEvent(eventId, event).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { updatedEvent ->
                            _updateEventState.value = ResourceState.Success(updatedEvent)
                            Timber.d("Cập nhật sự kiện thành công: ${updatedEvent.id}")
                        } ?: run {
                            _updateEventState.value = ResourceState.Error("Không cập nhật được sự kiện")
                        }
                    }
                    is Resource.Error -> {
                        _updateEventState.value = ResourceState.Error(result.message ?: "Không thể cập nhật sự kiện")
                        Timber.e("Cập nhật sự kiện thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _updateEventState.value = ResourceState.Loading
                    }
                }
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
            eventRepository.getEventById(eventId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { currentEvent ->
                            val updatedEvent = currentEvent.copy(
                                title = title,
                                description = description,
                                locationName = locationName,
                                address = address,
                                city = city,
                                categoryId = categoryId
                            )
                            
                            eventRepository.updateEvent(eventId, updatedEvent).collect { updateResult ->
                                when (updateResult) {
                                    is Resource.Success -> {
                                        updateResult.data?.let { updatedEventResponse ->
                                            _updateEventState.value = ResourceState.Success(updatedEventResponse)
                                            Timber.d("Cập nhật sự kiện thành công: ${updatedEventResponse.id}")
                                        } ?: run {
                                            _updateEventState.value = ResourceState.Error("Không cập nhật được sự kiện")
                                        }
                                    }
                                    is Resource.Error -> {
                                        _updateEventState.value = ResourceState.Error(updateResult.message ?: "Không thể cập nhật sự kiện")
                                        Timber.e("Cập nhật sự kiện thất bại: ${updateResult.message}")
                                    }
                                    is Resource.Loading -> {
                                        _updateEventState.value = ResourceState.Loading
                                    }
                                }
                            }
                        } ?: run {
                            _updateEventState.value = ResourceState.Error("Không tìm thấy sự kiện")
                        }
                    }
                    is Resource.Error -> {
                        _updateEventState.value = ResourceState.Error(result.message ?: "Không thể lấy thông tin sự kiện hiện tại")
                        Timber.e("Lấy thông tin sự kiện hiện tại thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _updateEventState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Xóa sự kiện
     */
    fun deleteEvent(eventId: String) {
        _deleteEventState.value = ResourceState.Loading
        
        viewModelScope.launch {
            eventRepository.deleteEvent(eventId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { deleted ->
                            if (deleted) {
                                _deleteEventState.value = ResourceState.Success(true)
                                Timber.d("Xóa sự kiện thành công: $eventId")
                            } else {
                                _deleteEventState.value = ResourceState.Error("Không xóa được sự kiện")
                            }
                        } ?: run {
                            _deleteEventState.value = ResourceState.Error("Không xóa được sự kiện")
                        }
                    }
                    is Resource.Error -> {
                        _deleteEventState.value = ResourceState.Error(result.message ?: "Không thể xóa sự kiện")
                        Timber.e("Xóa sự kiện thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _deleteEventState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Công bố sự kiện
     */
    fun publishEvent(eventId: String) {
        _publishEventState.value = ResourceState.Loading
        
        viewModelScope.launch {
            eventRepository.publishEvent(eventId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { publishedEvent ->
                            _publishEventState.value = ResourceState.Success(publishedEvent)
                            Timber.d("Công bố sự kiện thành công: ${publishedEvent.id}")
                        } ?: run {
                            _publishEventState.value = ResourceState.Error("Không công bố được sự kiện")
                        }
                    }
                    is Resource.Error -> {
                        _publishEventState.value = ResourceState.Error(result.message ?: "Không thể công bố sự kiện")
                        Timber.e("Công bố sự kiện thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _publishEventState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Hủy sự kiện
     */
    fun cancelEvent(eventId: String, reason: String) {
        _cancelEventState.value = ResourceState.Loading
        
        viewModelScope.launch {
            eventRepository.cancelEvent(eventId, reason).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { cancelledEvent ->
                            _cancelEventState.value = ResourceState.Success(cancelledEvent)
                            Timber.d("Hủy sự kiện thành công: ${cancelledEvent.id}")
                        } ?: run {
                            _cancelEventState.value = ResourceState.Error("Không hủy được sự kiện")
                        }
                    }
                    is Resource.Error -> {
                        _cancelEventState.value = ResourceState.Error(result.message ?: "Không thể hủy sự kiện")
                        Timber.e("Hủy sự kiện thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _cancelEventState.value = ResourceState.Loading
                    }
                }
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
} 