package com.nicha.eventticketing.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.nicha.eventticketing.data.local.dao.EventDao
import com.nicha.eventticketing.data.local.entity.EventEntity
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.mapper.EventMapper
import com.nicha.eventticketing.domain.model.Event
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Interface cho EventRepository, định nghĩa các phương thức để tương tác với Event
 */
interface EventRepository {
    suspend fun getEvents(page: Int, size: Int): Flow<Resource<List<Event>>>
    suspend fun getEventById(eventId: String): Flow<Resource<Event>>
    suspend fun getFeaturedEvents(limit: Int = 10): Flow<Resource<List<Event>>>
    suspend fun getUpcomingEvents(limit: Int = 10): Flow<Resource<List<Event>>>
    suspend fun getNearbyEvents(latitude: Double, longitude: Double, radius: Double = 10.0, page: Int, size: Int): Flow<Resource<List<Event>>>
    suspend fun searchEvents(
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
    ): Flow<Resource<List<Event>>>
    
    // Local operations
    fun getLocalEvents(): Flow<List<Event>>
    fun getLocalEventsPaging(): Flow<PagingData<Event>>
    fun getLocalEventById(eventId: String): Flow<Event?>
    suspend fun updateFavoriteStatus(eventId: String, isFavorite: Boolean)
    fun getFavoriteEvents(): Flow<List<Event>>
}

/**
 * Triển khai của EventRepository
 */
class EventRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val eventDao: EventDao,
    private val eventMapper: EventMapper
) : EventRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    override suspend fun getEvents(page: Int, size: Int): Flow<Resource<List<Event>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getEvents(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                val events = response.body()?.data?.content
                events?.let {
                    // Lưu events vào database local
                    val eventEntities = it.map { eventDto ->
                        eventMapper.mapToEntity(eventDto)
                    }
                    eventDao.insertEvents(eventEntities)
                    
                    // Chuyển đổi sang domain model
                    val domainEvents = it.map { eventDto ->
                        eventMapper.mapToDomainModel(eventDto)
                    }
                    
                    emit(Resource.Success(domainEvents))
                } ?: emit(Resource.Error("Events data is null"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to get events"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    override suspend fun getEventById(eventId: String): Flow<Resource<Event>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getEventById(eventId)
            if (response.isSuccessful && response.body()?.success == true) {
                val event = response.body()?.data
                event?.let {
                    // Lưu event vào database local
                    eventDao.insertEvent(eventMapper.mapToEntity(it))
                    
                    // Chuyển đổi sang domain model
                    val domainEvent = eventMapper.mapToDomainModel(it)
                    
                    emit(Resource.Success(domainEvent))
                } ?: emit(Resource.Error("Event data is null"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to get event"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    override suspend fun getFeaturedEvents(limit: Int): Flow<Resource<List<Event>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getFeaturedEvents(limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val events = response.body()?.data
                events?.let {
                    // Lưu events vào database local
                    val eventEntities = it.map { eventDto ->
                        eventMapper.mapToEntity(eventDto)
                    }
                    eventDao.insertEvents(eventEntities)
                    
                    // Chuyển đổi sang domain model
                    val domainEvents = it.map { eventDto ->
                        eventMapper.mapToDomainModel(eventDto)
                    }
                    
                    emit(Resource.Success(domainEvents))
                } ?: emit(Resource.Error("Featured events data is null"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to get featured events"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    override suspend fun getUpcomingEvents(limit: Int): Flow<Resource<List<Event>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getUpcomingEvents(limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val events = response.body()?.data
                events?.let {
                    // Lưu events vào database local
                    val eventEntities = it.map { eventDto ->
                        eventMapper.mapToEntity(eventDto)
                    }
                    eventDao.insertEvents(eventEntities)
                    
                    // Chuyển đổi sang domain model
                    val domainEvents = it.map { eventDto ->
                        eventMapper.mapToDomainModel(eventDto)
                    }
                    
                    emit(Resource.Success(domainEvents))
                } ?: emit(Resource.Error("Upcoming events data is null"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to get upcoming events"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    override suspend fun getNearbyEvents(
        latitude: Double,
        longitude: Double,
        radius: Double,
        page: Int,
        size: Int
    ): Flow<Resource<List<Event>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getNearbyEvents(latitude, longitude, radius, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                val events = response.body()?.data?.content
                events?.let {
                    // Lưu events vào database local
                    val eventEntities = it.map { eventDto ->
                        eventMapper.mapToEntity(eventDto)
                    }
                    eventDao.insertEvents(eventEntities)
                    
                    // Chuyển đổi sang domain model
                    val domainEvents = it.map { eventDto ->
                        eventMapper.mapToDomainModel(eventDto)
                    }
                    
                    emit(Resource.Success(domainEvents))
                } ?: emit(Resource.Error("Nearby events data is null"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to get nearby events"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    override suspend fun searchEvents(
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
    ): Flow<Resource<List<Event>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.searchEvents(
                keyword, categoryId, startDate, endDate, locationId, radius,
                latitude, longitude, minPrice, maxPrice, status, page, size
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val events = response.body()?.data?.content
                events?.let {
                    // Lưu events vào database local
                    val eventEntities = it.map { eventDto ->
                        eventMapper.mapToEntity(eventDto)
                    }
                    eventDao.insertEvents(eventEntities)
                    
                    // Chuyển đổi sang domain model
                    val domainEvents = it.map { eventDto ->
                        eventMapper.mapToDomainModel(eventDto)
                    }
                    
                    emit(Resource.Success(domainEvents))
                } ?: emit(Resource.Error("Search events data is null"))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to search events"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    // Local operations
    override fun getLocalEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents().map { entities ->
            entities.map { entity ->
                eventMapper.mapToDomainModel(entity)
            }
        }
    }

    override fun getLocalEventsPaging(): Flow<PagingData<Event>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                maxSize = 100
            ),
            pagingSourceFactory = { eventDao.getEventsPagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                eventMapper.mapToDomainModel(entity)
            }
        }
    }

    override fun getLocalEventById(eventId: String): Flow<Event?> {
        return eventDao.getEventById(eventId).map { entity ->
            entity?.let {
                eventMapper.mapToDomainModel(it)
            }
        }
    }

    override suspend fun updateFavoriteStatus(eventId: String, isFavorite: Boolean) {
        eventDao.updateFavoriteStatus(eventId, isFavorite)
    }

    override fun getFavoriteEvents(): Flow<List<Event>> {
        return eventDao.getFavoriteEvents().map { entities ->
            entities.map { entity ->
                eventMapper.mapToDomainModel(entity)
            }
        }
    }
} 