package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.entity.CommentStatus
import com.eventticketing.backend.security.CurrentUser
import com.eventticketing.backend.service.CommentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comment", description = "API để quản lý bình luận")
class CommentController(private val commentService: CommentService) {

    @PostMapping
    @Operation(summary = "Tạo bình luận mới", security = [SecurityRequirement(name = "bearer-key")])
    fun createComment(
        @Valid @RequestBody commentRequest: CommentRequest,
        @CurrentUser currentUserId: UUID
    ): ResponseEntity<CommentResponse> {
        val createdComment = commentService.createComment(commentRequest, currentUserId)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment)
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Cập nhật bình luận", security = [SecurityRequirement(name = "bearer-key")])
    fun updateComment(
        @PathVariable commentId: UUID,
        @Valid @RequestBody commentUpdateRequest: CommentUpdateRequest,
        @CurrentUser currentUserId: UUID
    ): ResponseEntity<CommentResponse> {
        val updatedComment = commentService.updateComment(commentId, commentUpdateRequest, currentUserId)
        return ResponseEntity.ok(updatedComment)
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Xóa bình luận", security = [SecurityRequirement(name = "bearer-key")])
    fun deleteComment(
        @PathVariable commentId: UUID,
        @CurrentUser currentUserId: UUID
    ): ResponseEntity<Void> {
        commentService.deleteComment(commentId, currentUserId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Lấy bình luận theo ID")
    fun getCommentById(@PathVariable commentId: UUID): ResponseEntity<CommentResponse> {
        val comment = commentService.getCommentById(commentId)
        return ResponseEntity.ok(comment)
    }

    @GetMapping("/events/{eventId}")
    @Operation(summary = "Lấy danh sách bình luận gốc của một sự kiện")
    fun getRootCommentsByEventId(
        @PathVariable eventId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<CommentPageResponse> {
        val comments = commentService.getRootCommentsByEventId(eventId, page, size)
        return ResponseEntity.ok(comments)
    }

    @GetMapping("/{commentId}/replies")
    @Operation(summary = "Lấy danh sách phản hồi của một bình luận")
    fun getRepliesByCommentId(
        @PathVariable commentId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<CommentPageResponse> {
        val replies = commentService.getRepliesByCommentId(commentId, page, size)
        return ResponseEntity.ok(replies)
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy danh sách bình luận của người dùng hiện tại", security = [SecurityRequirement(name = "bearer-key")])
    fun getCommentsByCurrentUser(
        @CurrentUser currentUserId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<CommentPageResponse> {
        val comments = commentService.getCommentsByCurrentUser(currentUserId, page, size)
        return ResponseEntity.ok(comments)
    }

    @PutMapping("/{commentId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Cập nhật trạng thái bình luận (Admin/Organizer)", security = [SecurityRequirement(name = "bearer-key")])
    fun updateCommentStatus(
        @PathVariable commentId: UUID,
        @Valid @RequestBody statusUpdateRequest: CommentStatusUpdateRequest,
        @CurrentUser currentUserId: UUID
    ): ResponseEntity<CommentResponse> {
        val updatedComment = commentService.updateCommentStatus(commentId, statusUpdateRequest, currentUserId)
        return ResponseEntity.ok(updatedComment)
    }

    @PostMapping("/{commentId}/report")
    @Operation(summary = "Báo cáo bình luận không phù hợp", security = [SecurityRequirement(name = "bearer-key")])
    fun reportComment(
        @PathVariable commentId: UUID,
        @Valid @RequestBody reportRequest: CommentReportRequest,
        @CurrentUser currentUserId: UUID
    ): ResponseEntity<Void> {
        commentService.reportComment(commentId, reportRequest, currentUserId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách bình luận theo trạng thái (Admin)", security = [SecurityRequirement(name = "bearer-key")])
    fun getCommentsByStatus(
        @PathVariable status: CommentStatus,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<CommentPageResponse> {
        val comments = commentService.getCommentsByStatus(status, page, size)
        return ResponseEntity.ok(comments)
    }
} 