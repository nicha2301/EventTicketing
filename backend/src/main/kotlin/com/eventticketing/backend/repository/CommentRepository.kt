package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.Comment
import com.eventticketing.backend.entity.CommentStatus
import com.eventticketing.backend.entity.Event
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommentRepository : JpaRepository<Comment, UUID> {
    
    // Tìm tất cả bình luận gốc (không có parent) của một sự kiện
    fun findByEventIdAndParentIsNullOrderByCreatedAtDesc(eventId: UUID, pageable: Pageable): Page<Comment>
    
    // Tìm tất cả bình luận phản hồi của một bình luận
    fun findByParentIdOrderByCreatedAtAsc(parentId: UUID, pageable: Pageable): Page<Comment>
    
    // Tìm tất cả bình luận của một người dùng
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<Comment>
    
    // Tìm tất cả bình luận của một sự kiện
    fun findByEventIdOrderByCreatedAtDesc(eventId: UUID, pageable: Pageable): Page<Comment>
    
    // Tìm tất cả bình luận theo trạng thái
    fun findByStatusOrderByCreatedAtDesc(status: CommentStatus, pageable: Pageable): Page<Comment>
    
    // Tìm tất cả bình luận của một sự kiện theo trạng thái
    fun findByEventIdAndStatusOrderByCreatedAtDesc(eventId: UUID, status: CommentStatus, pageable: Pageable): Page<Comment>
    
    // Đếm số lượng bình luận của một sự kiện
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.event.id = :eventId")
    fun countByEventId(@Param("eventId") eventId: UUID): Long
    
    // Đếm số lượng bình luận của một sự kiện theo trạng thái
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.event.id = :eventId AND c.status = :status")
    fun countByEventIdAndStatus(@Param("eventId") eventId: UUID, @Param("status") status: CommentStatus): Long
} 