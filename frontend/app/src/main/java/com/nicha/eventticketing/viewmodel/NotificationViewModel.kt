package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.notification.DeleteAllResultDto
import com.nicha.eventticketing.data.remote.dto.notification.MarkAllReadResultDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationPreferencesDto
import com.nicha.eventticketing.data.remote.dto.notification.UnreadCountDto
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    
    private val _notificationsState = MutableStateFlow<ResourceState<PageDto<NotificationDto>>>(ResourceState.Initial)
    val notificationsState: StateFlow<ResourceState<PageDto<NotificationDto>>> = _notificationsState.asStateFlow()
    
    private val _unreadNotificationsState = MutableStateFlow<ResourceState<PageDto<NotificationDto>>>(ResourceState.Initial)
    val unreadNotificationsState: StateFlow<ResourceState<PageDto<NotificationDto>>> = _unreadNotificationsState.asStateFlow()
    
    private val _unreadCountState = MutableStateFlow<ResourceState<UnreadCountDto>>(ResourceState.Initial)
    val unreadCountState: StateFlow<ResourceState<UnreadCountDto>> = _unreadCountState.asStateFlow()
    
    private val _preferencesState = MutableStateFlow<ResourceState<NotificationPreferencesDto>>(ResourceState.Initial)
    val preferencesState: StateFlow<ResourceState<NotificationPreferencesDto>> = _preferencesState.asStateFlow()
    
    private val _markAsReadState = MutableStateFlow<ResourceState<NotificationDto>>(ResourceState.Initial)
    val markAsReadState: StateFlow<ResourceState<NotificationDto>> = _markAsReadState.asStateFlow()
    
    private val _markAllAsReadState = MutableStateFlow<ResourceState<MarkAllReadResultDto>>(ResourceState.Initial)
    val markAllAsReadState: StateFlow<ResourceState<MarkAllReadResultDto>> = _markAllAsReadState.asStateFlow()
    
    private val _deleteNotificationState = MutableStateFlow<ResourceState<Boolean>>(ResourceState.Initial)
    val deleteNotificationState: StateFlow<ResourceState<Boolean>> = _deleteNotificationState.asStateFlow()
    
    private val _deleteAllNotificationsState = MutableStateFlow<ResourceState<DeleteAllResultDto>>(ResourceState.Initial)
    val deleteAllNotificationsState: StateFlow<ResourceState<DeleteAllResultDto>> = _deleteAllNotificationsState.asStateFlow()
    
    private val _fcmTokenRegistrationState = MutableStateFlow<ResourceState<Boolean>>(ResourceState.Initial)
    val fcmTokenRegistrationState: StateFlow<ResourceState<Boolean>> = _fcmTokenRegistrationState.asStateFlow()
    
    /**
     * Đăng ký FCM token với server
     * Gọi sau khi đăng nhập thành công
     */
    fun registerFcmToken() {
        viewModelScope.launch {
            _fcmTokenRegistrationState.value = ResourceState.Loading
            
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Timber.d("Đăng ký FCM token sau đăng nhập: $token")
                        
                        // Đăng ký token với server
                        viewModelScope.launch {
                            notificationRepository.registerDeviceToken(token, "ANDROID").collect { result ->
                                when (result) {
                                    is Resource.Success -> {
                                        _fcmTokenRegistrationState.value = ResourceState.Success(true)
                                        Timber.d("Đăng ký token sau đăng nhập thành công: ${result.data?.id}")
                                    }
                                    is Resource.Error -> {
                                        _fcmTokenRegistrationState.value = ResourceState.Error(
                                            result.message ?: "Đăng ký token thất bại"
                                        )
                                        Timber.e("Đăng ký token sau đăng nhập thất bại: ${result.message}")
                                    }
                                    is Resource.Loading -> {
                                        _fcmTokenRegistrationState.value = ResourceState.Loading
                                    }
                                }
                            }
                        }
                    } else {
                        _fcmTokenRegistrationState.value = ResourceState.Error("Không thể lấy FCM token: ${task.exception?.message}")
                        Timber.e(task.exception, "Không thể lấy FCM token")
                    }
                }
            } catch (e: Exception) {
                _fcmTokenRegistrationState.value = ResourceState.Error("Lỗi khi đăng ký FCM token: ${e.message}")
                Timber.e(e, "Lỗi khi đăng ký FCM token")
            }
        }
    }
    
    /**
     * Lấy danh sách thông báo
     */
    fun getNotifications(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            _notificationsState.value = ResourceState.Loading
            
            notificationRepository.getNotifications(page, size).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _notificationsState.value = ResourceState.Success(result.data!!)
                        Timber.d("Lấy danh sách thông báo thành công: ${result.data.content?.size ?: 0} thông báo")
                    }
                    is Resource.Error -> {
                        _notificationsState.value = ResourceState.Error(result.message ?: "Không thể lấy danh sách thông báo")
                        Timber.e("Lấy danh sách thông báo thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _notificationsState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Lấy danh sách thông báo chưa đọc
     */
    fun getUnreadNotifications(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            _unreadNotificationsState.value = ResourceState.Loading
            
            notificationRepository.getUnreadNotifications(page, size).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _unreadNotificationsState.value = ResourceState.Success(result.data!!)
                        Timber.d("Lấy danh sách thông báo chưa đọc thành công: ${result.data.content?.size ?: 0} thông báo")
                    }
                    is Resource.Error -> {
                        _unreadNotificationsState.value = ResourceState.Error(result.message ?: "Không thể lấy danh sách thông báo chưa đọc")
                        Timber.e("Lấy danh sách thông báo chưa đọc thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _unreadNotificationsState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Lấy số lượng thông báo chưa đọc
     */
    fun getUnreadNotificationCount() {
        viewModelScope.launch {
            _unreadCountState.value = ResourceState.Loading
            
            notificationRepository.getUnreadNotificationCount().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _unreadCountState.value = ResourceState.Success(result.data!!)
                        Timber.d("Lấy số lượng thông báo chưa đọc thành công: ${result.data.unreadCount}")
                    }
                    is Resource.Error -> {
                        _unreadCountState.value = ResourceState.Error(result.message ?: "Không thể lấy số lượng thông báo chưa đọc")
                        Timber.e("Lấy số lượng thông báo chưa đọc thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _unreadCountState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Đánh dấu thông báo đã đọc
     */
    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            _markAsReadState.value = ResourceState.Loading
            
            notificationRepository.markNotificationAsRead(notificationId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _markAsReadState.value = ResourceState.Success(result.data!!)
                        Timber.d("Đánh dấu thông báo đã đọc thành công: ${result.data.id}")
                        
                        // Cập nhật lại số lượng thông báo chưa đọc
                        getUnreadNotificationCount()
                    }
                    is Resource.Error -> {
                        _markAsReadState.value = ResourceState.Error(result.message ?: "Không thể đánh dấu thông báo đã đọc")
                        Timber.e("Đánh dấu thông báo đã đọc thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _markAsReadState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            _markAllAsReadState.value = ResourceState.Loading
            
            notificationRepository.markAllNotificationsAsRead().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _markAllAsReadState.value = ResourceState.Success(result.data!!)
                        Timber.d("Đánh dấu tất cả thông báo đã đọc thành công: ${result.data.markedCount} thông báo")
                        
                        getUnreadNotificationCount()
                        
                        getNotifications()
                    }
                    is Resource.Error -> {
                        _markAllAsReadState.value = ResourceState.Error(result.message ?: "Không thể đánh dấu tất cả thông báo đã đọc")
                        Timber.e("Đánh dấu tất cả thông báo đã đọc thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _markAllAsReadState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Xóa một thông báo
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            _deleteNotificationState.value = ResourceState.Loading
            
            notificationRepository.deleteNotification(notificationId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _deleteNotificationState.value = ResourceState.Success(true)
                        Timber.d("Xóa thông báo thành công: $notificationId")
                        
                        getNotifications()
                        
                        getUnreadNotificationCount()
                    }
                    is Resource.Error -> {
                        _deleteNotificationState.value = ResourceState.Error(result.message ?: "Không thể xóa thông báo")
                        Timber.e("Xóa thông báo thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _deleteNotificationState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Xóa tất cả thông báo
     */
    fun deleteAllNotifications() {
        viewModelScope.launch {
            _deleteAllNotificationsState.value = ResourceState.Loading
            
            notificationRepository.deleteAllNotifications().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _deleteAllNotificationsState.value = ResourceState.Success(result.data!!)
                        Timber.d("Xóa tất cả thông báo thành công: ${result.data.deletedCount} thông báo")
                        
                        getNotifications()
                        
                        getUnreadNotificationCount()
                    }
                    is Resource.Error -> {
                        _deleteAllNotificationsState.value = ResourceState.Error(result.message ?: "Không thể xóa tất cả thông báo")
                        Timber.e("Xóa tất cả thông báo thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _deleteAllNotificationsState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Lấy cài đặt thông báo
     */
    fun getNotificationPreferences() {
        viewModelScope.launch {
            _preferencesState.value = ResourceState.Loading
            
            notificationRepository.getNotificationPreferences().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _preferencesState.value = ResourceState.Success(result.data!!)
                        Timber.d("Lấy cài đặt thông báo thành công")
                    }
                    is Resource.Error -> {
                        _preferencesState.value = ResourceState.Error(result.message ?: "Không thể lấy cài đặt thông báo")
                        Timber.e("Lấy cài đặt thông báo thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _preferencesState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Cập nhật cài đặt thông báo
     */
    fun updateNotificationPreferences(preferences: NotificationPreferencesDto) {
        viewModelScope.launch {
            _preferencesState.value = ResourceState.Loading
            
            notificationRepository.updateNotificationPreferences(preferences).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _preferencesState.value = ResourceState.Success(result.data!!)
                        Timber.d("Cập nhật cài đặt thông báo thành công")
                    }
                    is Resource.Error -> {
                        _preferencesState.value = ResourceState.Error(result.message ?: "Không thể cập nhật cài đặt thông báo")
                        Timber.e("Cập nhật cài đặt thông báo thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _preferencesState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Reset các trạng thái
     */
    fun resetMarkAsReadState() {
        _markAsReadState.value = ResourceState.Initial
    }
    
    fun resetMarkAllAsReadState() {
        _markAllAsReadState.value = ResourceState.Initial
    }
    
    fun resetDeleteNotificationState() {
        _deleteNotificationState.value = ResourceState.Initial
    }
    
    fun resetDeleteAllNotificationsState() {
        _deleteAllNotificationsState.value = ResourceState.Initial
    }
} 