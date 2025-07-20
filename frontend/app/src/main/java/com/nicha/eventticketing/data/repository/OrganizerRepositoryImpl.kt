package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.organizer.OrganizerCreateDto
import com.nicha.eventticketing.data.remote.dto.organizer.OrganizerDto
import com.nicha.eventticketing.data.remote.dto.organizer.OrganizerUpdateDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.OrganizerRepository
import com.nicha.eventticketing.util.NetworkUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrganizerRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : OrganizerRepository {
    
    override fun getOrganizerEvents(
        organizerId: String,
        page: Int,
        size: Int
    ): Flow<Resource<PageDto<EventDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getOrganizerEvents(organizerId, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                val events = response.body()?.data
                if (events != null) {
                    emit(Resource.Success(events))
                    Timber.d("Lấy danh sách sự kiện của tổ chức thành công: ${events.content?.size ?: 0} sự kiện")
                } else {
                    emit(Resource.Error("Không tìm thấy sự kiện"))
                    Timber.e("Không tìm thấy sự kiện của tổ chức")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy danh sách sự kiện của tổ chức"))
                Timber.e("Lấy danh sách sự kiện của tổ chức thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách sự kiện của tổ chức: $organizerId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getOrganizerById(organizerId: String): Flow<Resource<OrganizerDto>> = flow {
        emit(Resource.Loading())
        try {
            // API hiện tại không có endpoint riêng để lấy thông tin tổ chức
            // Tạm thời chưa triển khai
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
            Timber.e("API getOrganizerById chưa được hỗ trợ")
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy thông tin tổ chức: $organizerId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getCurrentOrganizer(): Flow<Resource<OrganizerDto>> = flow {
        emit(Resource.Loading())
        try {
            // API hiện tại không có endpoint riêng để lấy thông tin tổ chức hiện tại
            // Tạm thời chưa triển khai
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
            Timber.e("API getCurrentOrganizer chưa được hỗ trợ")
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy thông tin tổ chức hiện tại")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun createOrganizer(organizer: OrganizerCreateDto): Flow<Resource<OrganizerDto>> = flow {
        emit(Resource.Loading())
        try {
            // API hiện tại không có endpoint riêng để tạo tổ chức
            // Tạm thời chưa triển khai
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
            Timber.e("API createOrganizer chưa được hỗ trợ")
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi tạo tổ chức")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun updateOrganizer(
        organizerId: String,
        organizer: OrganizerUpdateDto
    ): Flow<Resource<OrganizerDto>> = flow {
        emit(Resource.Loading())
        try {
            // API hiện tại không có endpoint riêng để cập nhật tổ chức
            // Tạm thời chưa triển khai
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
            Timber.e("API updateOrganizer chưa được hỗ trợ")
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật tổ chức: $organizerId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
} 