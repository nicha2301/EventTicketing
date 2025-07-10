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
    private val securityUtils: SecurityUtils
) : EventService {

    private val logger = LoggerFactory.getLogger(EventServiceImpl::class.java)

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
        logger.info("Đã tạo sự kiện mới: ${savedEvent.id}")

        return mapToEventDto(savedEvent)
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
        logger.info("Đã cập nhật sự kiện: ${updatedEvent.id}")

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
                    // Bỏ qua nếu status không hợp lệ
                    logger.warn("Trạng thái không hợp lệ: $status")
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
        
        // Kiểm tra xem sự kiện có thể xóa không (ví dụ: chưa có vé nào được bán)
        // Giả sử có một phương thức kiểm tra
        if (hasTicketsSold(event)) {
            throw BadRequestException("Không thể xóa sự kiện đã có vé được bán")
        }
        
        eventRepository.delete(event)
        logger.info("Đã xóa sự kiện: $id")
        
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
        logger.info("Đã công bố sự kiện: $id")
        
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
        logger.info("Đã hủy sự kiện: $id, lý do: $reason")
        
        // Gửi thông báo cho người đã mua vé
        // notificationService.notifyCancelledEvent(event, reason)
        
        return mapToEventDto(cancelledEvent)
    }

    @Transactional
    @CacheEvict(value = ["eventDetails"], key = "#id")
    override fun uploadEventImage(id: UUID, image: MultipartFile, isPrimary: Boolean): ImageDto {
        val event = findEventAndCheckPermission(id, null)
        
        // Lưu file và lấy URL
        val fileName = "${UUID.randomUUID()}-${image.originalFilename}"
        val subfolder = "events/$id"
        val filePath = fileStorageService.storeFile(image, subfolder)
        
        // Tạo đối tượng Image
        val eventImage = EventImage(
            event = event,
            url = filePath,
            isPrimary = isPrimary
        )
        
        // Nếu là ảnh chính, cập nhật các ảnh khác thành không phải ảnh chính
        if (isPrimary) {
            event.images.forEach { it.isPrimary = false }
        }
        
        // Thêm ảnh mới vào danh sách ảnh của sự kiện
        event.images.add(eventImage)
        
        // Lưu sự kiện
        eventRepository.save(event)
        logger.info("Đã tải lên ảnh cho sự kiện: $id, isPrimary: $isPrimary")
        
        return ImageDto(
            id = eventImage.id!!,
            url = eventImage.url,
            isPrimary = eventImage.isPrimary
        )
    }

    @Transactional
    @CacheEvict(value = ["eventDetails"], key = "#id")
    override fun deleteEventImage(id: UUID, imageId: UUID): Boolean {
        val event = findEventAndCheckPermission(id, null)
        
        // Tìm ảnh cần xóa
            val imageToDelete = event.images.find { it.id == imageId }
            ?: throw ResourceNotFoundException("Không tìm thấy ảnh với ID $imageId cho sự kiện $id")
        
        // Xóa file từ storage
        fileStorageService.deleteFile(imageToDelete.url)
        
        // Xóa ảnh khỏi danh sách
        event.images.removeIf { it.id == imageId }
        
        // Lưu sự kiện
        eventRepository.save(event)
        logger.info("Đã xóa ảnh $imageId của sự kiện: $id")
        
        return true
    }

    override fun getEventImages(id: UUID): List<ImageDto> {
        val event = eventRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy sự kiện với ID $id") }
        
        return event.images.map {
            ImageDto(
                id = it.id!!,
                url = it.url,
                isPrimary = it.isPrimary
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
        // Kiểm tra xem sự kiện đã có vé nào được bán chưa
        // Giả sử có một repository method để kiểm tra
        return false // Placeholder
    }
    
    private fun mapToEventDto(event: Event): EventDto {
        // Tìm ảnh chính của sự kiện
        val primaryImage = event.images.find { it.isPrimary }?.url
        
        // Danh sách URL hình ảnh
        val imageUrls = event.images.map { it.url }
        
        // Tính giá vé thấp nhất và cao nhất
        val ticketPrices = event.ticketTypes.map { it.price }
        val minTicketPrice = if (ticketPrices.isNotEmpty()) ticketPrices.minOrNull() else null
        val maxTicketPrice = if (ticketPrices.isNotEmpty()) ticketPrices.maxOrNull() else null
        
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
            updatedAt = event.updatedAt
        )
    }
} 