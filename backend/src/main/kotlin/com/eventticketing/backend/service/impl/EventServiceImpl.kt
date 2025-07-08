package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.entity.*
import com.eventticketing.backend.exception.BadRequestException
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.exception.UnauthorizedException
import com.eventticketing.backend.repository.*
import com.eventticketing.backend.service.EventService
import com.eventticketing.backend.util.FileStorageService
import com.eventticketing.backend.util.SecurityUtils
import jakarta.persistence.criteria.Predicate
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class EventServiceImpl(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val locationRepository: LocationRepository,
    private val fileStorageService: FileStorageService,
    private val securityUtils: SecurityUtils
) : EventService {

    private val logger = LoggerFactory.getLogger(EventServiceImpl::class.java)

    @Transactional
    override fun createEvent(eventCreateDto: EventCreateDto, organizerId: UUID): EventDto {
        // Kiểm tra người tổ chức
        val organizer = userRepository.findById(organizerId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với ID $organizerId") }

        // Kiểm tra quyền
        if (!securityUtils.isCurrentUserOrAdmin(organizerId) && organizer.role != UserRole.ORGANIZER) {
            throw UnauthorizedException("Bạn không có quyền tạo sự kiện")
        }

        // Kiểm tra danh mục
        val category = categoryRepository.findById(eventCreateDto.categoryId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy danh mục với ID ${eventCreateDto.categoryId}") }

        // Kiểm tra địa điểm
        val location = locationRepository.findById(eventCreateDto.locationId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy địa điểm với ID ${eventCreateDto.locationId}") }

        // Kiểm tra thời gian
        if (eventCreateDto.startDate.isAfter(eventCreateDto.endDate)) {
            throw BadRequestException("Thời gian bắt đầu phải trước thời gian kết thúc")
        }

        // Tạo sự kiện mới
        val event = Event(
            title = eventCreateDto.title,
            description = eventCreateDto.description,
            shortDescription = eventCreateDto.shortDescription,
            organizer = organizer,
            category = category,
            location = location,
            address = eventCreateDto.address,
            city = eventCreateDto.city,
            latitude = eventCreateDto.latitude,
            longitude = eventCreateDto.longitude,
            maxAttendees = eventCreateDto.maxAttendees,
            startDate = eventCreateDto.startDate,
            endDate = eventCreateDto.endDate,
            isPrivate = eventCreateDto.isPrivate,
            isFree = eventCreateDto.isFree,
            status = if (eventCreateDto.isDraft) EventStatus.DRAFT else EventStatus.PUBLISHED
        )

        // Lưu sự kiện
        val savedEvent = eventRepository.save(event)
        logger.info("Đã tạo sự kiện mới: ${savedEvent.id}")

        return mapToEventDto(savedEvent)
    }

    @Transactional
    override fun updateEvent(id: UUID, eventUpdateDto: EventUpdateDto, organizerId: UUID): EventDto {
        // Kiểm tra sự kiện tồn tại và quyền truy cập
        val event = findEventAndCheckPermission(id, organizerId)

        // Cập nhật thông tin
        eventUpdateDto.title?.let { event.title = it }
        eventUpdateDto.description?.let { event.description = it }
        eventUpdateDto.shortDescription?.let { event.shortDescription = it }
        eventUpdateDto.address?.let { event.address = it }
        eventUpdateDto.city?.let { event.city = it }
        eventUpdateDto.latitude?.let { event.latitude = it }
        eventUpdateDto.longitude?.let { event.longitude = it }
        eventUpdateDto.maxAttendees?.let { event.maxAttendees = it }
        eventUpdateDto.startDate?.let { event.startDate = it }
        eventUpdateDto.endDate?.let { event.endDate = it }
        eventUpdateDto.isPrivate?.let { event.isPrivate = it }
        eventUpdateDto.isFree?.let { event.isFree = it }

        // Kiểm tra và cập nhật danh mục
        if (eventUpdateDto.categoryId != null) {
            val category = categoryRepository.findById(eventUpdateDto.categoryId)
                .orElseThrow { ResourceNotFoundException("Không tìm thấy danh mục với ID ${eventUpdateDto.categoryId}") }
            event.category = category
        }

        // Kiểm tra và cập nhật địa điểm
        if (eventUpdateDto.locationId != null) {
            val location = locationRepository.findById(eventUpdateDto.locationId)
                .orElseThrow { ResourceNotFoundException("Không tìm thấy địa điểm với ID ${eventUpdateDto.locationId}") }
            event.location = location
        }

        // Kiểm tra thời gian
        if (event.startDate.isAfter(event.endDate)) {
            throw BadRequestException("Thời gian bắt đầu phải trước thời gian kết thúc")
        }

        val updatedEvent = eventRepository.save(event)
        logger.info("Đã cập nhật sự kiện: ${updatedEvent.id}")

        return mapToEventDto(updatedEvent)
    }

    override fun getEventById(id: UUID): EventDto {
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy sự kiện với ID $id") }

        // Nếu sự kiện ở trạng thái DRAFT, chỉ cho phép người tổ chức hoặc admin xem
        if (event.status == EventStatus.DRAFT && !securityUtils.isCurrentUserOrAdmin(event.organizer.id!!)) {
            throw UnauthorizedException("Bạn không có quyền xem sự kiện này")
        }

        return mapToEventDto(event)
    }

    override fun getAllEvents(pageable: Pageable): Page<EventDto> {
        val events = if (securityUtils.isAdmin()) {
            // Admin có thể xem tất cả sự kiện
            eventRepository.findAll(pageable)
        } else {
            // Người dùng thông thường chỉ xem được sự kiện đã công bố
            eventRepository.findByStatus(EventStatus.PUBLISHED, pageable)
        }

        return events.map { mapToEventDto(it) }
    }

    override fun getEventsByOrganizer(organizerId: UUID, pageable: Pageable): Page<EventDto> {
        // Kiểm tra xem có phải người tổ chức đang xem sự kiện của chính họ không
        val isOwnEvents = securityUtils.isCurrentUser(organizerId)
        
        val events = if (isOwnEvents || securityUtils.isAdmin()) {
            // Người tổ chức có thể xem tất cả sự kiện của họ
            eventRepository.findByOrganizerId(organizerId, pageable)
        } else {
            // Người dùng thông thường chỉ xem được sự kiện đã công bố
            val spec = Specification<Event> { root, query, criteriaBuilder ->
                val predicates = mutableListOf<Predicate>()
                predicates.add(criteriaBuilder.equal(root.get<UUID>("organizer").get<UUID>("id"), organizerId))
                predicates.add(criteriaBuilder.equal(root.get<EventStatus>("status"), EventStatus.PUBLISHED))
                criteriaBuilder.and(*predicates.toTypedArray())
            }
            eventRepository.findAll(spec, pageable)
        }

        return events.map { mapToEventDto(it) }
    }

    override fun searchEvents(
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
    ): Page<EventDto> {
        val spec = Specification<Event> { root, query, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()
            
            // Mặc định chỉ hiện sự kiện đã công bố, trừ khi là admin
            if (!securityUtils.isAdmin()) {
                predicates.add(criteriaBuilder.equal(root.get<EventStatus>("status"), EventStatus.PUBLISHED))
            } else if (status != null) {
                try {
                    val eventStatus = EventStatus.valueOf(status.uppercase())
                    predicates.add(criteriaBuilder.equal(root.get<EventStatus>("status"), eventStatus))
                } catch (e: IllegalArgumentException) {
                    logger.warn("Trạng thái không hợp lệ: $status")
                }
            }
            
            // Tìm theo từ khóa
            keyword?.let {
                val keywordPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%${it.lowercase()}%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%${it.lowercase()}%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("shortDescription")), "%${it.lowercase()}%")
                )
                predicates.add(keywordPredicate)
            }
            
            // Tìm theo danh mục
            categoryId?.let {
                predicates.add(criteriaBuilder.equal(root.get<Category>("category").get<UUID>("id"), it))
            }
            
            // Tìm theo địa điểm
            locationId?.let {
                predicates.add(criteriaBuilder.equal(root.get<Location>("location").get<UUID>("id"), it))
            }
            
            // Tìm theo thời gian
            startDate?.let {
                val localStartDate = LocalDateTime.ofInstant(it.toInstant(), TimeZone.getDefault().toZoneId())
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), localStartDate))
            }
            
            endDate?.let {
                val localEndDate = LocalDateTime.ofInstant(it.toInstant(), TimeZone.getDefault().toZoneId())
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), localEndDate))
            }
            
            criteriaBuilder.and(*predicates.toTypedArray())
        }
        
        // Với tìm kiếm theo khoảng cách, cần sử dụng native query riêng
        if (latitude != null && longitude != null && radius != null) {
            val eventStatus = if (securityUtils.isAdmin() && status != null) 
                status else EventStatus.PUBLISHED.name
            
            val nearbyEvents = eventRepository.findNearbyEvents(latitude, longitude, radius, eventStatus, pageable)
            return nearbyEvents.map { mapToEventDto(it) }
        }
        
        val events = eventRepository.findAll(spec, pageable)
        return events.map { mapToEventDto(it) }
    }

    @Transactional
    override fun deleteEvent(id: UUID, organizerId: UUID): Boolean {
        // Kiểm tra sự kiện tồn tại và quyền truy cập
        val event = findEventAndCheckPermission(id, organizerId)
        
        // Xóa tất cả hình ảnh trước
        event.images.forEach {
            try {
                fileStorageService.deleteFile(it.url)
            } catch (e: Exception) {
                logger.warn("Không thể xóa file: ${it.url}", e)
            }
        }
        
        eventRepository.delete(event)
        logger.info("Đã xóa sự kiện: $id")
        
        return true
    }

    @Transactional
    override fun publishEvent(id: UUID, organizerId: UUID): EventDto {
        // Kiểm tra sự kiện tồn tại và quyền truy cập
        val event = findEventAndCheckPermission(id, organizerId)
        
        // Kiểm tra trạng thái hiện tại
        if (event.status != EventStatus.DRAFT) {
            throw BadRequestException("Chỉ có thể công bố sự kiện ở trạng thái nháp")
        }
        
        event.status = EventStatus.PUBLISHED
        val publishedEvent = eventRepository.save(event)
        logger.info("Đã công bố sự kiện: $id")
        
        return mapToEventDto(publishedEvent)
    }

    @Transactional
    override fun cancelEvent(id: UUID, organizerId: UUID, reason: String): EventDto {
        // Kiểm tra sự kiện tồn tại và quyền truy cập
        val event = findEventAndCheckPermission(id, organizerId)
        
        // Kiểm tra trạng thái hiện tại
        if (event.status != EventStatus.PUBLISHED) {
            throw BadRequestException("Chỉ có thể hủy sự kiện đã công bố")
        }
        
        event.status = EventStatus.CANCELLED
        event.cancellationReason = reason
        
        val cancelledEvent = eventRepository.save(event)
        logger.info("Đã hủy sự kiện: $id với lý do: $reason")
        
        return mapToEventDto(cancelledEvent)
    }

    @Transactional
    override fun uploadEventImage(id: UUID, image: MultipartFile, isPrimary: Boolean): ImageDto {
        // Kiểm tra sự kiện tồn tại
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy sự kiện với ID $id") }
        
        // Kiểm tra quyền truy cập
        if (!securityUtils.isCurrentUserOrAdmin(event.organizer.id!!)) {
            throw UnauthorizedException("Bạn không có quyền tải lên hình ảnh cho sự kiện này")
        }
        
        // Kiểm tra loại file
        if (!fileStorageService.isImageFile(image)) {
            throw BadRequestException("Chỉ hỗ trợ tải lên file hình ảnh")
        }
        
        // Lưu file
        val filename = fileStorageService.storeFile(image, "events")
        val fileUrl = fileStorageService.getFileUrl(filename, "events")
        
        // Tạo đối tượng EventImage
        val eventImage = EventImage(
            url = fileUrl,
            isPrimary = isPrimary,
            event = event
        )
        
        // Nếu là hình ảnh chính, cập nhật featuredImageUrl của sự kiện
        if (isPrimary) {
            // Đặt tất cả hình ảnh hiện có thành không phải hình chính
            event.images.forEach { it.isPrimary = false }
            
            event.featuredImageUrl = fileUrl
        }
        
        // Thêm hình ảnh vào sự kiện
        event.addImage(eventImage)
        eventRepository.save(event)
        
        logger.info("Đã tải lên hình ảnh cho sự kiện $id: $fileUrl")
        
        return ImageDto(
            id = eventImage.id,
            url = fileUrl,
            eventId = id,
            isPrimary = isPrimary,
            createdAt = eventImage.createdAt
        )
    }

    @Transactional
    override fun deleteEventImage(id: UUID, imageId: UUID): Boolean {
        // Kiểm tra sự kiện tồn tại
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy sự kiện với ID $id") }
        
        // Kiểm tra quyền truy cập
        if (!securityUtils.isCurrentUserOrAdmin(event.organizer.id!!)) {
            throw UnauthorizedException("Bạn không có quyền xóa hình ảnh của sự kiện này")
        }
        
        // Tìm hình ảnh cần xóa
        val imageToRemove = event.images.find { it.id == imageId }
            ?: throw ResourceNotFoundException("Không tìm thấy hình ảnh với ID $imageId cho sự kiện này")
        
        // Xóa file
        try {
            fileStorageService.deleteFile(imageToRemove.url.replace("/api/files/", ""))
        } catch (e: Exception) {
            logger.warn("Không thể xóa file: ${imageToRemove.url}", e)
        }
        
        // Nếu là hình ảnh chính, cập nhật featuredImageUrl của sự kiện
        if (imageToRemove.isPrimary) {
            event.featuredImageUrl = null
        }
        
        // Xóa hình ảnh khỏi danh sách
        event.images.remove(imageToRemove)
        eventRepository.save(event)
        
        logger.info("Đã xóa hình ảnh $imageId của sự kiện $id")
        
        return true
    }

    override fun getEventImages(id: UUID): List<ImageDto> {
        // Kiểm tra sự kiện tồn tại
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy sự kiện với ID $id") }
        
        // Áp dụng quyền truy cập cho sự kiện draft
        if (event.status == EventStatus.DRAFT && !securityUtils.isCurrentUserOrAdmin(event.organizer.id!!)) {
            throw UnauthorizedException("Bạn không có quyền xem hình ảnh của sự kiện này")
        }
        
        // Chuyển đổi sang DTO
        return event.images.map { 
            ImageDto(
                id = it.id,
                url = it.url,
                eventId = id,
                isPrimary = it.isPrimary,
                createdAt = it.createdAt
            )
        }
    }

    override fun getFeaturedEvents(limit: Int): List<EventDto> {
        val pageable = Pageable.ofSize(limit)
        val featuredEvents = eventRepository.findByIsFeaturedTrueAndStatus(EventStatus.PUBLISHED, pageable)
        return featuredEvents.content.map { mapToEventDto(it) }
    }

    override fun getUpcomingEvents(limit: Int): List<EventDto> {
        val now = LocalDateTime.now()
        val pageable = Pageable.ofSize(limit)
        val upcomingEvents = eventRepository.findByStartDateAfterAndStatus(now, EventStatus.PUBLISHED, pageable)
        return upcomingEvents.content.map { mapToEventDto(it) }
    }

    override fun getEventsByCategory(categoryId: UUID, pageable: Pageable): Page<EventDto> {
        // Kiểm tra danh mục tồn tại
        if (!categoryRepository.existsById(categoryId)) {
            throw ResourceNotFoundException("Không tìm thấy danh mục với ID $categoryId")
        }
        
        val events = if (securityUtils.isAdmin()) {
            // Admin xem tất cả sự kiện trong danh mục
            eventRepository.findByCategoryId(categoryId, pageable)
        } else {
            // Người dùng chỉ xem sự kiện đã công bố
            val spec = Specification<Event> { root, query, criteriaBuilder ->
                val predicates = mutableListOf<Predicate>()
                predicates.add(criteriaBuilder.equal(root.get<Category>("category").get<UUID>("id"), categoryId))
                predicates.add(criteriaBuilder.equal(root.get<EventStatus>("status"), EventStatus.PUBLISHED))
                criteriaBuilder.and(*predicates.toTypedArray())
            }
            eventRepository.findAll(spec, pageable)
        }
        
        return events.map { mapToEventDto(it) }
    }

    override fun getNearbyEvents(latitude: Double, longitude: Double, radius: Double, pageable: Pageable): Page<EventDto> {
        val events = eventRepository.findNearbyEvents(
            latitude,
            longitude,
            radius,
            EventStatus.PUBLISHED.name,
            pageable
        )
        
        return events.map { mapToEventDto(it) }
    }

    /**
     * Phương thức hỗ trợ để kiểm tra sự kiện tồn tại và quyền truy cập
     */
    private fun findEventAndCheckPermission(id: UUID, organizerId: UUID): Event {
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy sự kiện với ID $id") }
        
        // Kiểm tra quyền truy cập
        if (event.organizer.id != organizerId && !securityUtils.isAdmin()) {
            throw UnauthorizedException("Bạn không có quyền thao tác với sự kiện này")
        }
        
        return event
    }

    /**
     * Chuyển đổi Event sang EventDto
     */
    private fun mapToEventDto(event: Event): EventDto {
        // Tính giá vé thấp nhất và cao nhất nếu có loại vé
        var minPrice: BigDecimal? = null
        var maxPrice: BigDecimal? = null
        
        if (event.ticketTypes.isNotEmpty()) {
            minPrice = event.ticketTypes.minOfOrNull { it.price }
            maxPrice = event.ticketTypes.maxOfOrNull { it.price }
        }
        
        // Danh sách URL hình ảnh
        val imageUrls = event.images.map { it.url }
        
        return EventDto(
            id = event.id,
            title = event.title,
            description = event.description,
            shortDescription = event.shortDescription,
            organizerId = event.organizer.id!!,
            organizerName = event.organizer.fullName,
            categoryId = event.category.id!!,
            categoryName = event.category.name,
            locationId = event.location.id!!,
            locationName = event.location.name,
            address = event.address,
            city = event.city,
            latitude = event.latitude,
            longitude = event.longitude,
            status = event.status,
            maxAttendees = event.maxAttendees,
            currentAttendees = event.currentAttendees,
            featuredImageUrl = event.featuredImageUrl,
            imageUrls = imageUrls,
            minTicketPrice = minPrice,
            maxTicketPrice = maxPrice,
            startDate = event.startDate,
            endDate = event.endDate,
            createdAt = event.createdAt,
            updatedAt = event.updatedAt,
            isPrivate = event.isPrivate,
            isFeatured = event.isFeatured,
            isFree = event.isFree
        )
    }
} 