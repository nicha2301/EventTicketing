package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.Rating
import com.eventticketing.backend.entity.RatingStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RatingRepository : JpaRepository<Rating, UUID> {
    
    // Tìm tất cả đánh giá của một sự kiện
    fun findByEventIdOrderByCreatedAtDesc(eventId: UUID, pageable: Pageable): Page<Rating>
    
    // Tìm tất cả đánh giá của một người dùng
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<Rating>
    
    // Tìm tất cả đánh giá của một sự kiện theo trạng thái
    fun findByEventIdAndStatusOrderByCreatedAtDesc(eventId: UUID, status: RatingStatus, pageable: Pageable): Page<Rating>
    
    // Tìm đánh giá của một người dùng cho một sự kiện
    fun findByUserIdAndEventId(userId: UUID, eventId: UUID): Optional<Rating>
    
    // Tìm tất cả đánh giá bị báo cáo
    fun findByIsReportedTrueOrderByCreatedAtDesc(pageable: Pageable): Page<Rating>
    
    // Tính điểm đánh giá trung bình của một sự kiện
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.event.id = :eventId AND r.status = 'APPROVED'")
    fun calculateAverageRatingByEventId(@Param("eventId") eventId: UUID): Double?
    
    // Đếm số lượng đánh giá của một sự kiện
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.event.id = :eventId AND r.status = 'APPROVED'")
    fun countByEventIdAndStatusApproved(@Param("eventId") eventId: UUID): Long
    
    // Đếm số lượng đánh giá theo điểm cho một sự kiện
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.event.id = :eventId AND r.score = :score AND r.status = 'APPROVED'")
    fun countByEventIdAndScoreAndStatusApproved(@Param("eventId") eventId: UUID, @Param("score") score: Int): Long
} 