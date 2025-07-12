package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.entity.Comment
import com.eventticketing.backend.entity.CommentStatus
import com.eventticketing.backend.entity.UserRole
import com.eventticketing.backend.exception.CommentAccessDeniedException
import com.eventticketing.backend.exception.CommentNotFoundException
import com.eventticketing.backend.exception.CommentValidationException
import com.eventticketing.backend.exception.EventNotFoundException
import com.eventticketing.backend.repository.CommentRepository
import com.eventticketing.backend.repository.EventRepository
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.service.CommentService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CommentServiceImpl(
    private val commentRepository: CommentRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository
) : CommentService {

    @Transactional
    override fun createComment(commentRequest: CommentRequest, currentUserId: UUID): CommentResponse {
        val user = userRepository.findByIdOrNull(currentUserId)
            ?: throw CommentValidationException("Người dùng không tồn tại")
        
        val event = eventRepository.findByIdOrNull(commentRequest.eventId)
            ?: throw EventNotFoundException("Sự kiện không tồn tại")
        
        // Kiểm tra nếu là phản hồi, thì bình luận gốc phải tồn tại
        var parent: Comment? = null
        if (commentRequest.parentId != null) {
            parent = commentRepository.findByIdOrNull(commentRequest.parentId)
                ?: throw CommentNotFoundException("Bình luận gốc không tồn tại")
            
            // Đảm bảo bình luận gốc thuộc cùng sự kiện
            if (parent.event.id != event.id) {
                throw CommentValidationException("Bình luận gốc không thuộc cùng sự kiện")
            }
            
            // Đảm bảo không phản hồi cho một phản hồi (chỉ cho phép 1 cấp phản hồi)
            if (parent.parent != null) {
                throw CommentValidationException("Không thể phản hồi cho một phản hồi")
            }
        }
        
        // Kiểm tra nội dung bình luận
        if (commentRequest.content.isBlank()) {
            throw CommentValidationException("Nội dung bình luận không được để trống")
        }
        
        // Tạo bình luận mới
        val comment = Comment(
            content = commentRequest.content,
            event = event,
            user = user,
            parent = parent,
            // Admin và Organizer không cần phê duyệt bình luận
            status = if (user.role == UserRole.ADMIN || user.role == UserRole.ORGANIZER) 
                CommentStatus.APPROVED else CommentStatus.PENDING
        )
        
        val savedComment = commentRepository.save(comment)
        
        return mapToCommentResponse(savedComment)
    }
    
    @Transactional
    override fun updateComment(commentId: UUID, commentUpdateRequest: CommentUpdateRequest, currentUserId: UUID): CommentResponse {
        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw CommentNotFoundException("Bình luận không tồn tại")
        
        // Chỉ người tạo bình luận mới có thể cập nhật
        if (comment.user.id != currentUserId) {
            throw CommentAccessDeniedException("Bạn không có quyền cập nhật bình luận này")
        }
        
        // Kiểm tra nội dung bình luận
        if (commentUpdateRequest.content.isBlank()) {
            throw CommentValidationException("Nội dung bình luận không được để trống")
        }
        
        comment.content = commentUpdateRequest.content
        val updatedComment = commentRepository.save(comment)
        
        return mapToCommentResponse(updatedComment)
    }
    
    @Transactional
    override fun deleteComment(commentId: UUID, currentUserId: UUID) {
        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw CommentNotFoundException("Bình luận không tồn tại")
        
        val currentUser = userRepository.findByIdOrNull(currentUserId)
            ?: throw CommentValidationException("Người dùng không tồn tại")
        
        // Người tạo bình luận, admin hoặc người tổ chức sự kiện có thể xóa bình luận
        if (comment.user.id != currentUserId 
            && currentUser.role != UserRole.ADMIN 
            && (currentUser.role != UserRole.ORGANIZER || comment.event.organizer.id != currentUserId)) {
            throw CommentAccessDeniedException("Bạn không có quyền xóa bình luận này")
        }
        
        commentRepository.delete(comment)
    }
    
    override fun getCommentById(commentId: UUID): CommentResponse {
        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw CommentNotFoundException("Bình luận không tồn tại")
        
        return mapToCommentResponse(comment)
    }
    
    override fun getRootCommentsByEventId(eventId: UUID, page: Int, size: Int): CommentPageResponse {
        // Kiểm tra sự kiện tồn tại
        if (!eventRepository.existsById(eventId)) {
            throw EventNotFoundException("Sự kiện không tồn tại")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val commentPage = commentRepository.findByEventIdAndParentIsNullOrderByCreatedAtDesc(eventId, pageable)
        
        val commentResponses = commentPage.content.map { comment ->
            val replyCount = commentRepository.countByEventIdAndStatus(comment.event.id!!, CommentStatus.APPROVED)
            mapToCommentResponse(comment, replyCount.toInt())
        }
        
        return CommentPageResponse(
            comments = commentResponses,
            currentPage = page,
            totalItems = commentPage.totalElements,
            totalPages = commentPage.totalPages
        )
    }
    
    override fun getRepliesByCommentId(commentId: UUID, page: Int, size: Int): CommentPageResponse {
        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw CommentNotFoundException("Bình luận không tồn tại")
        
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"))
        val repliesPage = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId, pageable)
        
        val commentResponses = repliesPage.content.map { reply ->
            mapToCommentResponse(reply)
        }
        
        return CommentPageResponse(
            comments = commentResponses,
            currentPage = page,
            totalItems = repliesPage.totalElements,
            totalPages = repliesPage.totalPages
        )
    }
    
    override fun getCommentsByCurrentUser(currentUserId: UUID, page: Int, size: Int): CommentPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val commentPage = commentRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, pageable)
        
        val commentResponses = commentPage.content.map { comment ->
            mapToCommentResponse(comment)
        }
        
        return CommentPageResponse(
            comments = commentResponses,
            currentPage = page,
            totalItems = commentPage.totalElements,
            totalPages = commentPage.totalPages
        )
    }
    
    @Transactional
    override fun updateCommentStatus(commentId: UUID, statusUpdateRequest: CommentStatusUpdateRequest, currentUserId: UUID): CommentResponse {
        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw CommentNotFoundException("Bình luận không tồn tại")
        
        val currentUser = userRepository.findByIdOrNull(currentUserId)
            ?: throw CommentValidationException("Người dùng không tồn tại")
        
        // Chỉ admin hoặc người tổ chức sự kiện mới có thể cập nhật trạng thái
        if (currentUser.role != UserRole.ADMIN && 
            (currentUser.role != UserRole.ORGANIZER || comment.event.organizer.id != currentUserId)) {
            throw CommentAccessDeniedException("Bạn không có quyền cập nhật trạng thái bình luận này")
        }
        
        comment.status = statusUpdateRequest.status
        val updatedComment = commentRepository.save(comment)
        
        return mapToCommentResponse(updatedComment)
    }
    
    @Transactional
    override fun reportComment(commentId: UUID, reportRequest: CommentReportRequest, currentUserId: UUID) {
        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw CommentNotFoundException("Bình luận không tồn tại")
        
        // Kiểm tra người dùng tồn tại
        if (!userRepository.existsById(currentUserId)) {
            throw CommentValidationException("Người dùng không tồn tại")
        }
        
        // Đánh dấu bình luận là đã bị báo cáo
        comment.status = CommentStatus.REPORTED
        commentRepository.save(comment)
    }
    
    override fun getCommentsByStatus(status: CommentStatus, page: Int, size: Int): CommentPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val commentPage = commentRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
        
        val commentResponses = commentPage.content.map { comment ->
            mapToCommentResponse(comment)
        }
        
        return CommentPageResponse(
            comments = commentResponses,
            currentPage = page,
            totalItems = commentPage.totalElements,
            totalPages = commentPage.totalPages
        )
    }
    
    // Helper method để chuyển đổi Comment thành CommentResponse
    private fun mapToCommentResponse(comment: Comment, replyCount: Int = 0): CommentResponse {
        return CommentResponse(
            id = comment.id,
            content = comment.content,
            eventId = comment.event.id!!,
            eventTitle = comment.event.title,
            userId = comment.user.id!!,
            username = comment.user.fullName,
            userAvatar = null, // TODO: Implement user avatar
            parentId = comment.parent?.id,
            status = comment.status,
            createdAt = comment.createdAt,
            updatedAt = comment.updatedAt,
            replyCount = replyCount,
            replies = null // Replies are loaded separately
        )
    }
} 