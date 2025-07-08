package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.dto.LocationDto
import com.eventticketing.backend.service.LocationService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/locations")
class LocationController(
    private val locationService: LocationService
) {

    @GetMapping
    fun getAllLocations(
        @PageableDefault(size = 20, sort = ["name"], direction = Sort.Direction.ASC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<LocationDto>>> {
        val locations = locationService.getAllLocations(pageable)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách địa điểm thành công",
                locations
            )
        )
    }

    @GetMapping("/{id}")
    fun getLocationById(@PathVariable id: UUID): ResponseEntity<ApiResponse<LocationDto>> {
        val location = locationService.getLocationById(id)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy thông tin địa điểm thành công",
                location
            )
        )
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun createLocation(@Valid @RequestBody locationDto: LocationDto): ResponseEntity<ApiResponse<LocationDto>> {
        val createdLocation = locationService.createLocation(locationDto)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                "Đã tạo địa điểm thành công",
                createdLocation
            )
        )
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    fun updateLocation(
        @PathVariable id: UUID,
        @Valid @RequestBody locationDto: LocationDto
    ): ResponseEntity<ApiResponse<LocationDto>> {
        val updatedLocation = locationService.updateLocation(id, locationDto)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã cập nhật địa điểm thành công",
                updatedLocation
            )
        )
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteLocation(@PathVariable id: UUID): ResponseEntity<ApiResponse<Boolean>> {
        val result = locationService.deleteLocation(id)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã xóa địa điểm thành công",
                result
            )
        )
    }

    @GetMapping("/search")
    fun searchLocationsByName(
        @RequestParam name: String,
        @PageableDefault(size = 20, sort = ["name"], direction = Sort.Direction.ASC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<LocationDto>>> {
        val locations = locationService.searchLocationsByName(name, pageable)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã tìm kiếm địa điểm thành công",
                locations
            )
        )
    }

    @GetMapping("/city/{city}")
    fun getLocationsByCity(
        @PathVariable city: String,
        @PageableDefault(size = 20, sort = ["name"], direction = Sort.Direction.ASC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<LocationDto>>> {
        val locations = locationService.getLocationsByCity(city, pageable)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách địa điểm theo thành phố thành công",
                locations
            )
        )
    }

    @GetMapping("/nearby")
    fun getNearbyLocations(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam(defaultValue = "10.0") radius: Double,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<LocationDto>>> {
        val locations = locationService.getNearbyLocations(latitude, longitude, radius, pageable)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách địa điểm gần đây thành công",
                locations
            )
        )
    }

    @GetMapping("/popular")
    fun getPopularLocations(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<ApiResponse<List<LocationDto>>> {
        val locations = locationService.getPopularLocations(limit)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách địa điểm phổ biến thành công",
                locations
            )
        )
    }
} 