package com.eventticketing.backend.service

import com.eventticketing.backend.dto.notification.NotificationCountResponse
import com.eventticketing.backend.dto.notification.NotificationDto
import com.eventticketing.backend.dto.notification.NotificationPreferencesRequest
import com.eventticketing.backend.dto.notification.NotificationResponse
import com.eventticketing.backend.entity.NotificationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface UserNotificationService {
    
    /**
     * Tạo thông báo mới
     */
    fun createNotification(notificationDto: NotificationDto): NotificationResponse
    
    /**
     * Lấy tất cả thông báo của một người dùng
     */
    fun getUserNotifications(userId: UUID?, pageable: Pageable): Page<NotificationResponse>
    
    /**
     * Lấy thông báo chưa đọc của một người dùng
     */
    fun getUnreadNotifications(userId: UUID?, pageable: Pageable): Page<NotificationResponse>
    
    /**
     * Lấy thông báo theo loại
     */
    fun getNotificationsByType(userId: UUID?, type: NotificationType, pageable: Pageable): Page<NotificationResponse>
    
    /**
     * Đánh dấu thông báo là đã đọc
     */
    fun markAsRead(userId: UUID?, notificationId: UUID): NotificationResponse
    
    /**
     * Đánh dấu tất cả thông báo là đã đọc
     */
    fun markAllAsRead(userId: UUID?): Int
    
    /**
     * Đếm số thông báo chưa đọc
     */
    fun countUnreadNotifications(userId: UUID?): NotificationCountResponse
    
    /**
     * Xóa thông báo
     */
    fun deleteNotification(userId: UUID?, notificationId: UUID): Boolean
    
    /**
     * Xóa tất cả thông báo của một người dùng
     */
    fun deleteAllNotifications(userId: UUID?): Int
    
    /**
     * Lấy cài đặt thông báo của người dùng
     */
    fun getNotificationPreferences(userId: UUID?): NotificationPreferencesRequest
    
    /**
     * Cập nhật cài đặt thông báo của người dùng
     */
    fun updateNotificationPreferences(userId: UUID?, preferences: NotificationPreferencesRequest): NotificationPreferencesRequest
} 