package com.nicha.eventticketing.di

import android.content.Context
import com.nicha.eventticketing.util.ReportGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {

    @Provides
    @Singleton
    fun provideReportGenerator(
        @ApplicationContext context: Context
    ): ReportGenerator {
        return ReportGenerator(context)
    }
}
