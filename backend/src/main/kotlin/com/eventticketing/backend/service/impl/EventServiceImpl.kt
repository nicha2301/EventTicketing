package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.entity.*
import com.eventticketing.backend.exception.BadRequestException
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.exception.UnauthorizedException
import com.eventticketing.backend.repository.*
import com.eventticketing.backend.service.EventService
import com.eventticketing.backend.util.FileStorageService
import com.eventticketing.backend.service.CloudinaryStorageService
import com.eventticketing.backend.entity.StorageProvider
import com.eventticketing.backend.util.SecurityUtils
import jakarta.persistence.criteria.Predicate
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
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
    private val cloudinaryStorageService: CloudinaryStorageService,
    private val securityUtils: SecurityUtils
) : EventService {

    @Transactional
    @Caching(evict = [
        CacheEvict(value = ["events"], allEntries = true),
        CacheEvict(value = ["featuredEvents"], allEntries = true),
        CacheEvict(value = ["upcomingEvents"], allEntries = true)
    ])
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

        return mapToEventDto(savedEvent)
    }

    @Transactional
    override fun createEventWithMultipartImages(
        eventCreateDto: EventCreateDto,
        images: List<MultipartFile>,
        primaryImageIndex: Int?,
        organizerId: UUID
    ): EventDto {
        // Validate img
        if (images.isEmpty()) {
            throw BadRequestException("Ít nhất một ảnh phải được upload")
        }
        if (images.size > 10) {
            throw BadRequestException("Tối đa 10 ảnh có thể được upload")
        }
        
        // Validate primary image index
        primaryImageIndex?.let { index ->
            if (index < 0 || index >= images.size) {
                throw BadRequestException("Primary image index phải trong khoảng 0 đến ${images.size - 1}")
            }
        }
        
        val organizer = userRepository.findById(organizerId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy người tổ chức với ID $organizerId") }

        if (!securityUtils.isCurrentUserOrAdmin(organizerId) && organizer.role != UserRole.ORGANIZER) {
            throw UnauthorizedException("Bạn không có quyền tạo sự kiện")
        }

        val category = categoryRepository.findById(eventCreateDto.categoryId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy danh mục với ID ${eventCreateDto.categoryId}") }

        val location = locationRepository.findById(eventCreateDto.locationId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy địa điểm với ID ${eventCreateDto.locationId}") }

        if (eventCreateDto.startDate.isAfter(eventCreateDto.endDate)) {
            throw BadRequestException("Thời gian bắt đầu phải trước thời gian kết thúc")
        }

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

        val savedEvent = eventRepository.save(event)
        
        val uploadedImages = mutableListOf<EventImage>()
        
        images.forEachIndexed { index, imageFile ->
            try {
                val isPrimary = primaryImageIndex == index
                val uploadResult = cloudinaryStorageService.uploadImage(imageFile, "events")
                
                if (!uploadResult.success) {
                    throw RuntimeException("Không thể tải lên ảnh ${index + 1}: ${uploadResult.error}")
                }
                
                val eventImage = EventImage(
                    event = savedEvent,
                    url = uploadResult.cloudinaryUrl,
                    cloudinaryPublicId = uploadResult.cloudinaryPublicId,
                    cloudinaryUrl = uploadResult.cloudinaryUrl,
                    thumbnailUrl = uploadResult.thumbnailUrl,
                    mediumUrl = uploadResult.mediumUrl,
                    storageProvider = StorageProvider.CLOUDINARY,
                    isPrimary = isPrimary
                )
                
                uploadedImages.add(eventImage)
                savedEvent.addImage(eventImage)
                
            } catch (e: Exception) {
                uploadedImages.forEach { img ->
                    img.cloudinaryPublicId?.let { cloudinaryStorageService.deleteImage(img) }
                }
                throw BadRequestException("Lỗi upload ảnh ${index + 1}: ${e.message}")
            }
        }
        
        val primaryImages = savedEvent.images.filter { it.isPrimary }
        if (primaryImages.isEmpty() && savedEvent.images.isNotEmpty()) {
            savedEvent.images.first().isPrimary = true
        } else if (primaryImages.size > 1) {
            primaryImages.drop(1).forEach { it.isPrimary = false }
        }
        
        val finalEvent = eventRepository.save(savedEvent)
        return mapToEventDto(finalEvent)
    }

    @Transactional
    @Caching(evict = [
        CacheEvict(value = ["eventDetails"], key = "#id"),
        CacheEvict(value = ["events"], allEntries = true),
        CacheEvict(value = ["featuredEvents"], allEntries = true),
        CacheEvict(value = ["upcomingEvents"], allEntries = true)
    ])
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

        return mapToEventDto(updatedEvent)
    }

    @Cacheable(value = ["eventDetails"], key = "#id", unless = "#result == null")
    override fun getEventById(id: UUID): EventDto {
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy sự kiện với ID $id") }

        // Nếu sự kiện ở trạng thái DRAFT, chỉ cho phép người tổ chức hoặc admin xem
        if (event.status == EventStatus.DRAFT && !securityUtils.isCurrentUserOrAdmin(event.organizer.id!!)) {
            throw UnauthorizedException("Bạn không có quyền xem sự kiện này")
        }

        return mapToEventDto(event)
    }

    @Cacheable(value = ["events"], key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result.isEmpty()")
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
        val isOwnEvents = securityUtils.isCurrentUser(organizerId)
        
        val events = if (isOwnEvents || securityUtils.isAdmin()) {
            eventRepository.findByOrganizerId(organizerId, pageable)
        } else {
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
            
            if (!securityUtils.isAdmin()) {
                predicates.add(criteriaBuilder.equal(root.get<EventStatus>("status"), EventStatus.PUBLISHED))
            } else if (status != null) {
                try {
                    val eventStatus = EventStatus.valueOf(status.uppercase())
                    predicates.add(criteriaBuilder.equal(root.get<EventStatus>("status"), eventStatus))
                } catch (e: IllegalArgumentException) {
                }
            }

            // Tìm theo từ khóa
            if (!keyword.isNullOrEmpty()) {
                val keywordLower = keyword.lowercase()
                val titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%$keywordLower%"
                )
                val descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%$keywordLower%"
                )
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate))
            }

            // Tìm theo danh mục
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get<Category>("category").get<UUID>("id"), categoryId))
            }

            // Tìm theo thời gian
            if (startDate != null) {
                val startLocalDateTime = startDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startLocalDateTime))
            }

            if (endDate != null) {
                val endLocalDateTime = endDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endLocalDateTime))
            }

            // Tìm theo địa điểm
            if (locationId != null) {
                predicates.add(criteriaBuilder.equal(root.get<Location>("location").get<UUID>("id"), locationId))
            }

            // Tìm theo giá
            if (minPrice != null) {
                // Tìm các sự kiện có ít nhất một loại vé có giá >= minPrice
                // Đây là một truy vấn phức tạp hơn, có thể cần join với bảng TicketType
                // Giả sử có một join với bảng TicketType
                val subquery = query!!.subquery(BigDecimal::class.java)
                val ticketTypeRoot = subquery.from(TicketType::class.java)
                subquery.select(ticketTypeRoot.get("price"))
                subquery.where(criteriaBuilder.equal(ticketTypeRoot.get<Event>("event"), root))
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    subquery.selection as jakarta.persistence.criteria.Expression<BigDecimal>,
                    BigDecimal.valueOf(minPrice)
                ))
            }

            if (maxPrice != null) {
                // Tương tự như trên
                val subquery = query!!.subquery(BigDecimal::class.java)
                val ticketTypeRoot = subquery.from(TicketType::class.java)
                subquery.select(ticketTypeRoot.get("price"))
                subquery.where(criteriaBuilder.equal(ticketTypeRoot.get<Event>("event"), root))
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    subquery.selection as jakarta.persistence.criteria.Expression<BigDecimal>,
                    BigDecimal.valueOf(maxPrice)
                ))
            }

            // Tìm theo bán kính
            if (latitude != null && longitude != null && radius != null) {
                // Tính toán bán kính dựa trên công thức Haversine
                // Đây là một tính toán phức tạp, có thể cần sử dụng native query hoặc function trong database
                // Giả sử database hỗ trợ tính khoảng cách địa lý
                // Ví dụ với PostgreSQL và extension PostGIS
                // Đây chỉ là giả lập logic, cần thay thế bằng native query thực tế
                val latPath = root.get<Double>("latitude")
                val lonPath = root.get<Double>("longitude")
                
                // Haversine formula
                val radiusInMeters = radius * 1000 // Convert km to meters
                val earthRadius = 6371000.0 // Earth radius in meters
                
                // Đây chỉ là mô phỏng, cần thay thế bằng native query thực tế
                val haversine = criteriaBuilder.function(
                    "haversine",
                    Double::class.java,
                    latPath,
                    lonPath,
                    criteriaBuilder.literal(latitude),
                    criteriaBuilder.literal(longitude)
                )
                
                predicates.add(criteriaBuilder.lessThanOrEqualTo(haversine, radiusInMeters))
            }

            criteriaBuilder.and(*predicates.toTypedArray())
        }

        val events = eventRepository.findAll(spec, pageable)
        return events.map { mapToEventDto(it) }
    }

    @Transactional
    @Caching(evict = [
        CacheEvict(value = ["eventDetails"], key = "#id"),
        CacheEvict(value = ["events"], allEntries = true),
        CacheEvict(value = ["featuredEvents"], allEntries = true),
        CacheEvict(value = ["upcomingEvents"], allEntries = true)
    ])
    override fun deleteEvent(id: UUID, organizerId: UUID): Boolean {
        val event = findEventAndCheckPermission(id, organizerId)
        
        if (hasTicketsSold(event)) {
            throw BadRequestException("Không thể xóa sự kiện đã có vé được bán")
        }
        
        eventRepository.delete(event)
        
        return true
    }

    @Transactional
    @Caching(evict = [
        CacheEvict(value = ["eventDetails"], key = "#id"),
        CacheEvict(value = ["events"], allEntries = true),
        CacheEvict(value = ["featuredEvents"], allEntries = true),
        CacheEvict(value = ["upcomingEvents"], allEntries = true)
    ])
    override fun publishEvent(id: UUID, organizerId: UUID): EventDto {
        val event = findEventAndCheckPermission(id, organizerId)
        
        if (event.status != EventStatus.DRAFT) {
            throw BadRequestException("Chỉ có thể công bố sự kiện đang ở trạng thái nháp")
        }
        
        event.status = EventStatus.PUBLISHED
        val publishedEvent = eventRepository.save(event)
        
        return mapToEventDto(publishedEvent)
    }

    @Transactional
    @Caching(evict = [
        CacheEvict(value = ["eventDetails"], key = "#id"),
        CacheEvict(value = ["events"], allEntries = true),
        CacheEvict(value = ["featuredEvents"], allEntries = true),
        CacheEvict(value = ["upcomingEvents"], allEntries = true)
    ])
    override fun cancelEvent(id: UUID, organizerId: UUID, reason: String): EventDto {
        val event = findEventAndCheckPermission(id, organizerId)
        
        if (event.status == EventStatus.CANCELLED) {
            throw BadRequestException("Sự kiện đã bị hủy trước đó")
        }
        
        event.status = EventStatus.CANCELLED
        event.cancellationReason = reason
        val cancelledEvent = eventRepository.save(event)
        
        
        return mapToEventDto(cancelledEvent)
    }

    @Transactional
    @CacheEvict(value = ["eventDetails"], key = "#id")
    override fun uploadEventImage(id: UUID, image: MultipartFile, isPrimary: Boolean): ImageDto {
        val event = findEventAndCheckPermission(id, null)
        
        val uploadResult = cloudinaryStorageService.uploadImage(image, "events")
        
        if (!uploadResult.success) {
            throw RuntimeException("Không thể tải lên hình ảnh: ${uploadResult.error}")
        }
        
        val eventImage = EventImage(
            event = event,
            url = uploadResult.cloudinaryUrl, 
            cloudinaryPublicId = uploadResult.cloudinaryPublicId,
            cloudinaryUrl = uploadResult.cloudinaryUrl,
            thumbnailUrl = uploadResult.thumbnailUrl,
            mediumUrl = uploadResult.mediumUrl,
            storageProvider = StorageProvider.CLOUDINARY, 
            isPrimary = isPrimary
        )
        
        if (isPrimary) {
            event.images.forEach { it.isPrimary = false }
        }
        
        event.addImage(eventImage)
        
        val savedEvent = eventRepository.save(event)
        
        val savedImage = savedEvent.images.find { 
            it.cloudinaryPublicId == uploadResult.cloudinaryPublicId
        } ?: throw RuntimeException("Không thể lưu hình ảnh sự kiện")
        
        val finalUrl = cloudinaryStorageService.getImageUrl(savedImage)
        
        return ImageDto(
            id = savedImage.id,
            url = finalUrl,
            eventId = id,
            isPrimary = savedImage.isPrimary,
            createdAt = savedImage.createdAt,
            width = savedImage.width,
            height = savedImage.height
        )
    }

    @Transactional
    @CacheEvict(value = ["eventDetails"], key = "#id")
    override fun saveCloudinaryImage(
        id: UUID, 
        publicId: String, 
        secureUrl: String, 
        width: Int, 
        height: Int, 
        isPrimary: Boolean
    ): ImageDto {
        val event = findEventAndCheckPermission(id, null)
        
        val baseUrl = secureUrl.substringBeforeLast("/") + "/"
        val fileName = secureUrl.substringAfterLast("/")
        val thumbnailUrl = "${baseUrl}c_thumb,w_300,h_300/${fileName}"
        val mediumUrl = "${baseUrl}c_scale,w_800/${fileName}"
        
        val eventImage = EventImage(
            event = event,
            url = secureUrl,
            cloudinaryPublicId = publicId,
            cloudinaryUrl = secureUrl,
            thumbnailUrl = thumbnailUrl,
            mediumUrl = mediumUrl,
            storageProvider = StorageProvider.CLOUDINARY,
            isPrimary = isPrimary,
            width = width,
            height = height
        )
        
        if (isPrimary) {
            event.images.forEach { it.isPrimary = false }
        }
        
        event.addImage(eventImage)
        
        val savedEvent = eventRepository.save(event)
        
        val savedImage = savedEvent.images.find { 
            it.cloudinaryPublicId == publicId
        } ?: throw RuntimeException("Không thể lưu thông tin ảnh Cloudinary")
        
        val finalUrl = cloudinaryStorageService.getImageUrl(savedImage)
        
        return ImageDto(
            id = savedImage.id,
            url = finalUrl,
            eventId = id,
            isPrimary = savedImage.isPrimary,
            createdAt = savedImage.createdAt,
            width = savedImage.width,
            height = savedImage.height
        )
    }

    @Transactional
    @CacheEvict(value = ["eventDetails"], key = "#id")
    override fun deleteEventImage(id: UUID, imageId: UUID): Boolean {
        val event = findEventAndCheckPermission(id, null)
        
        val imageToDelete = event.images.find { it.id == imageId }
            ?: throw ResourceNotFoundException("Không tìm thấy ảnh với ID $imageId cho sự kiện $id")
        
        val deleteSuccess = cloudinaryStorageService.deleteImage(imageToDelete)
        
        if (deleteSuccess) {
            event.images.removeIf { it.id == imageId }
            
            eventRepository.save(event)
        } else {
            event.images.removeIf { it.id == imageId }
            eventRepository.save(event)
        }
        
        return true
    }

    override fun getEventImages(id: UUID): List<ImageDto> {
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy sự kiện với ID $id") }
        
        return event.images.map {
            ImageDto(
                id = it.id!!,
                url = cloudinaryStorageService.getImageUrl(it),
                eventId = id,
                isPrimary = it.isPrimary,
                createdAt = it.createdAt
            )
        }
    }

    @Cacheable(value = ["featuredEvents"], key = "#limit", unless = "#result.isEmpty()")
    override fun getFeaturedEvents(limit: Int): List<EventDto> {
        // Lấy các sự kiện nổi bật (có đánh dấu isFeatured)
        val pageable = org.springframework.data.domain.PageRequest.of(0, limit)
        val featuredEvents = eventRepository.findByIsFeaturedTrueAndStatus(EventStatus.PUBLISHED, pageable)
        return featuredEvents.content.map { mapToEventDto(it) }
    }

    @Cacheable(value = ["upcomingEvents"], key = "#limit", unless = "#result.isEmpty()")
    override fun getUpcomingEvents(limit: Int): List<EventDto> {
        val now = LocalDateTime.now()
        val pageable = org.springframework.data.domain.PageRequest.of(0, limit)
        val upcomingEvents = eventRepository.findByStartDateAfterAndStatus(now, EventStatus.PUBLISHED, pageable)
        return upcomingEvents.content.map { mapToEventDto(it) }
    }

    override fun getEventsByCategory(categoryId: UUID, pageable: Pageable): Page<EventDto> {
        // Sử dụng specification để tìm theo categoryId và status
        val spec = Specification<Event> { root, query, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()
            predicates.add(criteriaBuilder.equal(root.get<Category>("category").get<UUID>("id"), categoryId))
            predicates.add(criteriaBuilder.equal(root.get<EventStatus>("status"), EventStatus.PUBLISHED))
            criteriaBuilder.and(*predicates.toTypedArray())
        }
        
        val events = eventRepository.findAll(spec, pageable)
        return events.map { mapToEventDto(it) }
    }

    override fun getNearbyEvents(latitude: Double, longitude: Double, radius: Double, pageable: Pageable): Page<EventDto> {
        // Sử dụng phương thức findNearbyEvents với status đã được chuyển thành String
        val statusStr = EventStatus.PUBLISHED.name
        val nearbyEvents = eventRepository.findNearbyEvents(latitude, longitude, radius, statusStr, pageable)
        return nearbyEvents.map { mapToEventDto(it) }
    }

    // Helper methods
    
    private fun findEventAndCheckPermission(id: UUID, organizerId: UUID?): Event {
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy sự kiện với ID $id") }
        
        // Nếu organizerId được cung cấp, kiểm tra quyền
        if (organizerId != null && event.organizer.id != organizerId && !securityUtils.isAdmin()) {
            throw UnauthorizedException("Bạn không có quyền thực hiện thao tác này")
        }
        
        return event
    }
    
    private fun hasTicketsSold(event: Event): Boolean {
        return false // Placeholder
    }
    
    private fun mapToEventDto(event: Event): EventDto {
        // Find primary image URL using Cloudinary storage
        val primaryImage = event.images.find { it.isPrimary }?.let { 
            cloudinaryStorageService.getImageUrl(it) 
        }
        
        // List of image URLs using Cloudinary storage
        val imageUrls = event.images.map { cloudinaryStorageService.getImageUrl(it) }
        
        // Calculate min and max ticket prices
        val ticketPrices = event.ticketTypes.map { it.price }
        val minTicketPrice = if (ticketPrices.isNotEmpty()) ticketPrices.minOrNull() else null
        val maxTicketPrice = if (ticketPrices.isNotEmpty()) ticketPrices.maxOrNull() else null
        
        // Chuyển đổi danh sách loại vé sang DTO
        val ticketTypeDtos = event.ticketTypes.map { ticketType ->
            TicketTypeDto(
                id = ticketType.id,
                name = ticketType.name,
                description = ticketType.description,
                price = ticketType.price,
                quantity = ticketType.quantity,
                availableQuantity = ticketType.availableQuantity,
                quantitySold = ticketType.quantitySold,
                eventId = event.id,
                salesStartDate = ticketType.salesStartDate,
                salesEndDate = ticketType.salesEndDate,
                maxTicketsPerCustomer = ticketType.maxTicketsPerCustomer,
                minTicketsPerOrder = ticketType.minTicketsPerOrder,
                isEarlyBird = ticketType.isEarlyBird,
                isVIP = ticketType.isVIP,
                isActive = ticketType.isActive
            )
        }
        
        return EventDto(
            id = event.id!!,
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
            maxAttendees = event.maxAttendees,
            currentAttendees = event.currentAttendees,
            startDate = event.startDate,
            endDate = event.endDate,
            isPrivate = event.isPrivate,
            isFree = event.isFree,
            isFeatured = event.isFeatured,
            status = event.status,
            featuredImageUrl = event.featuredImageUrl ?: primaryImage,
            imageUrls = imageUrls,
            minTicketPrice = minTicketPrice,
            maxTicketPrice = maxTicketPrice,
            createdAt = event.createdAt,
            updatedAt = event.updatedAt,
            ticketTypes = ticketTypeDtos
        )
    }
} 