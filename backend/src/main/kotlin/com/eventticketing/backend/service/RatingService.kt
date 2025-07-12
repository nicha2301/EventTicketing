package com.eventticketing.backend.service

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.entity.RatingStatus
import java.util.*

interface RatingService {
    
    /**
     * Tạo đánh giá mới hoặc cập nhật đánh giá hiện có
     */
    fun createOrUpdateRating(ratingRequest: RatingRequest, currentUserId: UUID): RatingResponse
    
    /**
     * Cập nhật đánh giá
     */
    fun updateRating(ratingId: UUID, ratingUpdateRequest: RatingUpdateRequest, currentUserId: UUID): RatingResponse
    
    /**
     * Xóa đánh giá
     */
    fun deleteRating(ratingId: UUID, currentUserId: UUID)
    
    /**
     * Lấy đánh giá theo ID
     */
    fun getRatingById(ratingId: UUID): RatingResponse
    
    /**
     * Lấy đánh giá của người dùng cho sự kiện
     */
    fun getUserRatingForEvent(userId: UUID, eventId: UUID): RatingResponse?
    
    /**
     * Lấy danh sách đánh giá của một sự kiện
     */
    fun getRatingsByEventId(eventId: UUID, page: Int, size: Int): RatingPageResponse
    
    /**
     * Lấy danh sách đánh giá của người dùng hiện tại
     */
    fun getRatingsByCurrentUser(currentUserId: UUID, page: Int, size: Int): RatingPageResponse
    
    /**
     * Cập nhật trạng thái đánh giá (chỉ dành cho Admin/Organizer)
     */
    fun updateRatingStatus(ratingId: UUID, statusUpdateRequest: RatingStatusUpdateRequest, currentUserId: UUID): RatingResponse
    
    /**
     * Báo cáo đánh giá không phù hợp
     */
    fun reportRating(ratingId: UUID, reportRequest: RatingReportRequest, currentUserId: UUID)
    
    /**
     * Lấy danh sách đánh giá bị báo cáo (chỉ dành cho Admin)
     */
    fun getReportedRatings(page: Int, size: Int): RatingPageResponse
    
    /**
     * Lấy thống kê đánh giá của một sự kiện
     */
    fun getRatingStatistics(eventId: UUID): RatingStatisticsResponse
} 