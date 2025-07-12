package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.entity.Rating
import com.eventticketing.backend.entity.RatingStatus
import com.eventticketing.backend.entity.UserRole
import com.eventticketing.backend.exception.*
import com.eventticketing.backend.repository.EventRepository
import com.eventticketing.backend.repository.RatingRepository
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.service.RatingService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class RatingServiceImpl(
    private val ratingRepository: RatingRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository
) : RatingService {

    @Transactional
    override fun createOrUpdateRating(ratingRequest: RatingRequest, currentUserId: UUID): RatingResponse {
        // Kiểm tra người dùng tồn tại
        val user = userRepository.findByIdOrNull(currentUserId)
            ?: throw RatingValidationException("Người dùng không tồn tại")
        
        // Kiểm tra sự kiện tồn tại
        val event = eventRepository.findByIdOrNull(ratingRequest.eventId)
            ?: throw EventNotFoundException("Sự kiện không tồn tại")
        
        // Kiểm tra điểm đánh giá hợp lệ (1-5)
        if (ratingRequest.score < 1 || ratingRequest.score > 5) {
            throw RatingValidationException("Điểm đánh giá phải từ 1 đến 5")
        }
        
        // Kiểm tra xem người dùng đã đánh giá sự kiện này chưa
        val existingRating = ratingRepository.findByUserIdAndEventId(currentUserId, ratingRequest.eventId)
        
        val rating = if (existingRating.isPresent) {
            // Cập nhật đánh giá hiện có
            val existing = existingRating.get()
            existing.score = ratingRequest.score
            existing.review = ratingRequest.review
            existing
        } else {
            // Tạo đánh giá mới
            Rating(
                score = ratingRequest.score,
                review = ratingRequest.review,
                event = event,
                user = user
            )
        }
        
        val savedRating = ratingRepository.save(rating)
        
        // Cập nhật đánh giá trung bình của sự kiện
        updateEventAverageRating(event.id!!)
        
        return mapToRatingResponse(savedRating)
    }
    
    @Transactional
    override fun updateRating(ratingId: UUID, ratingUpdateRequest: RatingUpdateRequest, currentUserId: UUID): RatingResponse {
        val rating = ratingRepository.findByIdOrNull(ratingId)
            ?: throw RatingNotFoundException("Đánh giá không tồn tại")
        
        // Kiểm tra quyền cập nhật
        if (rating.user.id != currentUserId) {
            throw RatingAccessDeniedException("Bạn không có quyền cập nhật đánh giá này")
        }
        
        // Kiểm tra điểm đánh giá hợp lệ
        if (ratingUpdateRequest.score < 1 || ratingUpdateRequest.score > 5) {
            throw RatingValidationException("Điểm đánh giá phải từ 1 đến 5")
        }
        
        rating.score = ratingUpdateRequest.score
        rating.review = ratingUpdateRequest.review
        val updatedRating = ratingRepository.save(rating)
        
        // Cập nhật đánh giá trung bình của sự kiện
        updateEventAverageRating(rating.event.id!!)
        
        return mapToRatingResponse(updatedRating)
    }
    
    @Transactional
    override fun deleteRating(ratingId: UUID, currentUserId: UUID) {
        val rating = ratingRepository.findByIdOrNull(ratingId)
            ?: throw RatingNotFoundException("Đánh giá không tồn tại")
        
        val currentUser = userRepository.findByIdOrNull(currentUserId)
            ?: throw RatingValidationException("Người dùng không tồn tại")
        
        // Kiểm tra quyền xóa (người tạo đánh giá, admin hoặc người tổ chức sự kiện)
        if (rating.user.id != currentUserId 
            && currentUser.role != UserRole.ADMIN 
            && (currentUser.role != UserRole.ORGANIZER || rating.event.organizer.id != currentUserId)) {
            throw RatingAccessDeniedException("Bạn không có quyền xóa đánh giá này")
        }
        
        val eventId = rating.event.id!!
        ratingRepository.delete(rating)
        
        // Cập nhật đánh giá trung bình của sự kiện
        updateEventAverageRating(eventId)
    }
    
    override fun getRatingById(ratingId: UUID): RatingResponse {
        val rating = ratingRepository.findByIdOrNull(ratingId)
            ?: throw RatingNotFoundException("Đánh giá không tồn tại")
        
        return mapToRatingResponse(rating)
    }
    
    override fun getUserRatingForEvent(userId: UUID, eventId: UUID): RatingResponse? {
        val ratingOptional = ratingRepository.findByUserIdAndEventId(userId, eventId)
        
        return if (ratingOptional.isPresent) {
            mapToRatingResponse(ratingOptional.get())
        } else {
            null
        }
    }
    
    override fun getRatingsByEventId(eventId: UUID, page: Int, size: Int): RatingPageResponse {
        // Kiểm tra sự kiện tồn tại
        if (!eventRepository.existsById(eventId)) {
            throw EventNotFoundException("Sự kiện không tồn tại")
        }
        
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val ratingPage = ratingRepository.findByEventIdAndStatusOrderByCreatedAtDesc(eventId, RatingStatus.APPROVED, pageable)
        
        val ratingResponses = ratingPage.content.map { rating ->
            mapToRatingResponse(rating)
        }
        
        return RatingPageResponse(
            ratings = ratingResponses,
            currentPage = page,
            totalItems = ratingPage.totalElements,
            totalPages = ratingPage.totalPages
        )
    }
    
    override fun getRatingsByCurrentUser(currentUserId: UUID, page: Int, size: Int): RatingPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val ratingPage = ratingRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, pageable)
        
        val ratingResponses = ratingPage.content.map { rating ->
            mapToRatingResponse(rating)
        }
        
        return RatingPageResponse(
            ratings = ratingResponses,
            currentPage = page,
            totalItems = ratingPage.totalElements,
            totalPages = ratingPage.totalPages
        )
    }
    
    @Transactional
    override fun updateRatingStatus(ratingId: UUID, statusUpdateRequest: RatingStatusUpdateRequest, currentUserId: UUID): RatingResponse {
        val rating = ratingRepository.findByIdOrNull(ratingId)
            ?: throw RatingNotFoundException("Đánh giá không tồn tại")
        
        val currentUser = userRepository.findByIdOrNull(currentUserId)
            ?: throw RatingValidationException("Người dùng không tồn tại")
        
        // Chỉ admin hoặc người tổ chức sự kiện mới có thể cập nhật trạng thái
        if (currentUser.role != UserRole.ADMIN && 
            (currentUser.role != UserRole.ORGANIZER || rating.event.organizer.id != currentUserId)) {
            throw RatingAccessDeniedException("Bạn không có quyền cập nhật trạng thái đánh giá này")
        }
        
        rating.status = statusUpdateRequest.status
        val updatedRating = ratingRepository.save(rating)
        
        // Cập nhật đánh giá trung bình của sự kiện
        updateEventAverageRating(rating.event.id!!)
        
        return mapToRatingResponse(updatedRating)
    }
    
    @Transactional
    override fun reportRating(ratingId: UUID, reportRequest: RatingReportRequest, currentUserId: UUID) {
        val rating = ratingRepository.findByIdOrNull(ratingId)
            ?: throw RatingNotFoundException("Đánh giá không tồn tại")
        
        // Kiểm tra người dùng tồn tại
        if (!userRepository.existsById(currentUserId)) {
            throw RatingValidationException("Người dùng không tồn tại")
        }
        
        // Kiểm tra đánh giá đã bị báo cáo chưa
        if (rating.isReported) {
            throw RatingAlreadyReportedException("Đánh giá này đã bị báo cáo trước đó")
        }
        
        rating.isReported = true
        rating.reportReason = reportRequest.reason
        ratingRepository.save(rating)
    }
    
    override fun getReportedRatings(page: Int, size: Int): RatingPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val ratingPage = ratingRepository.findByIsReportedTrueOrderByCreatedAtDesc(pageable)
        
        val ratingResponses = ratingPage.content.map { rating ->
            mapToRatingResponse(rating)
        }
        
        return RatingPageResponse(
            ratings = ratingResponses,
            currentPage = page,
            totalItems = ratingPage.totalElements,
            totalPages = ratingPage.totalPages
        )
    }
    
    override fun getRatingStatistics(eventId: UUID): RatingStatisticsResponse {
        // Kiểm tra sự kiện tồn tại
        if (!eventRepository.existsById(eventId)) {
            throw EventNotFoundException("Sự kiện không tồn tại")
        }
        
        val averageRating = ratingRepository.calculateAverageRatingByEventId(eventId) ?: 0.0
        val totalRatings = ratingRepository.countByEventIdAndStatusApproved(eventId)
        
        // Tính số lượng đánh giá cho từng điểm (1-5)
        val ratingCounts = mutableMapOf<Int, Long>()
        for (score in 1..5) {
            val count = ratingRepository.countByEventIdAndScoreAndStatusApproved(eventId, score)
            ratingCounts[score] = count
        }
        
        return RatingStatisticsResponse(
            eventId = eventId,
            averageRating = averageRating,
            totalRatings = totalRatings,
            ratingCounts = ratingCounts
        )
    }
    
    // Helper method để cập nhật đánh giá trung bình của sự kiện
    private fun updateEventAverageRating(eventId: UUID) {
        val event = eventRepository.findByIdOrNull(eventId)
            ?: return
        
        event.updateAverageRating()
        eventRepository.save(event)
    }
    
    // Helper method để chuyển đổi Rating thành RatingResponse
    private fun mapToRatingResponse(rating: Rating): RatingResponse {
        return RatingResponse(
            id = rating.id,
            score = rating.score,
            review = rating.review,
            eventId = rating.event.id!!,
            eventTitle = rating.event.title,
            userId = rating.user.id!!,
            username = rating.user.fullName,
            userAvatar = null, // TODO: Implement user avatar
            status = rating.status,
            isReported = rating.isReported,
            createdAt = rating.createdAt,
            updatedAt = rating.updatedAt
        )
    }
} 