package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.event.EventImageDto
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.domain.repository.EventImageRepository
import com.nicha.eventticketing.ui.components.UploadState
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

    // State cho danh sách hình ảnh
    private val _eventImagesState = MutableStateFlow<ResourceState<List<EventImageDto>>>(ResourceState.Initial)
    val eventImagesState: StateFlow<ResourceState<List<EventImageDto>>> = _eventImagesState.asStateFlow()

    // State cho upload hình ảnh
    private val _uploadImageState = MutableStateFlow<ResourceState<EventImageDto>>(ResourceState.Initial)
    val uploadImageState: StateFlow<ResourceState<EventImageDto>> = _uploadImageState.asStateFlow()

    // State cho xóa hình ảnh
    private val _deleteImageState = MutableStateFlow<ResourceState<String>>(ResourceState.Initial)
    val deleteImageState: StateFlow<ResourceState<String>> = _deleteImageState.asStateFlow()

    // State cho set primary image
    private val _setPrimaryImageState = MutableStateFlow<ResourceState<EventImageDto>>(ResourceState.Initial)
    val setPrimaryImageState: StateFlow<ResourceState<EventImageDto>> = _setPrimaryImageState.asStateFlow()

    // State cho danh sách images (để update UI)
    private val _images = MutableStateFlow<ResourceState<List<EventImageDto>>>(ResourceState.Initial)
    val images: StateFlow<ResourceState<List<EventImageDto>>> = _images.asStateFlow()

    // Upload state cho progress tracking và animations
    private val _uploadState = MutableStateFlow(UploadState())
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    /**
     * Lấy danh sách hình ảnh của sự kiện
     */
    fun getEventImages(eventId: String) {
        _eventImagesState.value = ResourceState.Loading
        
        viewModelScope.launch {
            eventImageRepository.getEventImages(eventId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val data = result.data ?: emptyList()
                        _eventImagesState.value = ResourceState.Success(data)
                        _images.value = ResourceState.Success(data)
                    }
                    is Resource.Error -> {
                        _eventImagesState.value = ResourceState.Error(result.message ?: "Lỗi khi tải danh sách hình ảnh")
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
        
        _uploadState.value = UploadState(
            isUploading = true,
            progress = 0f,
            fileName = imageFile.name
        )
        
        viewModelScope.launch {
            try {
                for (i in 1..10) {
                    kotlinx.coroutines.delay(100)
                    val progress = i / 10f * 0.8f 
                    _uploadState.value = UploadState(
                        isUploading = true,
                        progress = progress,
                        fileName = imageFile.name
                    )
                }
                
                // Tạo multipart request
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
                
                eventImageRepository.uploadEventImage(eventId, imagePart, isPrimary).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val uploadedImage = result.data
                            if (uploadedImage != null) {
                                _uploadImageState.value = ResourceState.Success(uploadedImage)
                                
                                _uploadState.value = UploadState(
                                    isUploading = false,
                                    progress = 1f,
                                    isCompleted = true,
                                    fileName = imageFile.name
                                )
                                
                                kotlinx.coroutines.delay(2000)
                                _uploadState.value = UploadState()
                                
                            } else {
                                _uploadImageState.value = ResourceState.Error("Không thể tải lên hình ảnh")
                                _uploadState.value = UploadState(error = "Không thể tải lên hình ảnh")
                            }
                        }
                        is Resource.Error -> {
                            _uploadImageState.value = ResourceState.Error(result.message ?: "Không thể tải lên hình ảnh")
                            _uploadState.value = UploadState(error = result.message ?: "Không thể tải lên hình ảnh")
                        }
                        is Resource.Loading -> {
                        }
                    }
                }
            } catch (e: Exception) {
                _uploadImageState.value = ResourceState.Error("Lỗi khi tải lên: ${e.message}")
                _uploadState.value = UploadState(error = "Lỗi khi tải lên: ${e.message}")
            }
        }
    }

    fun retryUpload(eventId: String, imageFile: File, isPrimary: Boolean = false) {
        resetUploadState()
        uploadEventImage(eventId, imageFile, isPrimary)
    }

    fun resetUploadState() {
        _uploadState.value = UploadState()
        _uploadImageState.value = ResourceState.Initial
    }

    fun deleteEventImage(eventId: String, imageId: String) {
        _deleteImageState.value = ResourceState.Loading
        
        viewModelScope.launch {
            eventImageRepository.deleteEventImage(eventId, imageId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _deleteImageState.value = ResourceState.Success("Đã xóa hình ảnh thành công")
                        
                        val currentImages = _eventImagesState.value
                        if (currentImages is ResourceState.Success) {
                            val updatedImages = currentImages.data.filter { it.id != imageId }
                            _eventImagesState.value = ResourceState.Success(updatedImages)
                            _images.value = ResourceState.Success(updatedImages)
                        }
                    }
                    is Resource.Error -> {
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
    fun setImageAsPrimary(eventId: String, imageId: String) {
        _setPrimaryImageState.value = ResourceState.Loading
        
        viewModelScope.launch {
            val currentImages = _eventImagesState.value
            if (currentImages is ResourceState.Success) {
                val updatedImages = currentImages.data.map { image ->
                    if (image.id == imageId) {
                        image.copy(isPrimary = true)
                    } else {
                        image.copy(isPrimary = false)
                    }
                }
                _eventImagesState.value = ResourceState.Success(updatedImages)
                _images.value = ResourceState.Success(updatedImages)
                _setPrimaryImageState.value = ResourceState.Success(updatedImages.find { it.id == imageId }!!)
            } 
        }
    }

    fun resetUploadImageState() {
        _uploadImageState.value = ResourceState.Initial
    }
    
    fun resetDeleteImageState() {
        _deleteImageState.value = ResourceState.Initial
    }
    
    fun resetSetPrimaryImageState() {
        _setPrimaryImageState.value = ResourceState.Initial
    }
}
