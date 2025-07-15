package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.dto.event.EventDto
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

/**
 * ViewModel để quản lý dữ liệu sự kiện
 */
@HiltViewModel
class EventViewModel @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
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
            try {
                Timber.d("Đang lấy danh sách sự kiện nổi bật")
                val response = apiService.getFeaturedEvents(limit)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val events = response.body()?.data
                    if (events != null) {
                        _featuredEventsState.value = ResourceState.Success(events)
                        Timber.d("Lấy danh sách sự kiện nổi bật thành công: ${events.size} sự kiện")
                    } else {
                        Timber.e("Không thể lấy danh sách sự kiện nổi bật từ response")
                        _featuredEventsState.value = ResourceState.Error("Không thể lấy danh sách sự kiện nổi bật")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy danh sách sự kiện nổi bật"
                    Timber.e("Lấy danh sách sự kiện nổi bật thất bại: $errorMessage")
                    _featuredEventsState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy danh sách sự kiện nổi bật", _featuredEventsState)
            }
        }
    }
    
    /**
     * Lấy danh sách sự kiện sắp diễn ra
     */
    fun getUpcomingEvents(limit: Int = 10) {
        _upcomingEventsState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy danh sách sự kiện sắp diễn ra")
                val response = apiService.getUpcomingEvents(limit)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val events = response.body()?.data
                    if (events != null) {
                        _upcomingEventsState.value = ResourceState.Success(events)
                        Timber.d("Lấy danh sách sự kiện sắp diễn ra thành công: ${events.size} sự kiện")
                    } else {
                        Timber.e("Không thể lấy danh sách sự kiện sắp diễn ra từ response")
                        _upcomingEventsState.value = ResourceState.Error("Không thể lấy danh sách sự kiện sắp diễn ra")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy danh sách sự kiện sắp diễn ra"
                    Timber.e("Lấy danh sách sự kiện sắp diễn ra thất bại: $errorMessage")
                    _upcomingEventsState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy danh sách sự kiện sắp diễn ra", _upcomingEventsState)
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
            try {
                Timber.d("Đang lấy danh sách sự kiện theo danh mục: $categoryId")
                val response = apiService.searchEvents(
                    categoryId = categoryId,
                    page = page,
                    size = size
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val events = response.body()?.data?.content
                    if (events != null) {
                        _categoryEventsState.value = ResourceState.Success(events)
                        Timber.d("Lấy danh sách sự kiện theo danh mục thành công: ${events.size} sự kiện")
                    } else {
                        Timber.e("Không thể lấy danh sách sự kiện theo danh mục từ response")
                        _categoryEventsState.value = ResourceState.Error("Không thể lấy danh sách sự kiện theo danh mục")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy danh sách sự kiện theo danh mục"
                    Timber.e("Lấy danh sách sự kiện theo danh mục thất bại: $errorMessage")
                    _categoryEventsState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy danh sách sự kiện theo danh mục", _categoryEventsState)
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
            try {
                Timber.d("Đang tìm kiếm sự kiện với keyword: $keyword, categoryId: $categoryId")
                val response = apiService.searchEvents(
                    keyword = keyword,
                    categoryId = categoryId,
                    startDate = startDate,
                    endDate = endDate,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    page = page,
                    size = size
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val events = response.body()?.data?.content
                    if (events != null) {
                        _searchEventsState.value = ResourceState.Success(events)
                        Timber.d("Tìm kiếm sự kiện thành công: ${events.size} kết quả")
                    } else {
                        Timber.e("Không thể lấy kết quả tìm kiếm sự kiện từ response")
                        _searchEventsState.value = ResourceState.Error("Không thể lấy kết quả tìm kiếm sự kiện")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể tìm kiếm sự kiện"
                    Timber.e("Tìm kiếm sự kiện thất bại: $errorMessage")
                    _searchEventsState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "tìm kiếm sự kiện", _searchEventsState)
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