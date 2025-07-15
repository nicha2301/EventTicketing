package com.nicha.eventticketing.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nicha.eventticketing.data.local.entity.TicketTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ticketType: TicketTypeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ticketTypes: List<TicketTypeEntity>)

    @Update
    suspend fun update(ticketType: TicketTypeEntity)

    @Delete
    suspend fun delete(ticketType: TicketTypeEntity)

    @Query("DELETE FROM ticket_types WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM ticket_types WHERE eventId = :eventId")
    suspend fun deleteByEventId(eventId: String)

    @Query("SELECT * FROM ticket_types WHERE id = :id")
    suspend fun getById(id: String): TicketTypeEntity?

    @Query("SELECT * FROM ticket_types WHERE id = :id")
    fun getByIdAsFlow(id: String): Flow<TicketTypeEntity?>

    @Query("SELECT * FROM ticket_types WHERE eventId = :eventId")
    suspend fun getByEventId(eventId: String): List<TicketTypeEntity>

    @Query("SELECT * FROM ticket_types WHERE eventId = :eventId")
    fun getByEventIdAsFlow(eventId: String): Flow<List<TicketTypeEntity>>

    @Query("SELECT * FROM ticket_types WHERE eventId = :eventId AND active = 1")
    suspend fun getActiveByEventId(eventId: String): List<TicketTypeEntity>

    @Query("SELECT * FROM ticket_types")
    fun getAllAsFlow(): Flow<List<TicketTypeEntity>>
} 