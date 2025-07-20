package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypePageResponse
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.TicketTypeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketTypeRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : TicketTypeRepository {
    
    override fun getTicketTypes(
        eventId: String, 
        page: Int, 
        size: Int
    ): Flow<Resource<TicketTypePageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTicketTypes(eventId, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                val ticketTypes = response.body()?.data
                if (ticketTypes != null) {
                    emit(Resource.Success(ticketTypes))
                } else {
                    emit(Resource.Error("Không tìm thấy loại vé"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách loại vé"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách loại vé của sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getTicketTypeById(ticketTypeId: String): Flow<Resource<TicketTypeDto>> = flow {
        emit(Resource.Loading())
        try {
            // API hiện tại không có endpoint riêng để lấy thông tin chi tiết của một loại vé
            // Tạm thời chưa triển khai
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy thông tin loại vé: $ticketTypeId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun createTicketType(
        eventId: String, 
        ticketType: TicketTypeDto
    ): Flow<Resource<TicketTypeDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.createTicketType(eventId, ticketType)
            if (response.isSuccessful && response.body()?.success == true) {
                val createdTicketType = response.body()?.data
                if (createdTicketType != null) {
                    emit(Resource.Success(createdTicketType))
                } else {
                    emit(Resource.Error("Không thể tạo loại vé"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Tạo loại vé thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi tạo loại vé cho sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun updateTicketType(
        ticketTypeId: String, 
        ticketType: TicketTypeDto
    ): Flow<Resource<TicketTypeDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.updateTicketType(ticketTypeId, ticketType)
            if (response.isSuccessful && response.body()?.success == true) {
                val updatedTicketType = response.body()?.data
                if (updatedTicketType != null) {
                    emit(Resource.Success(updatedTicketType))
                } else {
                    emit(Resource.Error("Không thể cập nhật loại vé"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Cập nhật loại vé thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật loại vé: $ticketTypeId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun deleteTicketType(ticketTypeId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteTicketType(ticketTypeId)
            if (response.isSuccessful && response.body()?.success == true) {
                val result = response.body()?.data
                if (result != null) {
                    emit(Resource.Success(result))
                } else {
                    emit(Resource.Error("Không thể xóa loại vé"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Xóa loại vé thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xóa loại vé: $ticketTypeId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun checkTicketTypeAvailability(ticketTypeId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            // API hiện tại không có endpoint riêng để kiểm tra tình trạng còn vé
            // Tạm thời chưa triển khai
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi kiểm tra tình trạng còn vé của loại vé: $ticketTypeId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun updateTicketTypeQuantity(
        ticketTypeId: String, 
        quantity: Int
    ): Flow<Resource<TicketTypeDto>> = flow {
        emit(Resource.Loading())
        try {
            // API hiện tại không có endpoint riêng để cập nhật số lượng vé
            // Tạm thời chưa triển khai
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật số lượng vé của loại vé: $ticketTypeId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
} 