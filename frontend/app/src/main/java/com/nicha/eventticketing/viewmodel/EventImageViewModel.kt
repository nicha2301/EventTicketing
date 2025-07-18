package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.event.EventImageDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import retrofit2.HttpException

@HiltViewModel
class EventImageViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    
    // State cho danh sách hình ảnh sự kiện
    private val _eventImagesState = MutableStateFlow<ResourceState<List<EventImageDto>>>(ResourceState.Initial)
    val eventImagesState: StateFlow<ResourceState<List<EventImageDto>>> = _eventImagesState.asStateFlow()
    
    // State cho việc tải lên hình ảnh
    private val _uploadImageState = MutableStateFlow<ResourceState<EventImageDto>>(ResourceState.Initial)
    val uploadImageState: StateFlow<ResourceState<EventImageDto>> = _uploadImageState.asStateFlow()
    
    // State cho việc xóa hình ảnh
    private val _deleteImageState = MutableStateFlow<ResourceState<Boolean>>(ResourceState.Initial)
    val deleteImageState: StateFlow<ResourceState<Boolean>> = _deleteImageState.asStateFlow()
    
    // State cho việc đặt ảnh làm ảnh chính
    private val _setPrimaryImageState = MutableStateFlow<ResourceState<EventImageDto>>(ResourceState.Initial)
    val setPrimaryImageState: StateFlow<ResourceState<EventImageDto>> = _setPrimaryImageState.asStateFlow()
    
    // State cho tỷ lệ tải lên
    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()
    
    /**
     * Lấy danh sách hình ảnh của sự kiện
     */
    fun getEventImages(eventId: String) {
        _eventImagesState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy danh sách hình ảnh cho sự kiện: $eventId")
                val response = apiService.getEventImages(eventId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val images = response.body()?.data
                    if (images != null) {
                        _eventImagesState.value = ResourceState.Success(images)
                        Timber.d("Lấy danh sách hình ảnh thành công: ${images.size} hình ảnh")
                    } else {
                        Timber.e("Không thể lấy danh sách hình ảnh từ response")
                        _eventImagesState.value = ResourceState.Error("Không thể lấy danh sách hình ảnh")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy danh sách hình ảnh"
                    Timber.e("Lấy danh sách hình ảnh thất bại: $errorMessage")
                    _eventImagesState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy danh sách hình ảnh", _eventImagesState)
            }
        }
    }
    
    /**
     * Tải lên hình ảnh cho sự kiện
     */
    fun uploadEventImage(eventId: String, imageFile: File, isPrimary: Boolean = false) {
        _uploadImageState.value = ResourceState.Loading
        _uploadProgress.value = 0f
        
        viewModelScope.launch {
            try {
                Timber.d("Đang tải lên hình ảnh cho sự kiện: $eventId")
                
                // Tạo multipart request
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
                
                val response = apiService.uploadEventImage(
                    eventId = eventId,
                    image = imagePart,
                    isPrimary = isPrimary
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val uploadedImage = response.body()?.data
                    if (uploadedImage != null) {
                        _uploadImageState.value = ResourceState.Success(uploadedImage)
                        _uploadProgress.value = 1f
                        Timber.d("Tải lên hình ảnh thành công: ${uploadedImage.id}")
                    } else {
                        Timber.e("Không thể lấy thông tin hình ảnh đã tải lên từ response")
                        _uploadImageState.value = ResourceState.Error("Không thể tải lên hình ảnh")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể tải lên hình ảnh"
                    Timber.e("Tải lên hình ảnh thất bại: $errorMessage")
                    _uploadImageState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "tải lên hình ảnh", _uploadImageState)
            }
        }
    }
    
    /**
     * Xóa hình ảnh sự kiện
     */
    fun deleteEventImage(eventId: String, imageId: String) {
        _deleteImageState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang xóa hình ảnh: $imageId từ sự kiện: $eventId")
                val response = apiService.deleteEventImage(eventId, imageId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val result = response.body()?.data
                    if (result != null && result) {
                        _deleteImageState.value = ResourceState.Success(true)
                        Timber.d("Xóa hình ảnh thành công")
                        
                        // Cập nhật lại danh sách hình ảnh
                        getEventImages(eventId)
                    } else {
                        Timber.e("Không thể xóa hình ảnh")
                        _deleteImageState.value = ResourceState.Error("Không thể xóa hình ảnh")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể xóa hình ảnh"
                    Timber.e("Xóa hình ảnh thất bại: $errorMessage")
                    _deleteImageState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "xóa hình ảnh", _deleteImageState)
            }
        }
    }
    
    /**
     * Đặt hình ảnh làm ảnh chính
     */
    fun setAsPrimaryImage(eventId: String, imageId: String) {
        _setPrimaryImageState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang đặt hình ảnh: $imageId làm ảnh chính cho sự kiện: $eventId")
                
                val response = apiService.setImageAsPrimary(eventId, imageId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedImage = response.body()?.data
                    if (updatedImage != null) {
                        _setPrimaryImageState.value = ResourceState.Success(updatedImage)
                        Timber.d("Đặt hình ảnh làm ảnh chính thành công: ${updatedImage.id}")
                        
                        // Cập nhật lại danh sách hình ảnh
                        getEventImages(eventId)
                    } else {
                        Timber.e("Không thể lấy thông tin hình ảnh đã cập nhật từ response")
                        _setPrimaryImageState.value = ResourceState.Error("Không thể đặt hình ảnh làm ảnh chính")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể đặt hình ảnh làm ảnh chính"
                    Timber.e("Đặt hình ảnh làm ảnh chính thất bại: $errorMessage")
                    _setPrimaryImageState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "đặt hình ảnh làm ảnh chính", _setPrimaryImageState)
            }
        }
    }
    
    /**
     * Reset states
     */
    fun resetUploadImageState() {
        _uploadImageState.value = ResourceState.Initial
        _uploadProgress.value = 0f
    }
    
    fun resetDeleteImageState() {
        _deleteImageState.value = ResourceState.Initial
    }
    
    fun resetSetPrimaryImageState() {
        _setPrimaryImageState.value = ResourceState.Initial
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