package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel để quản lý dữ liệu sự kiện
 */
@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    
    // State cho danh sách sự kiện nổi bật
    private val _featuredEventsState = MutableStateFlow<ResourceState<List<EventDto>>>(ResourceState.Initial)
    val featuredEventsState: StateFlow<ResourceState<List<EventDto>>> = _featuredEventsState.asStateFlow()
    
    // State cho danh sách sự kiện sắp diễn ra
    private val _upcomingEventsState = MutableStateFlow<ResourceState<List<EventDto>>>(ResourceState.Initial)
    val upcomingEventsState: StateFlow<ResourceState<List<EventDto>>> = _upcomingEventsState.asStateFlow()
    
    // State cho chi tiết sự kiện
    private val _eventDetailState = MutableStateFlow<ResourceState<EventDto>>(ResourceState.Initial)
    val eventDetailState: StateFlow<ResourceState<EventDto>> = _eventDetailState.asStateFlow()
    
    // State cho kết quả tìm kiếm sự kiện
    private val _searchEventsState = MutableStateFlow<ResourceState<List<EventDto>>>(ResourceState.Initial)
    val searchEventsState: StateFlow<ResourceState<List<EventDto>>> = _searchEventsState.asStateFlow()
    
    // State cho danh sách sự kiện theo danh mục
    private val _categoryEventsState = MutableStateFlow<ResourceState<List<EventDto>>>(ResourceState.Initial)
    val categoryEventsState: StateFlow<ResourceState<List<EventDto>>> = _categoryEventsState.asStateFlow()
    
    // State cho danh mục đang được chọn
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()
    
    /**
     * Lấy danh sách sự kiện nổi bật
     */
    fun getFeaturedEvents(limit: Int = 10) {
        _featuredEventsState.value = ResourceState.Loading
        
        viewModelScope.launch {
            eventRepository.getFeaturedEvents(limit).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val events = result.data
                        if (events != null) {
                            _featuredEventsState.value = ResourceState.Success(events)
                            Timber.d("Lấy danh sách sự kiện nổi bật thành công: ${events.size} sự kiện")
                        } else {
                            Timber.e("Không tìm thấy sự kiện nổi bật")
                            _featuredEventsState.value = ResourceState.Error("Không tìm thấy sự kiện nổi bật")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy danh sách sự kiện nổi bật thất bại: ${result.message}")
                        _featuredEventsState.value = ResourceState.Error(result.message ?: "Không thể lấy danh sách sự kiện nổi bật")
                    }
                    is Resource.Loading -> {
                        _featuredEventsState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Lấy danh sách sự kiện sắp diễn ra
     */
    fun getUpcomingEvents(limit: Int = 10) {
        _upcomingEventsState.value = ResourceState.Loading
        
        viewModelScope.launch {
            eventRepository.getUpcomingEvents(limit).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val events = result.data
                        if (events != null) {
                            _upcomingEventsState.value = ResourceState.Success(events)
                            Timber.d("Lấy danh sách sự kiện sắp diễn ra thành công: ${events.size} sự kiện")
                        } else {
                            Timber.e("Không tìm thấy sự kiện sắp diễn ra")
                            _upcomingEventsState.value = ResourceState.Error("Không tìm thấy sự kiện sắp diễn ra")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy danh sách sự kiện sắp diễn ra thất bại: ${result.message}")
                        _upcomingEventsState.value = ResourceState.Error(result.message ?: "Không thể lấy danh sách sự kiện sắp diễn ra")
                    }
                    is Resource.Loading -> {
                        _upcomingEventsState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Lấy danh sách sự kiện theo danh mục
     */
    fun getEventsByCategory(categoryId: String, page: Int = 0, size: Int = 20) {
        _categoryEventsState.value = ResourceState.Loading
        _selectedCategoryId.value = categoryId
        
        viewModelScope.launch {
            eventRepository.searchEvents(
                categoryId = categoryId,
                page = page,
                size = size
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val events = result.data?.content
                        if (events != null) {
                            _categoryEventsState.value = ResourceState.Success(events)
                            Timber.d("Lấy danh sách sự kiện theo danh mục thành công: ${events.size} sự kiện")
                        } else {
                            Timber.e("Không tìm thấy sự kiện theo danh mục")
                            _categoryEventsState.value = ResourceState.Error("Không tìm thấy sự kiện theo danh mục")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy danh sách sự kiện theo danh mục thất bại: ${result.message}")
                        _categoryEventsState.value = ResourceState.Error(result.message ?: "Không thể lấy danh sách sự kiện theo danh mục")
                    }
                    is Resource.Loading -> {
                        _categoryEventsState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Xóa lựa chọn danh mục hiện tại
     */
    fun clearCategorySelection() {
        _selectedCategoryId.value = null
        _categoryEventsState.value = ResourceState.Initial
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
                        val event = result.data
                        if (event != null) {
                            _eventDetailState.value = ResourceState.Success(event)
                            Timber.d("Lấy chi tiết sự kiện thành công: ${event.title}")
                        } else {
                            Timber.e("Không tìm thấy sự kiện")
                            _eventDetailState.value = ResourceState.Error("Không tìm thấy sự kiện")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy chi tiết sự kiện thất bại: ${result.message}")
                        _eventDetailState.value = ResourceState.Error(result.message ?: "Không thể lấy chi tiết sự kiện")
                    }
                    is Resource.Loading -> {
                        _eventDetailState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Tìm kiếm sự kiện
     */
    fun searchEvents(
        keyword: String? = null,
        categoryId: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        page: Int = 0,
        size: Int = 10
    ) {
        _searchEventsState.value = ResourceState.Loading
        
        viewModelScope.launch {
            eventRepository.searchEvents(
                keyword = keyword,
                categoryId = categoryId,
                startDate = startDate,
                endDate = endDate,
                minPrice = minPrice,
                maxPrice = maxPrice,
                page = page,
                size = size
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val events = result.data?.content
                        if (events != null) {
                            _searchEventsState.value = ResourceState.Success(events)
                            Timber.d("Tìm kiếm sự kiện thành công: ${events.size} kết quả")
                        } else {
                            Timber.e("Không tìm thấy sự kiện phù hợp")
                            _searchEventsState.value = ResourceState.Error("Không tìm thấy sự kiện phù hợp")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Tìm kiếm sự kiện thất bại: ${result.message}")
                        _searchEventsState.value = ResourceState.Error(result.message ?: "Không thể tìm kiếm sự kiện")
                    }
                    is Resource.Loading -> {
                        _searchEventsState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Reset trạng thái lỗi
     */
    fun resetFeaturedEventsError() {
        if (_featuredEventsState.value is ResourceState.Error) {
            _featuredEventsState.value = ResourceState.Initial
        }
    }
    
    fun resetUpcomingEventsError() {
        if (_upcomingEventsState.value is ResourceState.Error) {
            _upcomingEventsState.value = ResourceState.Initial
        }
    }
    
    fun resetEventDetailError() {
        if (_eventDetailState.value is ResourceState.Error) {
            _eventDetailState.value = ResourceState.Initial
        }
    }
    
    fun resetSearchEventsError() {
        if (_searchEventsState.value is ResourceState.Error) {
            _searchEventsState.value = ResourceState.Initial
        }
    }
    
    fun resetCategoryEventsError() {
        if (_categoryEventsState.value is ResourceState.Error) {
            _categoryEventsState.value = ResourceState.Initial
        }
    }
} 