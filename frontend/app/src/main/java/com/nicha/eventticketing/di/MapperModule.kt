package com.nicha.eventticketing.di

import com.nicha.eventticketing.domain.mapper.CategoryMapper
import com.nicha.eventticketing.domain.mapper.EventMapper
import com.nicha.eventticketing.domain.mapper.PaymentMapper
import com.nicha.eventticketing.domain.mapper.TicketMapper
import com.nicha.eventticketing.domain.mapper.UserMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Dagger Hilt để cung cấp các mapper
 */
@Module
@InstallIn(SingletonComponent::class)
object MapperModule {

    @Provides
    @Singleton
    fun provideEventMapper(): EventMapper {
        return EventMapper()
    }

    @Provides
    @Singleton
    fun provideCategoryMapper(): CategoryMapper {
        return CategoryMapper()
    }

    @Provides
    @Singleton
    fun provideUserMapper(): UserMapper {
        return UserMapper()
    }

    @Provides
    @Singleton
    fun provideTicketMapper(): TicketMapper {
        return TicketMapper()
    }

    @Provides
    @Singleton
    fun providePaymentMapper(): PaymentMapper {
        return PaymentMapper()
    }
} 