package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.mapper.EventMapper
import com.nicha.eventticketing.domain.model.Event
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.EventRepository
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

    override suspend fun getEvents(page: Int, size: Int): Flow<Resource<List<Event>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getEvents(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                val events = response.body()?.data?.content
                events?.let {
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
} 