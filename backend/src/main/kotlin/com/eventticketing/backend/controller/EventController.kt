package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.service.EventService
import com.eventticketing.backend.util.SecurityUtils
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.charset.StandardCharsets
import java.util.*
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/events")
class EventController(
    private val eventService: EventService,
    private val securityUtils: SecurityUtils
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun createEvent(@Valid @RequestBody eventCreateDto: EventCreateDto): ResponseEntity<ApiResponse<EventDto>> {
        val currentUser = securityUtils.getCurrentUser()
        val createdEvent = eventService.createEvent(eventCreateDto, currentUser?.id!!)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                "Đã tạo sự kiện thành công",
                createdEvent
            )
        )
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun updateEvent(
        @PathVariable id: UUID,
        @Valid @RequestBody eventUpdateDto: EventUpdateDto
    ): ResponseEntity<ApiResponse<EventDto>> {
        val currentUser = securityUtils.getCurrentUser()
        val updatedEvent = eventService.updateEvent(id, eventUpdateDto, currentUser?.id!!)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã cập nhật sự kiện thành công",
                updatedEvent
            )
        )
    }

    @GetMapping("/{id}")
    fun getEventById(@PathVariable id: UUID): ResponseEntity<ApiResponse<EventDto>> {
        val event = eventService.getEventById(id)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy thông tin sự kiện thành công",
                event
            )
        )
    }

    @GetMapping
    fun getAllEvents(
        @PageableDefault(size = 20, sort = ["startDate"]) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<EventDto>>> {
        val events = eventService.getAllEvents(pageable)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách sự kiện thành công",
                events
            )
        )
    }

    @GetMapping("/organizer/{organizerId}")
    fun getEventsByOrganizer(
        @PathVariable organizerId: UUID,
        @PageableDefault(size = 20, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<EventDto>>> {
        val events = eventService.getEventsByOrganizer(organizerId, pageable)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách sự kiện của người tổ chức thành công",
                events
            )
        )
    }

    @GetMapping("/search")
    fun searchEvents(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) categoryId: UUID?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: Date?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: Date?,
        @RequestParam(required = false) locationId: UUID?,
        @RequestParam(required = false) radius: Double?,
        @RequestParam(required = false) latitude: Double?,
        @RequestParam(required = false) longitude: Double?,
        @RequestParam(required = false) minPrice: Double?,
        @RequestParam(required = false) maxPrice: Double?,
        @RequestParam(required = false) status: String?,
        @PageableDefault(size = 20, sort = ["startDate"]) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<EventDto>>> {
        val events = eventService.searchEvents(
            keyword, categoryId, startDate, endDate, locationId,
            radius, latitude, longitude, minPrice, maxPrice, status, pageable
        )
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Tìm kiếm sự kiện thành công",
                events
            )
        )
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun deleteEvent(@PathVariable id: UUID): ResponseEntity<ApiResponse<Boolean>> {
        val currentUser = securityUtils.getCurrentUser()
        val result = eventService.deleteEvent(id, currentUser?.id!!)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã xóa sự kiện thành công",
                result
            )
        )
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun publishEvent(@PathVariable id: UUID): ResponseEntity<ApiResponse<EventDto>> {
        val currentUser = securityUtils.getCurrentUser()
        val publishedEvent = eventService.publishEvent(id, currentUser?.id!!)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã công bố sự kiện thành công",
                publishedEvent
            )
        )
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun cancelEvent(
        @PathVariable id: UUID,
        @RequestBody cancelDto: EventCancelDto
    ): ResponseEntity<ApiResponse<EventDto>> {
        val currentUser = securityUtils.getCurrentUser()
        val cancelledEvent = eventService.cancelEvent(id, currentUser?.id!!, cancelDto.reason)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã hủy sự kiện thành công",
                cancelledEvent
            )
        )
    }

    @PostMapping("/{id}/images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun uploadEventImage(
        @PathVariable id: UUID,
        @RequestParam("image") image: MultipartFile,
        @RequestParam(value = "isPrimary", defaultValue = "false") isPrimary: Boolean
    ): ResponseEntity<ApiResponse<ImageDto>> {
        val uploadedImage = eventService.uploadEventImage(id, image, isPrimary)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                "Đã tải lên hình ảnh sự kiện thành công",
                uploadedImage
            )
        )
    }

    @PostMapping("/{id}/images/cloudinary")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun saveCloudinaryImage(
        @PathVariable id: UUID,
        @RequestBody cloudinaryInfo: CloudinaryImageRequest
    ): ResponseEntity<ApiResponse<ImageDto>> {
        val savedImage = eventService.saveCloudinaryImage(
            id, 
            cloudinaryInfo.publicId,
            cloudinaryInfo.secureUrl,
            cloudinaryInfo.width,
            cloudinaryInfo.height,
            cloudinaryInfo.isPrimary
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                "Đã lưu thông tin ảnh Cloudinary thành công",
                savedImage
            )
        )
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun deleteEventImage(
        @PathVariable id: UUID,
        @PathVariable imageId: UUID
    ): ResponseEntity<ApiResponse<Boolean>> {
        val result = eventService.deleteEventImage(id, imageId)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã xóa hình ảnh sự kiện thành công",
                result
            )
        )
    }

    @GetMapping("/{id}/images")
    fun getEventImages(@PathVariable id: UUID): ResponseEntity<ApiResponse<List<ImageDto>>> {
        val images = eventService.getEventImages(id)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách hình ảnh sự kiện thành công",
                images
            )
        )
    }

    @GetMapping("/featured")
    fun getFeaturedEvents(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<ApiResponse<List<EventDto>>> {
        val featuredEvents = eventService.getFeaturedEvents(limit)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách sự kiện nổi bật thành công",
                featuredEvents
            )
        )
    }

    @GetMapping("/upcoming")
    fun getUpcomingEvents(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<ApiResponse<List<EventDto>>> {
        val upcomingEvents = eventService.getUpcomingEvents(limit)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách sự kiện sắp diễn ra thành công",
                upcomingEvents
            )
        )
    }

    @GetMapping("/category/{categoryId}")
    fun getEventsByCategory(
        @PathVariable categoryId: UUID,
        @PageableDefault(size = 20, sort = ["startDate"]) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<EventDto>>> {
        val events = eventService.getEventsByCategory(categoryId, pageable)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách sự kiện theo danh mục thành công",
                events
            )
        )
    }

    @GetMapping("/nearby")
    fun getNearbyEvents(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam(defaultValue = "10.0") radius: Double,
        @PageableDefault(size = 20, sort = ["startDate"]) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<EventDto>>> {
        val events = eventService.getNearbyEvents(latitude, longitude, radius, pageable)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách sự kiện gần đây thành công",
                events
            )
        )
    }
} 