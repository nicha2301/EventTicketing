package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository xử lý các chức năng liên quan đến sự kiện
 */
interface EventRepository {
    /**
     * Lấy danh sách sự kiện có phân trang
     * @param page Trang cần lấy (bắt đầu từ 0)
     * @param size Số lượng sự kiện trên một trang
     * @return Flow<Resource<PageDto<EventDto>>> Flow chứa danh sách sự kiện
     */
    fun getEvents(page: Int, size: Int): Flow<Resource<PageDto<EventDto>>>

    /**
     * Lấy thông tin chi tiết của một sự kiện theo ID
     * @param eventId ID của sự kiện
     * @return Flow<Resource<EventDto>> Flow chứa thông tin sự kiện
     */
    fun getEventById(eventId: String): Flow<Resource<EventDto>>
    
    /**
     * Tìm kiếm sự kiện theo các điều kiện
     * @param keyword Từ khóa tìm kiếm
     * @param categoryId ID danh mục
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param locationId ID địa điểm
     * @param radius Bán kính tìm kiếm (km)
     * @param latitude Vĩ độ
     * @param longitude Kinh độ
     * @param minPrice Giá tối thiểu
     * @param maxPrice Giá tối đa
     * @param status Trạng thái sự kiện
     * @param page Trang cần lấy
     * @param size Số lượng sự kiện trên một trang
     * @return Flow<Resource<PageDto<EventDto>>> Flow chứa danh sách sự kiện phù hợp
     */
    fun searchEvents(
        keyword: String? = null,
        categoryId: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        locationId: String? = null,
        radius: Double? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        status: String? = null,
        page: Int,
        size: Int
    ): Flow<Resource<PageDto<EventDto>>>
    
    /**
     * Lấy danh sách sự kiện nổi bật
     * @param limit Số lượng sự kiện tối đa cần lấy
     * @return Flow<Resource<List<EventDto>>> Flow chứa danh sách sự kiện nổi bật
     */
    fun getFeaturedEvents(limit: Int = 10): Flow<Resource<List<EventDto>>>
    
    /**
     * Lấy danh sách sự kiện sắp diễn ra
     * @param limit Số lượng sự kiện tối đa cần lấy
     * @return Flow<Resource<List<EventDto>>> Flow chứa danh sách sự kiện sắp diễn ra
     */
    fun getUpcomingEvents(limit: Int = 10): Flow<Resource<List<EventDto>>>
    
    /**
     * Lấy danh sách sự kiện gần đây dựa trên vị trí
     * @param latitude Vĩ độ
     * @param longitude Kinh độ
     * @param radius Bán kính tìm kiếm (km)
     * @param page Trang cần lấy
     * @param size Số lượng sự kiện trên một trang
     * @return Flow<Resource<PageDto<EventDto>>> Flow chứa danh sách sự kiện gần đây
     */
    fun getNearbyEvents(
        latitude: Double,
        longitude: Double,
        radius: Double = 10.0,
        page: Int,
        size: Int
    ): Flow<Resource<PageDto<EventDto>>>
    
    /**
     * Tạo sự kiện mới
     * @param eventDto Thông tin sự kiện cần tạo
     * @return Flow<Resource<EventDto>> Flow chứa thông tin sự kiện đã tạo
     */
    fun createEvent(eventDto: EventDto): Flow<Resource<EventDto>>
    
    /**
     * Tạo sự kiện mới kèm ảnh
     * @param eventData Thông tin sự kiện cần tạo
     * @param imageUris Danh sách URI của ảnh
     * @return Flow<Resource<EventDto>> Flow chứa thông tin sự kiện đã tạo
     */
    fun createEventWithImages(eventData: com.nicha.eventticketing.data.remote.dto.event.CreateEventWithImagesRequest, imageUris: List<android.net.Uri>): Flow<Resource<EventDto>>
    
    /**
     * Cập nhật thông tin sự kiện
     * @param eventId ID của sự kiện
     * @param eventDto Thông tin sự kiện cần cập nhật
     * @return Flow<Resource<EventDto>> Flow chứa thông tin sự kiện đã cập nhật
     */
    fun updateEvent(eventId: String, eventDto: EventDto): Flow<Resource<EventDto>>
    
    /**
     * Xóa sự kiện
     * @param eventId ID của sự kiện
     * @return Flow<Resource<Boolean>> Flow chứa kết quả xóa
     */
    fun deleteEvent(eventId: String): Flow<Resource<Boolean>>
    
    /**
     * Xuất bản sự kiện
     * @param eventId ID của sự kiện
     * @return Flow<Resource<EventDto>> Flow chứa thông tin sự kiện đã xuất bản
     */
    fun publishEvent(eventId: String): Flow<Resource<EventDto>>
    
    /**
     * Hủy sự kiện
     * @param eventId ID của sự kiện
     * @param reason Lý do hủy
     * @return Flow<Resource<EventDto>> Flow chứa thông tin sự kiện đã hủy
     */
    fun cancelEvent(eventId: String, reason: String): Flow<Resource<EventDto>>
} 