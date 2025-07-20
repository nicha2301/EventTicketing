package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationDto
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

/**
 * ViewModel để quản lý dữ liệu thông báo
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    
    // State cho danh sách thông báo
    private val _notificationsState = MutableStateFlow<ResourceState<PageDto<NotificationDto>>>(ResourceState.Initial)
    val notificationsState: StateFlow<ResourceState<PageDto<NotificationDto>>> = _notificationsState.asStateFlow()
    
    // State cho việc đánh dấu thông báo đã đọc
    private val _markAsReadState = MutableStateFlow<ResourceState<NotificationDto>>(ResourceState.Initial)
    val markAsReadState: StateFlow<ResourceState<NotificationDto>> = _markAsReadState.asStateFlow()
    
    // State cho việc cập nhật tùy chọn thông báo
    private val _preferencesState = MutableStateFlow<ResourceState<Map<String, Any>>>(ResourceState.Initial)
    val preferencesState: StateFlow<ResourceState<Map<String, Any>>> = _preferencesState.asStateFlow()
    
    // State cho việc đánh dấu tất cả thông báo đã đọc
    private val _markAllAsReadState = MutableStateFlow<ResourceState<Boolean>>(ResourceState.Initial)
    val markAllAsReadState: StateFlow<ResourceState<Boolean>> = _markAllAsReadState.asStateFlow()
    
    /**
     * Lấy danh sách thông báo
     */
    fun getNotifications(page: Int = 0, size: Int = 20) {
        _notificationsState.value = ResourceState.Loading
        
        viewModelScope.launch {
            notificationRepository.getNotifications(page, size).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { notifications ->
                            _notificationsState.value = ResourceState.Success(notifications)
                            Timber.d("Lấy danh sách thông báo thành công: ${notifications.content?.size ?: 0} thông báo")
                        } ?: run {
                            _notificationsState.value = ResourceState.Error("Không tìm thấy thông báo")
                        }
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
     * Đánh dấu thông báo đã đọc
     */
    fun markNotificationAsRead(notificationId: String) {
        _markAsReadState.value = ResourceState.Loading
        
        viewModelScope.launch {
            notificationRepository.markNotificationAsRead(notificationId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { notification ->
                            _markAsReadState.value = ResourceState.Success(notification)
                            Timber.d("Đánh dấu thông báo đã đọc thành công: ${notification.id}")
                            
                            // Cập nhật lại danh sách thông báo
                            updateNotificationInList(notification)
                        } ?: run {
                            _markAsReadState.value = ResourceState.Error("Không tìm thấy thông báo")
                        }
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
     * Cập nhật tùy chọn thông báo
     */
    fun updateNotificationPreferences(preferences: Map<String, Any>) {
        _preferencesState.value = ResourceState.Loading
        
        viewModelScope.launch {
            notificationRepository.updateNotificationPreferences(preferences).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { updatedPreferences ->
                            _preferencesState.value = ResourceState.Success(updatedPreferences)
                            Timber.d("Cập nhật tùy chọn thông báo thành công")
                        } ?: run {
                            _preferencesState.value = ResourceState.Error("Không thể cập nhật tùy chọn thông báo")
                        }
                    }
                    is Resource.Error -> {
                        _preferencesState.value = ResourceState.Error(result.message ?: "Không thể cập nhật tùy chọn thông báo")
                        Timber.e("Cập nhật tùy chọn thông báo thất bại: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _preferencesState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    fun markAllNotificationsAsRead() {
        _markAllAsReadState.value = ResourceState.Loading
        
        viewModelScope.launch {
            notificationRepository.markAllNotificationsAsRead().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { success ->
                            _markAllAsReadState.value = ResourceState.Success(success)
                            if (success) {
                                Timber.d("Đánh dấu tất cả thông báo đã đọc thành công")
                                
                                // Cập nhật lại danh sách thông báo
                                updateAllNotificationsAsRead()
                            } else {
                                _markAllAsReadState.value = ResourceState.Error("Không thể đánh dấu tất cả thông báo đã đọc")
                            }
                        } ?: run {
                            _markAllAsReadState.value = ResourceState.Error("Không thể đánh dấu tất cả thông báo đã đọc")
                        }
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
     * Cập nhật thông báo trong danh sách
     */
    private fun updateNotificationInList(updatedNotification: NotificationDto) {
        val currentState = _notificationsState.value
        if (currentState is ResourceState.Success) {
            val currentNotifications = currentState.data.content?.toMutableList() ?: mutableListOf()
            val index = currentNotifications.indexOfFirst { it.id == updatedNotification.id }
            if (index != -1) {
                currentNotifications[index] = updatedNotification
                val updatedPage = currentState.data.copy(content = currentNotifications)
                _notificationsState.value = ResourceState.Success(updatedPage)
            }
        }
    }
    
    /**
     * Cập nhật tất cả thông báo trong danh sách là đã đọc
     */
    private fun updateAllNotificationsAsRead() {
        val currentState = _notificationsState.value
        if (currentState is ResourceState.Success) {
            val currentNotifications = currentState.data.content?.toMutableList() ?: mutableListOf()
            val updatedNotifications = currentNotifications.map { it.copy(isRead = true) }
            val updatedPage = currentState.data.copy(content = updatedNotifications)
            _notificationsState.value = ResourceState.Success(updatedPage)
        }
    }
    
    /**
     * Reset states
     */
    fun resetMarkAsReadState() {
        _markAsReadState.value = ResourceState.Initial
    }
    
    fun resetPreferencesState() {
        _preferencesState.value = ResourceState.Initial
    }
    
    fun resetMarkAllAsReadState() {
        _markAllAsReadState.value = ResourceState.Initial
    }
} 