package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.mapper.EventMapper
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.EventRepository
import com.nicha.eventticketing.util.NetworkUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val eventMapper: EventMapper
) : EventRepository {

    override fun getEvents(page: Int, size: Int): Flow<Resource<PageDto<EventDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getEvents(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                val events = response.body()?.data
                if (events != null) {
                    emit(Resource.Success(events))
                } else {
                    emit(Resource.Error("Không tìm thấy sự kiện"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách sự kiện"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách sự kiện")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    override fun getEventById(eventId: String): Flow<Resource<EventDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getEventById(eventId)
            if (response.isSuccessful && response.body()?.success == true) {
                val event = response.body()?.data
                if (event != null) {
                    emit(Resource.Success(event))
                } else {
                    emit(Resource.Error("Không tìm thấy sự kiện"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Không thể lấy thông tin sự kiện"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy thông tin sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    override fun getFeaturedEvents(limit: Int): Flow<Resource<List<EventDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getFeaturedEvents(limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val events = response.body()?.data
                if (events != null) {
                    emit(Resource.Success(events))
                } else {
                    emit(Resource.Error("Không tìm thấy sự kiện nổi bật"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách sự kiện nổi bật"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách sự kiện nổi bật")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    override fun getUpcomingEvents(limit: Int): Flow<Resource<List<EventDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getUpcomingEvents(limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val events = response.body()?.data
                if (events != null) {
                    emit(Resource.Success(events))
                } else {
                    emit(Resource.Error("Không tìm thấy sự kiện sắp tới"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách sự kiện sắp tới"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách sự kiện sắp tới")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    override fun getNearbyEvents(
        latitude: Double,
        longitude: Double,
        radius: Double,
        page: Int,
        size: Int
    ): Flow<Resource<PageDto<EventDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getNearbyEvents(latitude, longitude, radius, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                val events = response.body()?.data
                if (events != null) {
                    emit(Resource.Success(events))
                } else {
                    emit(Resource.Error("Không tìm thấy sự kiện gần đây"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách sự kiện gần đây"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách sự kiện gần đây")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    override fun searchEvents(
        keyword: String?,
        categoryId: String?,
        startDate: String?,
        endDate: String?,
        locationId: String?,
        radius: Double?,
        latitude: Double?,
        longitude: Double?,
        minPrice: Double?,
        maxPrice: Double?,
        status: String?,
        page: Int,
        size: Int
    ): Flow<Resource<PageDto<EventDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.searchEvents(
                keyword, categoryId, startDate, endDate, locationId, radius,
                latitude, longitude, minPrice, maxPrice, status, page, size
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val events = response.body()?.data
                if (events != null) {
                    emit(Resource.Success(events))
                } else {
                    emit(Resource.Error("Không tìm thấy sự kiện phù hợp"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Không thể tìm kiếm sự kiện"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi tìm kiếm sự kiện")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun createEvent(eventDto: EventDto): Flow<Resource<EventDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.createEvent(eventDto)
            if (response.isSuccessful && response.body()?.success == true) {
                val createdEvent = response.body()?.data
                if (createdEvent != null) {
                    emit(Resource.Success(createdEvent))
                } else {
                    emit(Resource.Error("Không thể tạo sự kiện"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Tạo sự kiện thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi tạo sự kiện")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun updateEvent(eventId: String, eventDto: EventDto): Flow<Resource<EventDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.updateEvent(eventId, eventDto)
            if (response.isSuccessful && response.body()?.success == true) {
                val updatedEvent = response.body()?.data
                if (updatedEvent != null) {
                    emit(Resource.Success(updatedEvent))
                } else {
                    emit(Resource.Error("Không thể cập nhật sự kiện"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Cập nhật sự kiện thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun deleteEvent(eventId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteEvent(eventId)
            if (response.isSuccessful && response.body()?.success == true) {
                val result = response.body()?.data
                if (result != null) {
                    emit(Resource.Success(result))
                } else {
                    emit(Resource.Error("Không thể xóa sự kiện"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Xóa sự kiện thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xóa sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun publishEvent(eventId: String): Flow<Resource<EventDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.publishEvent(eventId)
            if (response.isSuccessful && response.body()?.success == true) {
                val publishedEvent = response.body()?.data
                if (publishedEvent != null) {
                    emit(Resource.Success(publishedEvent))
                } else {
                    emit(Resource.Error("Không thể xuất bản sự kiện"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Xuất bản sự kiện thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xuất bản sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun cancelEvent(eventId: String, reason: String): Flow<Resource<EventDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.cancelEvent(eventId, mapOf("reason" to reason))
            if (response.isSuccessful && response.body()?.success == true) {
                val cancelledEvent = response.body()?.data
                if (cancelledEvent != null) {
                    emit(Resource.Success(cancelledEvent))
                } else {
                    emit(Resource.Error("Không thể hủy sự kiện"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Hủy sự kiện thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi hủy sự kiện: $eventId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
} 