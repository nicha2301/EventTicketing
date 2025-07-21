package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.Event
import com.eventticketing.backend.entity.EventStatus
import com.eventticketing.backend.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface EventRepository : JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {
    
    /**
     * Tìm sự kiện theo organizerId
     */
    fun findByOrganizerId(organizerId: UUID, pageable: Pageable): Page<Event>
    
    /**
     * Tìm sự kiện theo trạng thái
     */
    fun findByStatus(status: EventStatus, pageable: Pageable): Page<Event>
    
    /**
     * Tìm sự kiện theo danh mục
     */
    fun findByCategoryId(categoryId: UUID, pageable: Pageable): Page<Event>
    
    /**
     * Tìm sự kiện theo vị trí
     */
    fun findByLocationId(locationId: UUID, pageable: Pageable): Page<Event>
    
    /**
     * Tìm sự kiện sắp diễn ra (startDate > now)
     */
    fun findByStartDateAfterAndStatus(startDate: LocalDateTime, status: EventStatus, pageable: Pageable): Page<Event>
    
    /**
     * Tìm sự kiện đã kết thúc (endDate < now)
     */
    fun findByEndDateBeforeAndStatus(endDate: LocalDateTime, status: EventStatus, pageable: Pageable): Page<Event>
    
    /**
     * Tìm tất cả sự kiện đã kết thúc (endDate < now) theo trạng thái
     */
    fun findByStatusAndEndDateBefore(status: EventStatus, endDate: LocalDateTime): List<Event>
    
    /**
     * Tìm sự kiện đang diễn ra (startDate < now < endDate)
     */
    fun findByStartDateBeforeAndEndDateAfterAndStatus(now: LocalDateTime, endTime: LocalDateTime, status: EventStatus, pageable: Pageable): Page<Event>
    
    /**
     * Tìm sự kiện nổi bật
     */
    fun findByIsFeaturedTrueAndStatus(status: EventStatus, pageable: Pageable): Page<Event>
    
    /**
     * Tìm sự kiện miễn phí
     */
    fun findByIsFreeTrue(pageable: Pageable): Page<Event>
    
    /**
     * Tìm kiếm sự kiện theo từ khóa trong tiêu đề hoặc mô tả
     */
    @Query("SELECT e FROM Event e WHERE " +
            "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND e.status = :status")
    fun searchByKeywordAndStatus(
        @Param("keyword") keyword: String,
        @Param("status") status: EventStatus,
        pageable: Pageable
    ): Page<Event>
    
    /**
     * Tìm sự kiện gần đây theo vị trí
     * Sử dụng công thức Haversine để tính khoảng cách
     */
    @Query(
        value = "SELECT * FROM events e " +
                "WHERE e.status = :status " +
                "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(e.latitude)) * cos(radians(e.longitude) - " +
                "radians(:longitude)) + sin(radians(:latitude)) * sin(radians(e.latitude)))) < :radius " +
                "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(e.latitude)) * cos(radians(e.longitude) - " +
                "radians(:longitude)) + sin(radians(:latitude)) * sin(radians(e.latitude))))",
        nativeQuery = true
    )
    fun findNearbyEvents(
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
        @Param("radius") radius: Double,
        @Param("status") status: String,
        pageable: Pageable
    ): Page<Event>
    
    /**
     * Đếm số sự kiện theo organizerId
     */
    fun countByOrganizerId(organizerId: UUID): Long
    
    /**
     * Đếm số sự kiện theo trạng thái
     */
    fun countByStatus(status: EventStatus): Long
    
    /**
     * Kiểm tra sự kiện tồn tại theo ID và organizerId
     */
    fun existsByIdAndOrganizerId(id: UUID, organizerId: UUID): Boolean

    @Query(
        "SELECT COUNT(e) FROM Event e WHERE e.category.id = :categoryId"
    )
    fun countByCategoryId(@Param("categoryId") categoryId: UUID): Long

    @Query(
        "SELECT COUNT(e) FROM Event e WHERE e.location.id = :locationId"
    )
    fun countByLocationId(@Param("locationId") locationId: UUID): Long
    
    /**
     * Tìm sự kiện theo trạng thái và thời gian bắt đầu trong khoảng
     * Dùng để tìm sự kiện sắp diễn ra để gửi thông báo nhắc nhở
     */
    fun findByStatusAndStartDateBetween(
        status: EventStatus, 
        startFrom: LocalDateTime, 
        startTo: LocalDateTime
    ): List<Event>
} 