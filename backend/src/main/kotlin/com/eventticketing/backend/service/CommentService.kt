package com.eventticketing.backend.service

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.entity.CommentStatus
import java.util.*

interface CommentService {
    
    /**
     * Tạo bình luận mới
     */
    fun createComment(commentRequest: CommentRequest, currentUserId: UUID): CommentResponse
    
    /**
     * Cập nhật nội dung bình luận
     */
    fun updateComment(commentId: UUID, commentUpdateRequest: CommentUpdateRequest, currentUserId: UUID): CommentResponse
    
    /**
     * Xóa bình luận
     */
    fun deleteComment(commentId: UUID, currentUserId: UUID)
    
    /**
     * Lấy bình luận theo ID
     */
    fun getCommentById(commentId: UUID): CommentResponse
    
    /**
     * Lấy danh sách bình luận gốc của một sự kiện
     */
    fun getRootCommentsByEventId(eventId: UUID, page: Int, size: Int): CommentPageResponse
    
    /**
     * Lấy danh sách phản hồi của một bình luận
     */
    fun getRepliesByCommentId(commentId: UUID, page: Int, size: Int): CommentPageResponse
    
    /**
     * Lấy danh sách bình luận của người dùng hiện tại
     */
    fun getCommentsByCurrentUser(currentUserId: UUID, page: Int, size: Int): CommentPageResponse
    
    /**
     * Cập nhật trạng thái bình luận (chỉ dành cho Admin/Organizer)
     */
    fun updateCommentStatus(commentId: UUID, statusUpdateRequest: CommentStatusUpdateRequest, currentUserId: UUID): CommentResponse
    
    /**
     * Báo cáo bình luận không phù hợp
     */
    fun reportComment(commentId: UUID, reportRequest: CommentReportRequest, currentUserId: UUID)
    
    /**
     * Lấy danh sách bình luận theo trạng thái (chỉ dành cho Admin)
     */
    fun getCommentsByStatus(status: CommentStatus, page: Int, size: Int): CommentPageResponse
} 