package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.location.LocationDto
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository xử lý các chức năng liên quan đến vị trí
 */
interface LocationRepository {
    /**
     * Lấy danh sách vị trí
     * @param page Số trang
     * @param size Số lượng mỗi trang
     * @return Flow<Resource<PageDto<LocationDto>>> Flow chứa danh sách vị trí theo trang
     */
    fun getLocations(
        page: Int = 0,
        size: Int = 20
    ): Flow<Resource<PageDto<LocationDto>>>
    
    /**
     * Lấy thông tin vị trí theo ID
     * @param locationId ID của vị trí
     * @return Flow<Resource<LocationDto>> Flow chứa thông tin vị trí
     */
    fun getLocationById(locationId: String): Flow<Resource<LocationDto>>
    
    /**
     * Tìm kiếm vị trí gần đây
     * @param latitude Vĩ độ
     * @param longitude Kinh độ
     * @param radius Bán kính tìm kiếm (km)
     * @param page Số trang
     * @param size Số lượng mỗi trang
     * @return Flow<Resource<PageDto<LocationDto>>> Flow chứa danh sách vị trí gần đây theo trang
     */
    fun getNearbyLocations(
        latitude: Double,
        longitude: Double,
        radius: Double = 10.0,
        page: Int = 0,
        size: Int = 20
    ): Flow<Resource<PageDto<LocationDto>>>
} 