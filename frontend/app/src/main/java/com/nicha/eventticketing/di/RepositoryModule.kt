package com.nicha.eventticketing.di

import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.mapper.EventMapper
import com.nicha.eventticketing.domain.repository.AuthRepository
import com.nicha.eventticketing.data.repository.AuthRepositoryImpl
import com.nicha.eventticketing.domain.repository.EventRepository
import com.nicha.eventticketing.data.repository.EventRepositoryImpl
import com.nicha.eventticketing.domain.repository.UserRepository
import com.nicha.eventticketing.data.repository.UserRepositoryImpl
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
        preferencesManager: PreferencesManager
    ): AuthRepository {
        return AuthRepositoryImpl(apiService, preferencesManager)
    }

    @Provides
    @Singleton
    fun provideEventRepository(
        apiService: ApiService,
        eventMapper: EventMapper
    ): EventRepository {
        return EventRepositoryImpl(apiService, eventMapper)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService
    ): UserRepository {
        return UserRepositoryImpl(apiService)
    }
} 