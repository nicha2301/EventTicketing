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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * ViewModel để quản lý chức năng tìm kiếm sự kiện
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val eventRepository: EventRepository
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
        
        eventRepository.searchEvents(
            keyword = if (keyword.isBlank()) null else keyword,
            categoryId = _selectedCategoryId.value,
            startDate = _dateRange.value.first,
            endDate = _dateRange.value.second,
            minPrice = _priceRange.value.first,
            maxPrice = _priceRange.value.second,
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
     * Reset trạng thái lỗi
     */
    fun resetSearchEventsError() {
        if (_searchEventsState.value is ResourceState.Error) {
            _searchEventsState.value = ResourceState.Initial
        }
    }
} 