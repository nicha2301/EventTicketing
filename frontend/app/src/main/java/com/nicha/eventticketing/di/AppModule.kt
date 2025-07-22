package com.nicha.eventticketing.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.nicha.eventticketing.config.AppConfig
import com.nicha.eventticketing.data.local.AppDatabase
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.preferences.PreferencesManagerImpl
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.util.NetworkStatus
import com.nicha.eventticketing.util.NetworkSyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
    
    @Singleton
    @Provides
    fun providePreferencesManager(
        dataStore: DataStore<Preferences>,
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManagerImpl(dataStore, context)
    }
    
    @Singleton
    @Provides
    fun provideNetworkStatus(@ApplicationContext context: Context): NetworkStatus {
        return NetworkStatus(context)
    }
} 