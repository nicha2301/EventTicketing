package com.nicha.eventticketing.di

import android.content.Context
import androidx.room.Room
import com.nicha.eventticketing.data.local.AppDatabase
import com.nicha.eventticketing.data.local.dao.EventDao
import com.nicha.eventticketing.data.local.dao.TicketDao
import com.nicha.eventticketing.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "event_ticketing.db"
        ).build()
    }

    @Provides
    fun provideEventDao(db: AppDatabase): EventDao = db.eventDao()

    @Provides
    fun provideTicketDao(db: AppDatabase): TicketDao = db.ticketDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
} 