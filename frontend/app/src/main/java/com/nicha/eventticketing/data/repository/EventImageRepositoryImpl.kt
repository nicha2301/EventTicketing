package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.event.EventImageDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.EventImageRepository
import com.nicha.eventticketing.util.NetworkUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventImageRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : EventImageRepository {
    
    override fun getEventImages(eventId: String): Flow<Resource<List<EventImageDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getEventImages(eventId)
            if (response.isSuccessful && response.body()?.success == true) {
                val images = response.body()?.data
                if (images != null) {
                    emit(Resource.Success(images))
                } else {
                    emit(Resource.Error("Không tìm thấy hình ảnh"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách hình ảnh"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách hình ảnh của sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    //Chưa triển khai
    override fun getEventImage(imageId: String): Flow<Resource<EventImageDto>> = flow {
        emit(Resource.Loading())
        try {

            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy thông tin hình ảnh: $imageId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun uploadEventImage(
        eventId: String, 
        image: MultipartBody.Part, 
        isFeatured: Boolean
    ): Flow<Resource<EventImageDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.uploadEventImage(eventId, image, isFeatured)
            if (response.isSuccessful && response.body()?.success == true) {
                val uploadedImage = response.body()?.data
                if (uploadedImage != null) {
                    emit(Resource.Success(uploadedImage))
                } else {
                    emit(Resource.Error("Không thể tải lên hình ảnh"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Tải lên hình ảnh thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi tải lên hình ảnh cho sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    //Chưa triển khai
    override fun updateEventImage(
        imageId: String, 
        isFeatured: Boolean
    ): Flow<Resource<EventImageDto>> = flow {
        emit(Resource.Loading())
        try {
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật thông tin hình ảnh: $imageId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    //Chưa triển khai
    override fun deleteEventImage(imageId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            emit(Resource.Error("Cần cung cấp cả eventId và imageId để xóa hình ảnh"))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xóa hình ảnh: $imageId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    fun deleteEventImageWithEventId(eventId: String, imageId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteEventImage(eventId, imageId)
            if (response.isSuccessful && response.body()?.success == true) {
                val result = response.body()?.data
                if (result != null) {
                    emit(Resource.Success(result))
                } else {
                    emit(Resource.Error("Không thể xóa hình ảnh"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Xóa hình ảnh thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xóa hình ảnh: $imageId của sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    //Chưa triển khai
    override fun setFeaturedImage(imageId: String): Flow<Resource<EventImageDto>> = flow {
        emit(Resource.Loading())
        try {
            emit(Resource.Error("Cần cung cấp cả eventId và imageId để đặt hình ảnh nổi bật"))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đặt hình ảnh nổi bật: $imageId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    fun setFeaturedImageWithEventId(eventId: String, imageId: String): Flow<Resource<EventImageDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.setImageAsPrimary(eventId, imageId)
            if (response.isSuccessful && response.body()?.success == true) {
                val image = response.body()?.data
                if (image != null) {
                    emit(Resource.Success(image))
                } else {
                    emit(Resource.Error("Không thể đặt hình ảnh nổi bật"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Đặt hình ảnh nổi bật thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đặt hình ảnh nổi bật: $imageId của sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getFeaturedImage(eventId: String): Flow<Resource<EventImageDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getEventImages(eventId)
            if (response.isSuccessful && response.body()?.success == true) {
                val images = response.body()?.data
                if (images != null && images.isNotEmpty()) {
                    val featuredImage = images.find { it.isPrimary } ?: images.first()
                    emit(Resource.Success(featuredImage))
                } else {
                    emit(Resource.Error("Không tìm thấy hình ảnh nổi bật"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Không thể lấy hình ảnh nổi bật"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy hình ảnh nổi bật của sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    /**
     * Lưu thông tin hình ảnh Cloudinary vào database
     */
    fun saveCloudinaryImage(
        eventId: String,
        cloudinaryUrl: String,
        publicId: String,
        width: Int,
        height: Int,
        isPrimary: Boolean
    ): Flow<Resource<EventImageDto>> = flow {
        emit(Resource.Loading())
        try {
            val cloudinaryRequest = com.nicha.eventticketing.data.remote.dto.request.CloudinaryImageRequest(
                publicId = publicId,
                secureUrl = cloudinaryUrl,
                width = width,
                height = height,
                isPrimary = isPrimary
            )
            
            val response = apiService.saveCloudinaryImage(eventId, cloudinaryRequest)
            if (response.isSuccessful && response.body()?.success == true) {
                val savedImage = response.body()?.data
                if (savedImage != null) {
                    emit(Resource.Success(savedImage))
                } else {
                    emit(Resource.Error("Không thể lưu thông tin Cloudinary"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Lưu thông tin Cloudinary thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lưu thông tin Cloudinary cho sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
} 