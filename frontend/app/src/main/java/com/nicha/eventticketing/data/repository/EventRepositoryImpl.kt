package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.local.dao.EventDao
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.event.PageableDto
import com.nicha.eventticketing.data.remote.dto.event.SortDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.mapper.EventMapper
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.EventRepository
import com.nicha.eventticketing.util.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val eventMapper: EventMapper,
    private val eventDao: EventDao
) : EventRepository {

    private var isOnline = true

    fun setNetworkStatus(online: Boolean) {
        isOnline = online
    }

    override fun getEvents(page: Int, size: Int): Flow<Resource<PageDto<EventDto>>> = flow {
        emit(Resource.Loading())
        
        val actuallyOnline = isOnline && NetworkUtil.isActuallyConnected()
        
        if (actuallyOnline) {
            try {
                val response = apiService.getEvents(page, size)
                if (response.isSuccessful && response.body()?.success == true) {
                    val events = response.body()?.data
                    if (events != null) {
                        withContext(Dispatchers.IO) {
                            val eventEntities = events.content.map { eventMapper.dtoToEntity(it) }
                            eventDao.insertEvents(eventEntities)
                        }
                        emit(Resource.Success(events))
                    } else {
                        emit(Resource.Error("Không tìm thấy sự kiện"))
                    }
                } else {
                    emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách sự kiện"))
                }
            } catch (e: Exception) {
                val cachedEvents = withContext(Dispatchers.IO) {
                    eventDao.getAllEvents()
                }
                if (cachedEvents.isNotEmpty()) {
                    val eventDtos = cachedEvents.map { eventMapper.entityToDto(it) }
                    val pageDto = PageDto(
                        content = eventDtos,
                        pageable = PageableDto(
                            pageNumber = 0,
                            pageSize = eventDtos.size,
                            sort = SortDto(
                                sorted = false,
                                unsorted = true,
                                empty = true
                            ),
                            offset = 0,
                            paged = true,
                            unpaged = false
                        ),
                        last = true,
                        totalElements = eventDtos.size.toLong(),
                        totalPages = 1,
                        size = eventDtos.size,
                        number = 0,
                        sort = SortDto(
                            sorted = false,
                            unsorted = true,
                            empty = true
                        ),
                        first = true,
                        numberOfElements = eventDtos.size,
                        empty = eventDtos.isEmpty()
                    )
                    emit(Resource.Success(pageDto))
                } else {
                    emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
                }
            }
        } else {
            val cachedEvents = withContext(Dispatchers.IO) {
                eventDao.getAllEvents()
            }
            if (cachedEvents.isNotEmpty()) {
                val eventDtos = cachedEvents.map { eventMapper.entityToDto(it) }
                val pageDto = PageDto(
                    content = eventDtos,
                    pageable = PageableDto(
                        pageNumber = 0,
                        pageSize = eventDtos.size,
                        sort = SortDto(
                            sorted = false,
                            unsorted = true,
                            empty = true
                        ),
                        offset = 0,
                        paged = true,
                        unpaged = false
                    ),
                    last = true,
                    totalElements = eventDtos.size.toLong(),
                    totalPages = 1,
                    size = eventDtos.size,
                    number = 0,
                    sort = SortDto(
                        sorted = false,
                        unsorted = true,
                        empty = true
                    ),
                    first = true,
                    numberOfElements = eventDtos.size,
                    empty = eventDtos.isEmpty()
                )
                emit(Resource.Success(pageDto))
            } else {
                emit(Resource.Error("Không có dữ liệu offline và không có kết nối mạng"))
            }
        }
    }

    override fun getEventById(eventId: String): Flow<Resource<EventDto>> = flow {
        emit(Resource.Loading())
        
        val actuallyOnline = isOnline && NetworkUtil.isActuallyConnected()
        
        if (actuallyOnline) {
            try {
                val response = apiService.getEventById(eventId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val event = response.body()?.data
                    if (event != null) {
                        withContext(Dispatchers.IO) {
                            val eventEntity = eventMapper.dtoToEntity(event)
                            eventDao.insertEvent(eventEntity)
                        }
                        emit(Resource.Success(event))
                    } else {
                        emit(Resource.Error("Không tìm thấy sự kiện"))
                    }
                } else {
                    emit(Resource.Error(response.body()?.message ?: "Không thể lấy thông tin sự kiện"))
                }
            } catch (e: Exception) {
                
                val cachedEvent = withContext(Dispatchers.IO) {
                    eventDao.getEventById(eventId)
                }
                if (cachedEvent != null) {
                    emit(Resource.Success(eventMapper.entityToDto(cachedEvent)))
                } else {
                    emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
                }
            }
        } else {
            val cachedEvent = withContext(Dispatchers.IO) {
                eventDao.getEventById(eventId)
            }
            if (cachedEvent != null) {
                emit(Resource.Success(eventMapper.entityToDto(cachedEvent)))
            } else {
                emit(Resource.Error("Không có dữ liệu sự kiện offline và không có kết nối mạng"))
            }
        }
    }

    override fun getFeaturedEvents(limit: Int): Flow<Resource<List<EventDto>>> = flow {
        emit(Resource.Loading())
        
        val actuallyOnline = isOnline && NetworkUtil.isActuallyConnected()
        
        if (actuallyOnline) {
            try {
                val response = apiService.getFeaturedEvents(limit)
                if (response.isSuccessful && response.body()?.success == true) {
                    val events = response.body()?.data
                    if (events != null) {
                        withContext(Dispatchers.IO) {
                            val eventEntities = events.map { eventMapper.dtoToEntity(it) }
                            eventDao.insertEvents(eventEntities)
                        }
                        emit(Resource.Success(events))
                    } else {
                        emit(Resource.Error("Không tìm thấy sự kiện nổi bật"))
                    }
                } else {
                    emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách sự kiện nổi bật"))
                }
            } catch (e: Exception) {
                val cachedEvents = withContext(Dispatchers.IO) {
                    eventDao.getAllEvents()
                }
                val featuredEvents = cachedEvents.filter { it.isFeatured == true }
                if (featuredEvents.isNotEmpty()) {
                    emit(Resource.Success(featuredEvents.map { eventMapper.entityToDto(it) }))
                } else {
                    if (cachedEvents.isNotEmpty()) {
                        val anyEvents = cachedEvents.take(3)
                        emit(Resource.Success(anyEvents.map { eventMapper.entityToDto(it) }))
                    } else {
                        emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
                    }
                }
            }
        } else {
            val cachedEvents = withContext(Dispatchers.IO) {
                eventDao.getAllEvents()
            }
            val featuredEvents = cachedEvents.filter { it.isFeatured == true }
            if (featuredEvents.isNotEmpty()) {
                emit(Resource.Success(featuredEvents.map { eventMapper.entityToDto(it) }))
            } else {
                if (cachedEvents.isNotEmpty()) {
                    val anyEvents = cachedEvents.take(3)
                    emit(Resource.Success(anyEvents.map { eventMapper.entityToDto(it) }))
                } else {
                    emit(Resource.Error("Không có dữ liệu sự kiện nổi bật offline và không có kết nối mạng"))
                }
            }
        }
    }

    override fun getUpcomingEvents(limit: Int): Flow<Resource<List<EventDto>>> = flow {
        emit(Resource.Loading())
        
        val actuallyOnline = isOnline && NetworkUtil.isActuallyConnected()
        
        if (actuallyOnline) {
            try {
                val response = apiService.getUpcomingEvents(limit)
                if (response.isSuccessful && response.body()?.success == true) {
                    val events = response.body()?.data
                    if (events != null) {
                        withContext(Dispatchers.IO) {
                            val eventEntities = events.map { eventMapper.dtoToEntity(it) }
                            eventDao.insertEvents(eventEntities)
                        }
                        emit(Resource.Success(events))
                    } else {
                        emit(Resource.Error("Không tìm thấy sự kiện sắp tới"))
                    }
                } else {
                    emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách sự kiện sắp tới"))
                }
            } catch (e: Exception) {
                val cachedEvents = withContext(Dispatchers.IO) {
                    eventDao.getAllEvents()
                }
                val now = System.currentTimeMillis()
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                val upcomingEvents = cachedEvents.filter { event ->
                    try {
                        val startDateStr = event.startDate
                        if (startDateStr != null) {
                            val startDate = dateFormat.parse(startDateStr)
                            startDate?.time ?: 0 > now
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        false
                    }
                }
                if (upcomingEvents.isNotEmpty()) {
                    emit(Resource.Success(upcomingEvents.map { eventMapper.entityToDto(it) }))
                } else {
                    if (cachedEvents.isNotEmpty()) {
                        val anyEvents = cachedEvents.take(3)
                        emit(Resource.Success(anyEvents.map { eventMapper.entityToDto(it) }))
                    } else {
                        emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
                    }
                }
            }
        } else {
            val cachedEvents = withContext(Dispatchers.IO) {
                eventDao.getAllEvents()
            }
            val now = System.currentTimeMillis()
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            val upcomingEvents = cachedEvents.filter { event ->
                try {
                    val startDateStr = event.startDate
                    if (startDateStr != null) {
                        val startDate = dateFormat.parse(startDateStr)
                        startDate?.time ?: 0 > now
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
            if (upcomingEvents.isNotEmpty()) {
                emit(Resource.Success(upcomingEvents.map { eventMapper.entityToDto(it) }))
            } else {
                if (cachedEvents.isNotEmpty()) {
                    val anyEvents = cachedEvents.take(3)
                    emit(Resource.Success(anyEvents.map { eventMapper.entityToDto(it) }))
                } else {
                    emit(Resource.Error("Không có dữ liệu sự kiện sắp tới offline và không có kết nối mạng"))
                }
            }
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