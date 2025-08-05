package com.nicha.eventticketing.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.data.remote.dto.category.CategoryResponse
import com.nicha.eventticketing.data.remote.dto.event.CreateEventWithImagesRequest
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.location.LocationDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.CategoryRepository
import com.nicha.eventticketing.domain.repository.EventRepository
import com.nicha.eventticketing.domain.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val locationRepository: LocationRepository,
    private val eventRepository: EventRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    // UI state
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Categories
    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()
    
    // Locations
    private val _locations = MutableStateFlow<List<LocationDto>>(emptyList())
    val locations: StateFlow<List<LocationDto>> = _locations.asStateFlow()
    
    // Load categories
    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            categoryRepository.getCategories(includeInactive = false).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.content?.let { categories ->
                            _categories.value = categories
                        }
                        _isLoading.value = false
                    }
                    is Resource.Error -> {
                        _error.value = result.message ?: "Không thể tải danh mục"
                        _isLoading.value = false
                        Timber.e("Lỗi khi tải danh mục: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }
    
    // Load locations
    fun loadLocations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            locationRepository.getLocations().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.content?.let { locations ->
                            _locations.value = locations
                        }
                        _isLoading.value = false
                    }
                    is Resource.Error -> {
                        _error.value = result.message ?: "Không thể tải địa điểm"
                        _isLoading.value = false
                        Timber.e("Lỗi khi tải địa điểm: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }
    
    // Create event
    fun createEventWithImages(
        title: String,
        description: String,
        shortDescription: String,
        categoryId: String,
        locationId: String,
        address: String,
        city: String,
        maxAttendees: Int,
        startDate: String,
        endDate: String,
        isPrivate: Boolean = false,
        isDraft: Boolean = true,
        isFree: Boolean = false,
        latitude: Double = 21.0285,
        longitude: Double = 105.8542,
        imageUris: List<Uri> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _uiState.value = UiState.Loading
            
            val eventData = CreateEventWithImagesRequest(
                title = title,
                description = description,
                shortDescription = shortDescription,
                categoryId = categoryId,
                locationId = locationId,
                address = address,
                city = city,
                latitude = latitude,
                longitude = longitude,
                maxAttendees = maxAttendees,
                startDate = startDate,
                endDate = endDate,
                isPrivate = isPrivate,
                isDraft = isDraft,
                isFree = isFree
            )
            
            eventRepository.createEventWithImages(eventData, imageUris).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { event ->
                            _uiState.value = UiState.Success(event.id)
                        } ?: run {
                            _uiState.value = UiState.Error("Không thể tạo sự kiện: Không nhận được thông tin sự kiện")
                            _error.value = "Không thể tạo sự kiện: Không nhận được thông tin sự kiện"
                        }
                        _isLoading.value = false
                    }
                    is Resource.Error -> {
                        _uiState.value = UiState.Error(result.message ?: "Không thể tạo sự kiện")
                        _error.value = result.message
                        _isLoading.value = false
                        Timber.e("Lỗi khi tạo sự kiện: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _isLoading.value = true
                        _uiState.value = UiState.Loading
                    }
                }
            }
        }
    }
    
    // UI States
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val eventId: String) : UiState()
        data class Error(val message: String) : UiState()
    }
} 