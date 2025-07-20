package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.NotificationRepository
import com.nicha.eventticketing.util.NetworkUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : NotificationRepository {
    
    override fun getNotifications(page: Int, size: Int): Flow<Resource<PageDto<NotificationDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getNotifications(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                val notifications = response.body()?.data
                if (notifications != null) {
                    emit(Resource.Success(notifications))
                    Timber.d("Lấy danh sách thông báo thành công: ${notifications.content?.size ?: 0} thông báo")
                } else {
                    emit(Resource.Error("Không tìm thấy thông báo"))
                    Timber.e("Không tìm thấy thông báo")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy danh sách thông báo"))
                Timber.e("Lấy danh sách thông báo thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách thông báo")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun markNotificationAsRead(notificationId: String): Flow<Resource<NotificationDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.markNotificationAsRead(notificationId)
            if (response.isSuccessful && response.body()?.success == true) {
                val notification = response.body()?.data
                if (notification != null) {
                    emit(Resource.Success(notification))
                    Timber.d("Đánh dấu thông báo đã đọc thành công: ${notification.id}")
                } else {
                    emit(Resource.Error("Không tìm thấy thông báo"))
                    Timber.e("Không tìm thấy thông báo")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể đánh dấu thông báo đã đọc"))
                Timber.e("Đánh dấu thông báo đã đọc thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đánh dấu thông báo đã đọc: $notificationId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun updateNotificationPreferences(preferences: Map<String, Any>): Flow<Resource<Map<String, Any>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.updateNotificationPreferences(preferences)
            if (response.isSuccessful && response.body()?.success == true) {
                val updatedPreferences = response.body()?.data
                if (updatedPreferences != null) {
                    emit(Resource.Success(updatedPreferences))
                    Timber.d("Cập nhật tùy chọn thông báo thành công")
                } else {
                    emit(Resource.Error("Không thể cập nhật tùy chọn thông báo"))
                    Timber.e("Không thể cập nhật tùy chọn thông báo")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể cập nhật tùy chọn thông báo"))
                Timber.e("Cập nhật tùy chọn thông báo thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật tùy chọn thông báo")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun markAllNotificationsAsRead(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            // API hiện tại không có endpoint riêng để đánh dấu tất cả thông báo đã đọc
            // Tạm thời chưa triển khai
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
            Timber.e("API markAllNotificationsAsRead chưa được hỗ trợ")
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đánh dấu tất cả thông báo đã đọc")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
} 