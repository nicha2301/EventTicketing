package com.nicha.eventticketing.di

import com.nicha.eventticketing.data.auth.GoogleAuthManager
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.mapper.EventMapper
import com.nicha.eventticketing.domain.repository.AuthRepository
import com.nicha.eventticketing.data.repository.AuthRepositoryImpl
import com.nicha.eventticketing.domain.repository.EventRepository
import com.nicha.eventticketing.data.repository.EventRepositoryImpl
import com.nicha.eventticketing.domain.repository.UserRepository
import com.nicha.eventticketing.data.repository.UserRepositoryImpl
import com.nicha.eventticketing.domain.repository.EventImageRepository
import com.nicha.eventticketing.data.repository.EventImageRepositoryImpl
import com.nicha.eventticketing.domain.repository.TicketTypeRepository
import com.nicha.eventticketing.data.repository.TicketTypeRepositoryImpl
import com.nicha.eventticketing.domain.repository.TicketRepository
import com.nicha.eventticketing.data.repository.TicketRepositoryImpl
import com.nicha.eventticketing.data.local.dao.TicketDao
import com.nicha.eventticketing.data.local.dao.UserDao
import com.nicha.eventticketing.data.local.dao.EventDao
import com.nicha.eventticketing.domain.repository.AnalyticsRepository
import com.nicha.eventticketing.data.repository.AnalyticsRepositoryImpl
import com.nicha.eventticketing.domain.mapper.UserMapper
import com.nicha.eventticketing.domain.repository.CategoryRepository
import com.nicha.eventticketing.data.repository.CategoryRepositoryImpl
import com.nicha.eventticketing.domain.repository.PaymentRepository
import com.nicha.eventticketing.data.repository.PaymentRepositoryImpl
import com.nicha.eventticketing.domain.repository.OrganizerRepository
import com.nicha.eventticketing.data.repository.OrganizerRepositoryImpl
import com.nicha.eventticketing.domain.repository.NotificationRepository
import com.nicha.eventticketing.data.repository.NotificationRepositoryImpl
import com.nicha.eventticketing.domain.repository.LocationRepository
import com.nicha.eventticketing.data.repository.LocationRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        preferencesManager: PreferencesManager,
        googleAuthManager: GoogleAuthManager
    ): AuthRepository {
        return AuthRepositoryImpl(apiService, preferencesManager, googleAuthManager)
    }

    @Provides
    @Singleton
    fun provideEventRepository(
        apiService: ApiService,
        eventMapper: EventMapper,
        eventDao: EventDao
    ): EventRepository {
        return EventRepositoryImpl(apiService, eventMapper, eventDao)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        userDao: UserDao,
        userMapper: UserMapper
    ): UserRepository {
        return UserRepositoryImpl(apiService, userDao, userMapper)
    }
    
    @Provides
    @Singleton
    fun provideEventImageRepository(
        apiService: ApiService
    ): EventImageRepository {
        return EventImageRepositoryImpl(apiService)
    }
    
    @Provides
    @Singleton
    fun provideTicketTypeRepository(
        apiService: ApiService
    ): TicketTypeRepository {
        return TicketTypeRepositoryImpl(apiService)
    }
    
    @Provides
    @Singleton
    fun provideTicketRepository(
        apiService: ApiService,
        ticketDao: TicketDao
    ): TicketRepository {
        return TicketRepositoryImpl(apiService, ticketDao)
    }
    
    @Provides
    @Singleton
    fun provideCategoryRepository(
        apiService: ApiService
    ): CategoryRepository {
        return CategoryRepositoryImpl(apiService)
    }
    
    @Provides
    @Singleton
    fun providePaymentRepository(
        apiService: ApiService
    ): PaymentRepository {
        return PaymentRepositoryImpl(apiService)
    }
    
    @Provides
    @Singleton
    fun provideOrganizerRepository(
        apiService: ApiService
    ): OrganizerRepository {
        return OrganizerRepositoryImpl(apiService)
    }
    
    @Provides
    @Singleton
    fun provideNotificationRepository(
        apiService: ApiService
    ): NotificationRepository {
        return NotificationRepositoryImpl(apiService)
    }
    
    @Provides
    @Singleton
    fun provideLocationRepository(
        apiService: ApiService
    ): LocationRepository {
        return LocationRepositoryImpl(apiService)
    }
    
    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        apiService: ApiService
    ): AnalyticsRepository {
        return AnalyticsRepositoryImpl(apiService)
    }
} 