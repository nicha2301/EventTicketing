package com.nicha.eventticketing.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nicha.eventticketing.data.local.converter.DateConverter
import com.nicha.eventticketing.data.local.dao.EventDao
import com.nicha.eventticketing.data.local.dao.PaymentDao
import com.nicha.eventticketing.data.local.dao.TicketDao
import com.nicha.eventticketing.data.local.dao.TicketTypeDao
import com.nicha.eventticketing.data.local.dao.UserDao
import com.nicha.eventticketing.data.local.entity.EventEntity
import com.nicha.eventticketing.data.local.entity.PaymentEntity
import com.nicha.eventticketing.data.local.entity.TicketEntity
import com.nicha.eventticketing.data.local.entity.TicketTypeEntity
import com.nicha.eventticketing.data.local.entity.UserEntity

@Database(
    entities = [
        EventEntity::class,
        UserEntity::class,
        TicketEntity::class,
        TicketTypeEntity::class,
        PaymentEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun ticketDao(): TicketDao
    abstract fun ticketTypeDao(): TicketTypeDao
    abstract fun paymentDao(): PaymentDao
} 