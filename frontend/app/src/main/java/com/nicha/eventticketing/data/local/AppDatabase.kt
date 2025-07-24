package com.nicha.eventticketing.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nicha.eventticketing.data.local.converter.StringListConverter
import com.nicha.eventticketing.data.local.converter.TicketTypeListConverter
import com.nicha.eventticketing.data.local.dao.EventDao
import com.nicha.eventticketing.data.local.dao.TicketDao
import com.nicha.eventticketing.data.local.dao.UserDao
import com.nicha.eventticketing.data.local.entity.EventEntity
import com.nicha.eventticketing.data.local.entity.TicketEntity
import com.nicha.eventticketing.data.local.entity.UserEntity

@Database(
    entities = [EventEntity::class, TicketEntity::class, UserEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(StringListConverter::class, TicketTypeListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun ticketDao(): TicketDao
    abstract fun userDao(): UserDao
} 