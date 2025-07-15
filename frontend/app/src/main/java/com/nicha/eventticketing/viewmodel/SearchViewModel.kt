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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.FlowPreview

/**
 * ViewModel để quản lý chức năng tìm kiếm sự kiện
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _searchEventsState = MutableStateFlow<ResourceState<List<EventDto>>>(ResourceState.Initial)
    val searchEventsState: StateFlow<ResourceState<List<EventDto>>> = _searchEventsState.asStateFlow()
    
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()
    
    private val _priceRange = MutableStateFlow<Pair<Double?, Double?>>(null to null)
    val priceRange: StateFlow<Pair<Double?, Double?>> = _priceRange.asStateFlow()
    
    private val _dateRange = MutableStateFlow<Pair<String?, String?>>(null to null)
    val dateRange: StateFlow<Pair<String?, String?>> = _dateRange.asStateFlow()
    
    private val searchQueryFlow = MutableSharedFlow<String>()
    private var searchJob: Job? = null
    
    init {
        searchQueryFlow
            .debounce(300) 
            .onEach { query ->
                performSearch(query)
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Tìm kiếm sự kiện với debounce
     */
    fun searchEventsWithDebounce(keyword: String?) {
        viewModelScope.launch {
            searchQueryFlow.emit(keyword ?: "")
        }
    }
    
    /**
     * Thực hiện tìm kiếm ngay lập tức (không debounce)
     */
    fun searchEvents(
        keyword: String? = null,
        page: Int = 0,
        size: Int = 10
    ) {
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            performSearch(keyword ?: "", page, size)
        }
    }
    
    /**
     * Thực hiện tìm kiếm thực tế
     */
    private suspend fun performSearch(
        keyword: String,
        page: Int = 0,
        size: Int = 10
    ) {
        _searchEventsState.value = ResourceState.Loading
        
        try {
            Timber.d("Đang tìm kiếm sự kiện với keyword: $keyword, categoryId: ${_selectedCategoryId.value}")
            val response = apiService.searchEvents(
                keyword = if (keyword.isBlank()) null else keyword,
                categoryId = _selectedCategoryId.value,
                startDate = _dateRange.value.first,
                endDate = _dateRange.value.second,
                minPrice = _priceRange.value.first,
                maxPrice = _priceRange.value.second,
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
    
    /**
     * Cập nhật danh mục đã chọn
     */
    fun updateSelectedCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }
    
    /**
     * Cập nhật khoảng giá
     */
    fun updatePriceRange(minPrice: Double?, maxPrice: Double?) {
        _priceRange.value = minPrice to maxPrice
    }
    
    /**
     * Cập nhật khoảng thời gian
     */
    fun updateDateRange(startDate: Date?, endDate: Date?) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDateStr = startDate?.let { dateFormat.format(it) }
        val endDateStr = endDate?.let { dateFormat.format(it) }
        _dateRange.value = startDateStr to endDateStr
    }
    
    /**
     * Reset bộ lọc
     */
    fun resetFilters() {
        _selectedCategoryId.value = null
        _priceRange.value = null to null
        _dateRange.value = null to null
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
    fun resetSearchEventsError() {
        if (_searchEventsState.value is ResourceState.Error) {
            _searchEventsState.value = ResourceState.Initial
        }
    }
} 