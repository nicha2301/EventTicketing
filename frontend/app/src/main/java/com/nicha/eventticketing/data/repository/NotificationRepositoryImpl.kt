package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.notification.DeleteAllResultDto
import com.nicha.eventticketing.data.remote.dto.notification.DeviceTokenDto
import com.nicha.eventticketing.data.remote.dto.notification.DeviceTokenRequestDto
import com.nicha.eventticketing.data.remote.dto.notification.MarkAllReadResultDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationPreferencesDto
import com.nicha.eventticketing.data.remote.dto.notification.TopicSubscriptionDto
import com.nicha.eventticketing.data.remote.dto.notification.UnreadCountDto
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
            if (response.isSuccessful) {
                val notifications = response.body()
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
    
    override fun getUnreadNotifications(page: Int, size: Int): Flow<Resource<PageDto<NotificationDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getUnreadNotifications(page, size)
            if (response.isSuccessful) {
                val notifications = response.body()
                if (notifications != null) {
                    emit(Resource.Success(notifications))
                    Timber.d("Lấy danh sách thông báo chưa đọc thành công: ${notifications.content?.size ?: 0} thông báo")
                } else {
                    emit(Resource.Error("Không tìm thấy thông báo chưa đọc"))
                    Timber.e("Không tìm thấy thông báo chưa đọc")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy danh sách thông báo chưa đọc"))
                Timber.e("Lấy danh sách thông báo chưa đọc thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách thông báo chưa đọc")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getNotificationsByType(type: String, page: Int, size: Int): Flow<Resource<PageDto<NotificationDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getNotificationsByType(type, page, size)
            if (response.isSuccessful) {
                val notifications = response.body()
                if (notifications != null) {
                    emit(Resource.Success(notifications))
                    Timber.d("Lấy danh sách thông báo theo loại $type thành công: ${notifications.content?.size ?: 0} thông báo")
                } else {
                    emit(Resource.Error("Không tìm thấy thông báo loại $type"))
                    Timber.e("Không tìm thấy thông báo loại $type")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy danh sách thông báo loại $type"))
                Timber.e("Lấy danh sách thông báo loại $type thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách thông báo loại $type")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getUnreadNotificationCount(): Flow<Resource<UnreadCountDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getUnreadNotificationCount()
            if (response.isSuccessful) {
                val count = response.body()
                if (count != null) {
                    emit(Resource.Success(count))
                    Timber.d("Lấy số lượng thông báo chưa đọc thành công: ${count.unreadCount}")
                } else {
                    emit(Resource.Error("Không thể lấy số lượng thông báo chưa đọc"))
                    Timber.e("Không thể lấy số lượng thông báo chưa đọc")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy số lượng thông báo chưa đọc"))
                Timber.e("Lấy số lượng thông báo chưa đọc thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy số lượng thông báo chưa đọc")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun markNotificationAsRead(notificationId: String): Flow<Resource<NotificationDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.markNotificationAsRead(notificationId)
            if (response.isSuccessful) {
                val notification = response.body()
                if (notification != null) {
                    emit(Resource.Success(notification))
                    Timber.d("Đánh dấu thông báo đã đọc thành công: ${notification.id}")
                } else {
                    emit(Resource.Error("Không thể đánh dấu thông báo đã đọc"))
                    Timber.e("Không thể đánh dấu thông báo đã đọc")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể đánh dấu thông báo đã đọc"))
                Timber.e("Đánh dấu thông báo đã đọc thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đánh dấu thông báo đã đọc")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun markAllNotificationsAsRead(): Flow<Resource<MarkAllReadResultDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.markAllNotificationsAsRead()
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    emit(Resource.Success(result))
                    Timber.d("Đánh dấu tất cả thông báo đã đọc thành công: ${result.markedCount} thông báo")
                } else {
                    emit(Resource.Error("Không thể đánh dấu tất cả thông báo đã đọc"))
                    Timber.e("Không thể đánh dấu tất cả thông báo đã đọc")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể đánh dấu tất cả thông báo đã đọc"))
                Timber.e("Đánh dấu tất cả thông báo đã đọc thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đánh dấu tất cả thông báo đã đọc")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun deleteNotification(notificationId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteNotification(notificationId)
            if (response.isSuccessful) {
                // API trả về Void, chỉ cần kiểm tra response.isSuccessful
                emit(Resource.Success(true))
                Timber.d("Xóa thông báo thành công: $notificationId")
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể xóa thông báo"))
                Timber.e("Xóa thông báo thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xóa thông báo")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun deleteAllNotifications(): Flow<Resource<DeleteAllResultDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteAllNotifications()
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    emit(Resource.Success(result))
                    Timber.d("Xóa tất cả thông báo thành công: ${result.deletedCount} thông báo")
                } else {
                    emit(Resource.Error("Không thể xóa tất cả thông báo"))
                    Timber.e("Không thể xóa tất cả thông báo")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể xóa tất cả thông báo"))
                Timber.e("Xóa tất cả thông báo thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xóa tất cả thông báo")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getNotificationPreferences(): Flow<Resource<NotificationPreferencesDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getNotificationPreferences()
            if (response.isSuccessful) {
                val preferences = response.body()
                if (preferences != null) {
                    emit(Resource.Success(preferences))
                    Timber.d("Lấy cài đặt thông báo thành công")
                } else {
                    emit(Resource.Error("Không thể lấy cài đặt thông báo"))
                    Timber.e("Không thể lấy cài đặt thông báo")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy cài đặt thông báo"))
                Timber.e("Lấy cài đặt thông báo thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy cài đặt thông báo")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun updateNotificationPreferences(preferences: NotificationPreferencesDto): Flow<Resource<NotificationPreferencesDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.updateNotificationPreferences(preferences)
            if (response.isSuccessful) {
                val updatedPreferences = response.body()
                if (updatedPreferences != null) {
                    emit(Resource.Success(updatedPreferences))
                    Timber.d("Cập nhật cài đặt thông báo thành công")
                } else {
                    emit(Resource.Error("Không thể cập nhật cài đặt thông báo"))
                    Timber.e("Không thể cập nhật cài đặt thông báo")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể cập nhật cài đặt thông báo"))
                Timber.e("Cập nhật cài đặt thông báo thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật cài đặt thông báo")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun registerDeviceToken(token: String, deviceType: String): Flow<Resource<DeviceTokenDto>> = flow {
        emit(Resource.Loading())
        try {
            val request = DeviceTokenRequestDto(token, deviceType)
            val response = apiService.registerDeviceToken(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val deviceToken = response.body()?.data
                if (deviceToken != null) {
                    emit(Resource.Success(deviceToken))
                    Timber.d("Đăng ký token thiết bị thành công: ${deviceToken.id}")
                } else {
                    emit(Resource.Error("Không thể đăng ký token thiết bị"))
                    Timber.e("Không thể đăng ký token thiết bị")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể đăng ký token thiết bị"))
                Timber.e("Đăng ký token thiết bị thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đăng ký token thiết bị")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getDeviceTokens(): Flow<Resource<List<DeviceTokenDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getDeviceTokens()
            if (response.isSuccessful && response.body()?.success == true) {
                val tokens = response.body()?.data
                if (tokens != null) {
                    emit(Resource.Success(tokens))
                    Timber.d("Lấy danh sách token thiết bị thành công: ${tokens.size} token")
                } else {
                    emit(Resource.Error("Không thể lấy danh sách token thiết bị"))
                    Timber.e("Không thể lấy danh sách token thiết bị")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy danh sách token thiết bị"))
                Timber.e("Lấy danh sách token thiết bị thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách token thiết bị")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun deleteDeviceToken(tokenId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteDeviceToken(tokenId)
            if (response.isSuccessful && response.body()?.success == true) {
                val result = response.body()?.data
                if (result == true) {
                    emit(Resource.Success(true))
                    Timber.d("Xóa token thiết bị thành công: $tokenId")
                } else {
                    emit(Resource.Error("Không thể xóa token thiết bị"))
                    Timber.e("Không thể xóa token thiết bị")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể xóa token thiết bị"))
                Timber.e("Xóa token thiết bị thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xóa token thiết bị")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun deleteAllDeviceTokens(): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteAllDeviceTokens()
            if (response.isSuccessful && response.body()?.success == true) {
                val count = response.body()?.data
                if (count != null) {
                    emit(Resource.Success(count))
                    Timber.d("Xóa tất cả token thiết bị thành công: $count token")
                } else {
                    emit(Resource.Error("Không thể xóa tất cả token thiết bị"))
                    Timber.e("Không thể xóa tất cả token thiết bị")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể xóa tất cả token thiết bị"))
                Timber.e("Xóa tất cả token thiết bị thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xóa tất cả token thiết bị")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun subscribeToTopic(topic: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val request = TopicSubscriptionDto(topic)
            val response = apiService.subscribeToTopic(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val result = response.body()?.data
                if (result == true) {
                    emit(Resource.Success(true))
                    Timber.d("Đăng ký nhận thông báo theo chủ đề thành công: $topic")
                } else {
                    emit(Resource.Error("Không thể đăng ký nhận thông báo theo chủ đề"))
                    Timber.e("Không thể đăng ký nhận thông báo theo chủ đề")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể đăng ký nhận thông báo theo chủ đề"))
                Timber.e("Đăng ký nhận thông báo theo chủ đề thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đăng ký nhận thông báo theo chủ đề")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun unsubscribeFromTopic(topic: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val request = TopicSubscriptionDto(topic)
            val response = apiService.unsubscribeFromTopic(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val result = response.body()?.data
                if (result == true) {
                    emit(Resource.Success(true))
                    Timber.d("Hủy đăng ký nhận thông báo theo chủ đề thành công: $topic")
                } else {
                    emit(Resource.Error("Không thể hủy đăng ký nhận thông báo theo chủ đề"))
                    Timber.e("Không thể hủy đăng ký nhận thông báo theo chủ đề")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể hủy đăng ký nhận thông báo theo chủ đề"))
                Timber.e("Hủy đăng ký nhận thông báo theo chủ đề thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi hủy đăng ký nhận thông báo theo chủ đề")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
} 