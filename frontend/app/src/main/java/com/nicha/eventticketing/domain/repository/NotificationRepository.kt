package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.notification.DeleteAllResultDto
import com.nicha.eventticketing.data.remote.dto.notification.DeviceTokenDto
import com.nicha.eventticketing.data.remote.dto.notification.MarkAllReadResultDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationPreferencesDto
import com.nicha.eventticketing.data.remote.dto.notification.UnreadCountDto
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository cho các thao tác liên quan đến thông báo
 */
interface NotificationRepository {
    /**
     * Lấy danh sách thông báo
     */
    fun getNotifications(page: Int, size: Int): Flow<Resource<PageDto<NotificationDto>>>
    
    /**
     * Lấy danh sách thông báo chưa đọc
     */
    fun getUnreadNotifications(page: Int, size: Int): Flow<Resource<PageDto<NotificationDto>>>
    
    /**
     * Lấy danh sách thông báo theo loại
     */
    fun getNotificationsByType(type: String, page: Int, size: Int): Flow<Resource<PageDto<NotificationDto>>>
    
    /**
     * Lấy số lượng thông báo chưa đọc
     */
    fun getUnreadNotificationCount(): Flow<Resource<UnreadCountDto>>
    
    /**
     * Đánh dấu thông báo đã đọc
     */
    fun markNotificationAsRead(notificationId: String): Flow<Resource<NotificationDto>>
    
    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    fun markAllNotificationsAsRead(): Flow<Resource<MarkAllReadResultDto>>
    
    /**
     * Xóa một thông báo
     */
    fun deleteNotification(notificationId: String): Flow<Resource<Boolean>>
    
    /**
     * Xóa tất cả thông báo
     */
    fun deleteAllNotifications(): Flow<Resource<DeleteAllResultDto>>
    
    /**
     * Lấy cài đặt thông báo
     */
    fun getNotificationPreferences(): Flow<Resource<NotificationPreferencesDto>>
    
    /**
     * Cập nhật cài đặt thông báo
     */
    fun updateNotificationPreferences(preferences: NotificationPreferencesDto): Flow<Resource<NotificationPreferencesDto>>
    
    /**
     * Đăng ký token thiết bị
     */
    fun registerDeviceToken(token: String, deviceType: String): Flow<Resource<DeviceTokenDto>>
    
    /**
     * Lấy danh sách token thiết bị
     */
    fun getDeviceTokens(): Flow<Resource<List<DeviceTokenDto>>>
    
    /**
     * Xóa token thiết bị
     */
    fun deleteDeviceToken(tokenId: String): Flow<Resource<Boolean>>
    
    /**
     * Xóa tất cả token thiết bị
     */
    fun deleteAllDeviceTokens(): Flow<Resource<Int>>
    
    /**
     * Đăng ký nhận thông báo theo chủ đề
     */
    fun subscribeToTopic(topic: String): Flow<Resource<Boolean>>
    
    /**
     * Hủy đăng ký thông báo theo chủ đề
     */
    fun unsubscribeFromTopic(topic: String): Flow<Resource<Boolean>>
} 