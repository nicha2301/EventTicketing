package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.event.EventImageDto
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

/**
 * Repository xử lý các chức năng liên quan đến hình ảnh sự kiện
 */
interface EventImageRepository {
    /**
     * Lấy danh sách hình ảnh của sự kiện
     * @param eventId ID của sự kiện
     * @return Flow<Resource<List<EventImageDto>>> Flow chứa danh sách hình ảnh
     */
    fun getEventImages(eventId: String): Flow<Resource<List<EventImageDto>>>
    
    /**
     * Lấy thông tin chi tiết của một hình ảnh
     * @param imageId ID của hình ảnh
     * @return Flow<Resource<EventImageDto>> Flow chứa thông tin hình ảnh
     */
    fun getEventImage(imageId: String): Flow<Resource<EventImageDto>>
    
    /**
     * Tải lên hình ảnh mới cho sự kiện
     * @param eventId ID của sự kiện
     * @param image File hình ảnh cần tải lên
     * @param isFeatured Có phải là hình ảnh nổi bật không
     * @return Flow<Resource<EventImageDto>> Flow chứa thông tin hình ảnh đã tải lên
     */
    fun uploadEventImage(eventId: String, image: MultipartBody.Part, isFeatured: Boolean): Flow<Resource<EventImageDto>>
    
    /**
     * Cập nhật thông tin hình ảnh
     * @param imageId ID của hình ảnh
     * @param isFeatured Có phải là hình ảnh nổi bật không
     * @return Flow<Resource<EventImageDto>> Flow chứa thông tin hình ảnh đã cập nhật
     */
    fun updateEventImage(imageId: String, isFeatured: Boolean): Flow<Resource<EventImageDto>>
    
    /**
     * Xóa hình ảnh
     * @param imageId ID của hình ảnh
     * @return Flow<Resource<Boolean>> Flow chứa kết quả xóa
     */
    fun deleteEventImage(imageId: String): Flow<Resource<Boolean>>
    
    /**
     * Đặt hình ảnh làm hình ảnh nổi bật
     * @param imageId ID của hình ảnh
     * @return Flow<Resource<EventImageDto>> Flow chứa thông tin hình ảnh đã cập nhật
     */
    fun setFeaturedImage(imageId: String): Flow<Resource<EventImageDto>>
    
    /**
     * Lấy hình ảnh nổi bật của sự kiện
     * @param eventId ID của sự kiện
     * @return Flow<Resource<EventImageDto>> Flow chứa thông tin hình ảnh nổi bật
     */
    fun getFeaturedImage(eventId: String): Flow<Resource<EventImageDto>>
} 