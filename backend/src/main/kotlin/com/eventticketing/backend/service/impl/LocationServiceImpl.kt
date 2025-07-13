package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.LocationDto
import com.eventticketing.backend.entity.Location
import com.eventticketing.backend.exception.BadRequestException
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.repository.EventRepository
import com.eventticketing.backend.repository.LocationRepository
import com.eventticketing.backend.service.LocationService
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class LocationServiceImpl(
    private val locationRepository: LocationRepository,
    private val eventRepository: EventRepository
) : LocationService {

    private val logger = LoggerFactory.getLogger(LocationServiceImpl::class.java)

    @Cacheable(value = ["locations"], key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result.isEmpty()")
    override fun getAllLocations(pageable: Pageable): Page<LocationDto> {
        logger.debug("Fetching all locations from database")
        return locationRepository.findAll(pageable).map { mapToLocationDto(it) }
    }

    @Cacheable(value = ["locations"], key = "#id", unless = "#result == null")
    override fun getLocationById(id: UUID): LocationDto {
        logger.debug("Fetching location with ID: $id from database")
        val location = findLocationById(id)
        return mapToLocationDto(location)
    }

    @Transactional
    @CacheEvict(value = ["locations"], allEntries = true)
    override fun createLocation(locationDto: LocationDto): LocationDto {
        // Kiểm tra xem đã tồn tại địa điểm với tên và địa chỉ này chưa
        val existingLocation = locationRepository.findByNameContainingIgnoreCase(locationDto.name, Pageable.ofSize(1))
        if (existingLocation.hasContent() && existingLocation.content.first().address == locationDto.address) {
            throw BadRequestException("Địa điểm với tên và địa chỉ này đã tồn tại")
        }

        val location = Location(
            name = locationDto.name,
            address = locationDto.address,
            city = locationDto.city,
            state = locationDto.state,
            country = locationDto.country,
            latitude = locationDto.latitude,
            longitude = locationDto.longitude,
            description = locationDto.description,
        )

        val savedLocation = locationRepository.save(location)
        logger.info("Đã tạo địa điểm mới: ${savedLocation.id}")
        
        return mapToLocationDto(savedLocation)
    }

    @Transactional
    @Caching(
        evict = [
            CacheEvict(value = ["locations"], key = "#id"),
            CacheEvict(value = ["locations"], allEntries = true)
        ]
    )
    override fun updateLocation(id: UUID, locationDto: LocationDto): LocationDto {
        val location = findLocationById(id)
        
        // Cập nhật thông tin
        location.name = locationDto.name
        location.address = locationDto.address
        location.city = locationDto.city
        location.state = locationDto.state
        location.country = locationDto.country
        location.latitude = locationDto.latitude
        location.longitude = locationDto.longitude
        location.description = locationDto.description
        // Không có trường imageUrl trong LocationDto
        location.updatedAt = LocalDateTime.now()

        val updatedLocation = locationRepository.save(location)
        logger.info("Đã cập nhật địa điểm: $id")
        
        return mapToLocationDto(updatedLocation)
    }

    @Transactional
    @Caching(
        evict = [
            CacheEvict(value = ["locations"], key = "#id"),
            CacheEvict(value = ["locations"], allEntries = true)
        ]
    )
    override fun deleteLocation(id: UUID): Boolean {
        val location = findLocationById(id)
        
        // Kiểm tra xem có sự kiện nào đang sử dụng địa điểm này không
        val eventsCount = eventRepository.countByLocationId(id)
        if (eventsCount > 0) {
            throw BadRequestException("Không thể xóa địa điểm đang được sử dụng bởi $eventsCount sự kiện")
        }
        
        locationRepository.delete(location)
        logger.info("Đã xóa địa điểm: $id")
        
        return true
    }

    override fun searchLocationsByName(name: String, pageable: Pageable): Page<LocationDto> {
        return locationRepository.findByNameContainingIgnoreCase(name, pageable)
            .map { mapToLocationDto(it) }
    }

    override fun getLocationsByCity(city: String, pageable: Pageable): Page<LocationDto> {
        return locationRepository.findByCityIgnoreCase(city, pageable)
            .map { mapToLocationDto(it) }
    }

    override fun getNearbyLocations(
        latitude: Double, 
        longitude: Double, 
        radius: Double,
        pageable: Pageable
    ): Page<LocationDto> {
        // Kiểm tra tọa độ và bán kính hợp lệ
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw BadRequestException("Tọa độ không hợp lệ")
        }
        
        if (radius <= 0 || radius > 1000) {
            throw BadRequestException("Bán kính phải lớn hơn 0 và nhỏ hơn 1000km")
        }
        
        return locationRepository.findNearbyLocations(latitude, longitude, radius, pageable)
            .map { mapToLocationDto(it) }
    }

    override fun getPopularLocations(limit: Int): List<LocationDto> {
        if (limit <= 0 || limit > 100) {
            throw BadRequestException("Số lượng địa điểm phải từ 1 đến 100")
        }
        
        val pageable = Pageable.ofSize(limit)
        return locationRepository.findPopularLocations(pageable)
            .map { mapToLocationDto(it) }
    }

    /**
     * Tìm Location theo ID
     */
    private fun findLocationById(id: UUID): Location {
        return locationRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy địa điểm với ID $id") }
    }

    /**
     * Chuyển đổi Location thành LocationDto
     */
    private fun mapToLocationDto(location: Location): LocationDto {
        return LocationDto(
            id = location.id,
            name = location.name,
            address = location.address,
            city = location.city,
            state = location.state,
            country = location.country,
            latitude = location.latitude,
            longitude = location.longitude,
            description = location.description,
            createdAt = location.createdAt,
            updatedAt = location.updatedAt
        )
    }
} 