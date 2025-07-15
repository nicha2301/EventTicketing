package com.nicha.eventticketing.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nicha.eventticketing.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: String): Flow<EventEntity?>

    @Query("SELECT * FROM events ORDER BY startDate ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY startDate ASC")
    fun getEventsPagingSource(): PagingSource<Int, EventEntity>

    @Query("SELECT * FROM events WHERE startDate >= :now ORDER BY startDate ASC LIMIT :limit")
    fun getUpcomingEvents(now: Date, limit: Int): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE isFavorite = 1")
    fun getFavoriteEvents(): Flow<List<EventEntity>>

    @Query("UPDATE events SET isFavorite = :isFavorite WHERE id = :eventId")
    suspend fun updateFavoriteStatus(eventId: String, isFavorite: Boolean)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
} 