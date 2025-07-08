package com.eventticketing.backend.service

import com.eventticketing.backend.dto.LocationDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface LocationService {
    /**
     * Lấy tất cả địa điểm
     */
    fun getAllLocations(pageable: Pageable): Page<LocationDto>
    
    /**
     * Lấy thông tin địa điểm theo ID
     */
    fun getLocationById(id: UUID): LocationDto
    
    /**
     * Tạo địa điểm mới
     */
    fun createLocation(locationDto: LocationDto): LocationDto
    
    /**
     * Cập nhật địa điểm
     */
    fun updateLocation(id: UUID, locationDto: LocationDto): LocationDto
    
    /**
     * Xóa địa điểm
     */
    fun deleteLocation(id: UUID): Boolean
    
    /**
     * Tìm kiếm địa điểm theo tên
     */
    fun searchLocationsByName(name: String, pageable: Pageable): Page<LocationDto>
    
    /**
     * Lọc địa điểm theo thành phố
     */
    fun getLocationsByCity(city: String, pageable: Pageable): Page<LocationDto>
    
    /**
     * Tìm các địa điểm gần đây theo tọa độ và bán kính
     */
    fun getNearbyLocations(latitude: Double, longitude: Double, radius: Double, pageable: Pageable): Page<LocationDto>
    
    /**
     * Lấy các địa điểm phổ biến (có nhiều sự kiện)
     */
    fun getPopularLocations(limit: Int): List<LocationDto>
} 