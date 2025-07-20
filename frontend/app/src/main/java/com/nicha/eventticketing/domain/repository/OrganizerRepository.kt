package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.organizer.OrganizerCreateDto
import com.nicha.eventticketing.data.remote.dto.organizer.OrganizerDto
import com.nicha.eventticketing.data.remote.dto.organizer.OrganizerUpdateDto
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository xử lý các chức năng liên quan đến tổ chức sự kiện
 */
interface OrganizerRepository {
    /**
     * Lấy danh sách sự kiện của một tổ chức
     * @param organizerId ID của tổ chức
     * @param page Số trang
     * @param size Số lượng mỗi trang
     * @return Flow<Resource<PageDto<EventDto>>> Flow chứa danh sách sự kiện theo trang
     */
    fun getOrganizerEvents(
        organizerId: String,
        page: Int = 0,
        size: Int = 20
    ): Flow<Resource<PageDto<EventDto>>>
    
    /**
     * Lấy thông tin chi tiết của một tổ chức
     * @param organizerId ID của tổ chức
     * @return Flow<Resource<OrganizerDto>> Flow chứa thông tin tổ chức
     */
    fun getOrganizerById(organizerId: String): Flow<Resource<OrganizerDto>>
    
    /**
     * Lấy thông tin tổ chức của người dùng hiện tại
     * @return Flow<Resource<OrganizerDto>> Flow chứa thông tin tổ chức
     */
    fun getCurrentOrganizer(): Flow<Resource<OrganizerDto>>
    
    /**
     * Tạo tổ chức mới
     * @param organizer Thông tin tổ chức cần tạo
     * @return Flow<Resource<OrganizerDto>> Flow chứa thông tin tổ chức đã tạo
     */
    fun createOrganizer(organizer: OrganizerCreateDto): Flow<Resource<OrganizerDto>>
    
    /**
     * Cập nhật thông tin tổ chức
     * @param organizerId ID của tổ chức
     * @param organizer Thông tin tổ chức cần cập nhật
     * @return Flow<Resource<OrganizerDto>> Flow chứa thông tin tổ chức đã cập nhật
     */
    fun updateOrganizer(
        organizerId: String,
        organizer: OrganizerUpdateDto
    ): Flow<Resource<OrganizerDto>>
} 