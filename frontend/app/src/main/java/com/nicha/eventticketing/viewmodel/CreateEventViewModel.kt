package com.nicha.eventticketing.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.data.remote.dto.category.CategoryResponse
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.location.LocationDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.CategoryRepository
import com.nicha.eventticketing.domain.repository.EventImageRepository
import com.nicha.eventticketing.domain.repository.EventRepository
import com.nicha.eventticketing.domain.repository.LocationRepository
import com.nicha.eventticketing.util.ImagePickerUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val apiService: ApiService,
    private val categoryRepository: CategoryRepository,
    private val locationRepository: LocationRepository,
    private val eventRepository: EventRepository,
    private val eventImageRepository: EventImageRepository,
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
    fun createEvent(
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
        featuredImageUri: Uri? = null,
        bannerImageUri: Uri? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _uiState.value = UiState.Loading
            
            val eventDto = EventDto(
                id = "",  
                title = title,
                description = description,
                shortDescription = shortDescription,
                organizerId = "", 
                organizerName = "", 
                categoryId = categoryId,
                categoryName = "", 
                locationId = locationId,
                locationName = "", 
                address = address,
                city = city,
                latitude = null, 
                longitude = null,  
                status = if (isDraft) "DRAFT" else "PUBLISHED",
                maxAttendees = maxAttendees,
                currentAttendees = 0,
                featuredImageUrl = null,
                imageUrls = emptyList(),
                minTicketPrice = null,
                maxTicketPrice = null,
                startDate = startDate,
                endDate = endDate,
                createdAt = "", 
                updatedAt = "", 
                isPrivate = isPrivate,
                isFeatured = false,
                isFree = isFree,
                ticketTypes = null
            )
            
            eventRepository.createEvent(eventDto).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.id?.let { eventId ->
                            // Tải ảnh lên nếu có
                            uploadEventImages(eventId, featuredImageUri, bannerImageUri)
                            _uiState.value = UiState.Success(eventId)
                        } ?: run {
                            _uiState.value = UiState.Error("Không thể tạo sự kiện: Không nhận được ID sự kiện")
                            _error.value = "Không thể tạo sự kiện: Không nhận được ID sự kiện"
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
    
    /**
     * Tải ảnh sự kiện lên
     */
    private suspend fun uploadEventImages(eventId: String, featuredImageUri: Uri?, bannerImageUri: Uri?) {
        try {
            // Tải ảnh đại diện
            featuredImageUri?.let { uri ->
                ImagePickerUtil.uriToFile(context, uri)?.let { file ->
                    uploadEventImage(eventId, file, true)
                }
            }
            
            // Tải ảnh banner
            bannerImageUri?.let { uri ->
                ImagePickerUtil.uriToFile(context, uri)?.let { file ->
                    uploadEventImage(eventId, file, false)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi tải ảnh sự kiện")
            // Không throw exception ở đây để không ảnh hưởng đến luồng tạo sự kiện
        }
    }
    
    /**
     * Tải một ảnh sự kiện lên
     */
    private suspend fun uploadEventImage(eventId: String, imageFile: File, isPrimary: Boolean) {
        try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            
            val result = eventImageRepository.uploadEventImage(eventId, imagePart, isPrimary).first()
            
            when (result) {
                is Resource.Success -> {
                    Timber.d("Tải ảnh thành công: ${result.data?.id}")
                }
                is Resource.Error -> {
                    Timber.e("Lỗi khi tải ảnh: ${result.message}")
                }
                is Resource.Loading -> {
                    // Đang tải
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi tải ảnh")
        }
    }
    
    // Hàm xử lý lỗi từ server để lấy thông báo lỗi cụ thể
    private fun parseErrorBody(responseBody: ResponseBody?): String {
        return try {
            responseBody?.string()?.let { errorBody ->
                try {
                    val jsonObject = JSONObject(errorBody)
                    val errorMessage = jsonObject.optString("message", null)
                    
                    if (errorMessage != null) {
                        // Nếu có lỗi validation (MethodArgumentNotValidException)
                        if (errorMessage.contains("Validation failed for argument") || 
                            errorMessage.contains("MethodArgumentNotValidException")) {
                            
                            // Cố gắng tìm thông báo lỗi trong errorBody
                            val defaultMessageMatch = "default message \\[(.+?)\\]".toRegex().find(errorMessage)
                            val fieldErrorMatch = "Field error in object '(.+?)' on field '(.+?)'".toRegex().find(errorMessage)
                            
                            if (defaultMessageMatch != null && fieldErrorMatch != null) {
                                val defaultMessage = defaultMessageMatch.groupValues[1]
                                val field = fieldErrorMatch.groupValues[2]
                                
                                when (field) {
                                    "startDate" -> return "Ngày bắt đầu phải là ngày trong tương lai"
                                    "endDate" -> return "Ngày kết thúc phải sau ngày bắt đầu"
                                    else -> return "$defaultMessage ($field)"
                                }
                            }
                        } else if (errorMessage.contains("Failed to deserialize java.time.LocalDateTime")) {
                            return "Lỗi định dạng ngày tháng. Vui lòng thử lại."
                        }
                        return errorMessage
                    }
                    
                    // Thử lấy thông báo lỗi từ các trường khác
                    jsonObject.optString("error", "Lỗi không xác định từ server")
                } catch (e: Exception) {
                    // Nếu không phải JSON, trả về chuỗi gốc
                    errorBody
                }
            } ?: "Không thể lấy thông tin lỗi từ server"
        } catch (e: IllegalStateException) {
            // Xử lý lỗi "closed" từ OkHttp
            if (e.message?.contains("closed") == true) {
                Timber.e(e, "Lỗi IllegalStateException: closed khi phân tích lỗi")
                return "Lỗi kết nối với server. Vui lòng thử lại sau."
            } else {
                Timber.e(e, "Lỗi IllegalStateException khi phân tích lỗi")
                return "Lỗi không xác định: ${e.message}"
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi phân tích thông báo lỗi từ server")
            "Lỗi không xác định từ server"
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