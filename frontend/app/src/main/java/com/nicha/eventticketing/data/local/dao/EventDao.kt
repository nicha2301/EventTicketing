package com.nicha.eventticketing.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nicha.eventticketing.data.local.entity.EventEntity

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("SELECT * FROM events")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    suspend fun getEventById(eventId: String): EventEntity?

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
} 