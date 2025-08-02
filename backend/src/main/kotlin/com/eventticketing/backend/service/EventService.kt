package com.eventticketing.backend.service

import com.eventticketing.backend.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface EventService {

    /**
     * Tạo sự kiện mới
     */
    fun createEvent(eventCreateDto: EventCreateDto, organizerId: UUID): EventDto

    /**
     * Cập nhật thông tin sự kiện
     */
    fun updateEvent(id: UUID, eventUpdateDto: EventUpdateDto, organizerId: UUID): EventDto

    /**
     * Lấy thông tin chi tiết sự kiện theo ID
     */
    fun getEventById(id: UUID): EventDto

    /**
     * Lấy danh sách sự kiện phân trang
     */
    fun getAllEvents(pageable: Pageable): Page<EventDto>

    /**
     * Lấy danh sách sự kiện theo người tổ chức
     */
    fun getEventsByOrganizer(organizerId: UUID, pageable: Pageable): Page<EventDto>

    /**
     * Tìm kiếm sự kiện theo nhiều điều kiện
     */
    fun searchEvents(
        keyword: String?,
        categoryId: UUID?,
        startDate: Date?,
        endDate: Date?,
        locationId: UUID?,
        radius: Double?,
        latitude: Double?,
        longitude: Double?,
        minPrice: Double?,
        maxPrice: Double?,
        status: String?,
        pageable: Pageable
    ): Page<EventDto>

    /**
     * Xóa sự kiện
     */
    fun deleteEvent(id: UUID, organizerId: UUID): Boolean

    /**
     * Công khai sự kiện (chuyển từ draft sang published)
     */
    fun publishEvent(id: UUID, organizerId: UUID): EventDto

    /**
     * Hủy sự kiện (chuyển sang cancelled)
     */
    fun cancelEvent(id: UUID, organizerId: UUID, reason: String): EventDto

    /**
     * Tải lên hình ảnh cho sự kiện
     */
    fun uploadEventImage(id: UUID, image: MultipartFile, isPrimary: Boolean): ImageDto

    /**
     * Lưu thông tin ảnh Cloudinary vào database
     */
    fun saveCloudinaryImage(
        id: UUID, 
        publicId: String, 
        secureUrl: String, 
        width: Int, 
        height: Int, 
        isPrimary: Boolean
    ): ImageDto

    /**
     * Xóa hình ảnh sự kiện
     */
    fun deleteEventImage(id: UUID, imageId: UUID): Boolean

    /**
     * Lấy danh sách hình ảnh của sự kiện
     */
    fun getEventImages(id: UUID): List<ImageDto>

    /**
     * Lấy các sự kiện nổi bật
     */
    fun getFeaturedEvents(limit: Int): List<EventDto>

    /**
     * Lấy các sự kiện gần đây
     */
    fun getUpcomingEvents(limit: Int): List<EventDto>

    /**
     * Lấy các sự kiện theo danh mục
     */
    fun getEventsByCategory(categoryId: UUID, pageable: Pageable): Page<EventDto>

    /**
     * Lấy các sự kiện gần vị trí hiện tại
     */
    fun getNearbyEvents(latitude: Double, longitude: Double, radius: Double, pageable: Pageable): Page<EventDto>
} 