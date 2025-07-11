package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.Notification
import com.eventticketing.backend.entity.NotificationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface NotificationRepository : JpaRepository<Notification, UUID> {
    
    /**
     * Tìm thông báo theo userId
     */
    fun findByUserId(userId: UUID, pageable: Pageable): Page<Notification>
    
    /**
     * Tìm thông báo theo userId và isRead
     */
    fun findByUserIdAndIsRead(userId: UUID, isRead: Boolean, pageable: Pageable): Page<Notification>
    
    /**
     * Tìm thông báo theo userId và notificationType
     */
    fun findByUserIdAndNotificationType(userId: UUID, notificationType: NotificationType, pageable: Pageable): Page<Notification>
    
    /**
     * Tìm thông báo theo referenceId
     */
    fun findByReferenceId(referenceId: UUID, pageable: Pageable): Page<Notification>
    
    /**
     * Đếm số thông báo chưa đọc theo userId
     */
    fun countByUserIdAndIsReadFalse(userId: UUID): Long
    
    /**
     * Đánh dấu tất cả thông báo của một người dùng là đã đọc
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user.id = :userId AND n.isRead = false")
    fun markAllAsRead(@Param("userId") userId: UUID, @Param("readAt") readAt: LocalDateTime = LocalDateTime.now()): Int
    
    /**
     * Xóa thông báo cũ hơn một ngày nhất định
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :date")
    fun deleteOlderThan(@Param("date") date: LocalDateTime): Int
} 