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
import com.nicha.eventticketing.util.ImagePickerUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            try {
                _isLoading.value = true
                val response = apiService.getCategories(includeInactive = false)
                if (response.isSuccessful && response.body() != null) {
                    val categoryResponse = response.body()!!
                    if (categoryResponse.success) {
                        categoryResponse.data?.content?.let { categories ->
                            _categories.value = categories
                        }
                    } else {
                        _error.value = categoryResponse.message ?: "Không thể tải danh mục"
                    }
                } else {
                    _error.value = "Không thể tải danh mục từ server"
                }
            } catch (e: HttpException) {
                _error.value = "Lỗi HTTP: ${e.message()}"
                Timber.e(e, "Lỗi khi tải danh mục")
            } catch (e: IOException) {
                _error.value = "Lỗi kết nối: ${e.message}"
                Timber.e(e, "Lỗi kết nối khi tải danh mục")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Load locations
    fun loadLocations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = apiService.getLocations()
                if (response.isSuccessful && response.body() != null) {
                    val locationResponse = response.body()!!
                    if (locationResponse.success) {
                        locationResponse.data?.content?.let { locations ->
                            _locations.value = locations
                        }
                    } else {
                        _error.value = locationResponse.message ?: "Không thể tải địa điểm"
                    }
                } else {
                    _error.value = "Không thể tải địa điểm từ server"
                }
            } catch (e: HttpException) {
                _error.value = "Lỗi HTTP: ${e.message()}"
                Timber.e(e, "Lỗi khi tải địa điểm")
            } catch (e: IOException) {
                _error.value = "Lỗi kết nối: ${e.message}"
                Timber.e(e, "Lỗi kết nối khi tải địa điểm")
            } finally {
                _isLoading.value = false
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
            try {
                _isLoading.value = true
                _error.value = null
                
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
                
                val response = apiService.createEvent(eventDto)
                
                if (response.isSuccessful && response.body() != null) {
                    val eventResponse = response.body()!!
                    if (eventResponse.success) {
                        eventResponse.data?.id?.let { eventId ->
                            // Tải ảnh lên nếu có
                            uploadEventImages(eventId, featuredImageUri, bannerImageUri)
                            _uiState.value = UiState.Success(eventId)
                        }
                    } else {
                        _uiState.value = UiState.Error(eventResponse.message ?: "Không thể tạo sự kiện")
                        _error.value = eventResponse.message
                    }
                } else {
                    val errorMessage = parseErrorBody(response.errorBody())
                    _uiState.value = UiState.Error(errorMessage)
                    _error.value = errorMessage
                }
            } catch (e: HttpException) {
                val errorMessage = parseErrorBody(e.response()?.errorBody())
                _uiState.value = UiState.Error(errorMessage)
                _error.value = errorMessage
                Timber.e(e, "Lỗi HTTP khi tạo sự kiện")
            } catch (e: IllegalStateException) {
                // Xử lý lỗi "closed" từ OkHttp
                if (e.message?.contains("closed") == true) {
                    _uiState.value = UiState.Error("Lỗi kết nối với server. Vui lòng thử lại sau.")
                    _error.value = "Lỗi kết nối với server. Vui lòng thử lại sau."
                    Timber.e(e, "Lỗi IllegalStateException: closed khi tạo sự kiện")
                } else {
                    _uiState.value = UiState.Error("Lỗi không xác định: ${e.message}")
                    _error.value = "Lỗi không xác định: ${e.message}"
                    Timber.e(e, "Lỗi IllegalStateException khi tạo sự kiện")
                }
            } catch (e: IOException) {
                _uiState.value = UiState.Error("Lỗi kết nối: ${e.message}")
                _error.value = "Lỗi kết nối: ${e.message}"
                Timber.e(e, "Lỗi kết nối khi tạo sự kiện")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Lỗi không xác định: ${e.message}")
                _error.value = "Lỗi không xác định: ${e.message}"
                Timber.e(e, "Lỗi không xác định khi tạo sự kiện")
            } finally {
                _isLoading.value = false
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
            
            val response = apiService.uploadEventImage(eventId, imagePart, isPrimary)
            
            if (!response.isSuccessful || response.body() == null || !response.body()!!.success) {
                Timber.e("Lỗi khi tải ảnh: ${response.errorBody()?.string()}")
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