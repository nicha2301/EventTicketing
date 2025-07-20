package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationDto
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository xử lý các chức năng liên quan đến thông báo
 */
interface NotificationRepository {
    /**
     * Lấy danh sách thông báo của người dùng hiện tại
     * @param page Số trang
     * @param size Số lượng mỗi trang
     * @return Flow<Resource<PageDto<NotificationDto>>> Flow chứa danh sách thông báo theo trang
     */
    fun getNotifications(
        page: Int = 0,
        size: Int = 20
    ): Flow<Resource<PageDto<NotificationDto>>>
    
    /**
     * Đánh dấu thông báo đã đọc
     * @param notificationId ID của thông báo
     * @return Flow<Resource<NotificationDto>> Flow chứa thông tin thông báo đã đọc
     */
    fun markNotificationAsRead(notificationId: String): Flow<Resource<NotificationDto>>
    
    /**
     * Cập nhật tùy chọn thông báo
     * @param preferences Map chứa các tùy chọn thông báo
     * @return Flow<Resource<Map<String, Any>>> Flow chứa thông tin tùy chọn thông báo đã cập nhật
     */
    fun updateNotificationPreferences(preferences: Map<String, Any>): Flow<Resource<Map<String, Any>>>
    
    /**
     * Đánh dấu tất cả thông báo đã đọc
     * @return Flow<Resource<Boolean>> Flow chứa kết quả đánh dấu
     */
    fun markAllNotificationsAsRead(): Flow<Resource<Boolean>>
} 