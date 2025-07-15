package com.nicha.eventticketing.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nicha.eventticketing.data.local.entity.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ticket: TicketEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tickets: List<TicketEntity>)

    @Update
    suspend fun update(ticket: TicketEntity)

    @Delete
    suspend fun delete(ticket: TicketEntity)

    @Query("DELETE FROM tickets WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getById(id: String): TicketEntity?

    @Query("SELECT * FROM tickets WHERE id = :id")
    fun getByIdAsFlow(id: String): Flow<TicketEntity?>

    @Query("SELECT * FROM tickets WHERE userId = :userId")
    suspend fun getByUserId(userId: String): List<TicketEntity>

    @Query("SELECT * FROM tickets WHERE userId = :userId")
    fun getByUserIdAsFlow(userId: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE eventId = :eventId")
    suspend fun getByEventId(eventId: String): List<TicketEntity>

    @Query("SELECT * FROM tickets WHERE eventId = :eventId")
    fun getByEventIdAsFlow(eventId: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE ticketTypeId = :ticketTypeId")
    suspend fun getByTicketTypeId(ticketTypeId: String): List<TicketEntity>

    @Query("SELECT * FROM tickets WHERE userId = :userId AND eventId = :eventId")
    suspend fun getByUserIdAndEventId(userId: String, eventId: String): List<TicketEntity>

    @Query("SELECT * FROM tickets WHERE orderCode = :orderCode")
    suspend fun getByOrderCode(orderCode: String): List<TicketEntity>

    @Query("SELECT * FROM tickets WHERE status = :status")
    suspend fun getByStatus(status: String): List<TicketEntity>

    @Query("SELECT * FROM tickets WHERE userId = :userId AND status = :status")
    suspend fun getByUserIdAndStatus(userId: String, status: String): List<TicketEntity>

    @Query("SELECT * FROM tickets")
    fun getAllAsFlow(): Flow<List<TicketEntity>>
} 