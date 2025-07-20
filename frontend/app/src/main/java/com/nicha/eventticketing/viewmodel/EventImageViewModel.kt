package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.event.EventImageDto
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.domain.repository.EventImageRepository
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
import javax.inject.Inject

@HiltViewModel
class EventImageViewModel @Inject constructor(
    private val eventImageRepository: EventImageRepository
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
            eventImageRepository.getEventImages(eventId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val images = result.data
                        if (images != null) {
                            _eventImagesState.value = ResourceState.Success(images)
                            Timber.d("Lấy danh sách hình ảnh thành công: ${images.size} hình ảnh")
                        } else {
                            Timber.e("Không tìm thấy hình ảnh")
                            _eventImagesState.value = ResourceState.Error("Không tìm thấy hình ảnh")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy danh sách hình ảnh thất bại: ${result.message}")
                        _eventImagesState.value = ResourceState.Error(result.message ?: "Không thể lấy danh sách hình ảnh")
                    }
                    is Resource.Loading -> {
                        _eventImagesState.value = ResourceState.Loading
                    }
                }
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
            // Tạo multipart request
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            
            eventImageRepository.uploadEventImage(eventId, imagePart, isPrimary).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val uploadedImage = result.data
                        if (uploadedImage != null) {
                            _uploadImageState.value = ResourceState.Success(uploadedImage)
                            _uploadProgress.value = 1f
                            Timber.d("Tải lên hình ảnh thành công: ${uploadedImage.id}")
                        } else {
                            Timber.e("Không thể tải lên hình ảnh")
                            _uploadImageState.value = ResourceState.Error("Không thể tải lên hình ảnh")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Tải lên hình ảnh thất bại: ${result.message}")
                        _uploadImageState.value = ResourceState.Error(result.message ?: "Không thể tải lên hình ảnh")
                    }
                    is Resource.Loading -> {
                        _uploadImageState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Xóa hình ảnh sự kiện
     */
    fun deleteEventImage(eventId: String, imageId: String) {
        _deleteImageState.value = ResourceState.Loading
        
        viewModelScope.launch {
            // Sử dụng hàm phụ trợ từ EventImageRepositoryImpl vì interface chỉ nhận imageId
            (eventImageRepository as? com.nicha.eventticketing.data.repository.EventImageRepositoryImpl)
                ?.deleteEventImageWithEventId(eventId, imageId)
                ?.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val success = result.data
                            if (success == true) {
                                _deleteImageState.value = ResourceState.Success(true)
                                Timber.d("Xóa hình ảnh thành công")
                                
                                // Cập nhật lại danh sách hình ảnh
                                getEventImages(eventId)
                            } else {
                                Timber.e("Không thể xóa hình ảnh")
                                _deleteImageState.value = ResourceState.Error("Không thể xóa hình ảnh")
                            }
                        }
                        is Resource.Error -> {
                            Timber.e("Xóa hình ảnh thất bại: ${result.message}")
                            _deleteImageState.value = ResourceState.Error(result.message ?: "Không thể xóa hình ảnh")
                        }
                        is Resource.Loading -> {
                            _deleteImageState.value = ResourceState.Loading
                        }
                    }
                }
        }
    }
    
    /**
     * Đặt hình ảnh làm ảnh chính
     */
    fun setAsPrimaryImage(eventId: String, imageId: String) {
        _setPrimaryImageState.value = ResourceState.Loading
        
        viewModelScope.launch {
            // Sử dụng hàm phụ trợ từ EventImageRepositoryImpl vì interface chỉ nhận imageId
            (eventImageRepository as? com.nicha.eventticketing.data.repository.EventImageRepositoryImpl)
                ?.setFeaturedImageWithEventId(eventId, imageId)
                ?.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val updatedImage = result.data
                            if (updatedImage != null) {
                                _setPrimaryImageState.value = ResourceState.Success(updatedImage)
                                Timber.d("Đặt hình ảnh làm ảnh chính thành công: ${updatedImage.id}")
                                
                                // Cập nhật lại danh sách hình ảnh
                                getEventImages(eventId)
                            } else {
                                Timber.e("Không thể đặt hình ảnh làm ảnh chính")
                                _setPrimaryImageState.value = ResourceState.Error("Không thể đặt hình ảnh làm ảnh chính")
                            }
                        }
                        is Resource.Error -> {
                            Timber.e("Đặt hình ảnh làm ảnh chính thất bại: ${result.message}")
                            _setPrimaryImageState.value = ResourceState.Error(result.message ?: "Không thể đặt hình ảnh làm ảnh chính")
                        }
                        is Resource.Loading -> {
                            _setPrimaryImageState.value = ResourceState.Loading
                        }
                    }
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
} 