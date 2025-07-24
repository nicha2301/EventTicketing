package com.nicha.eventticketing.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nicha.eventticketing.data.local.entity.TicketEntity

@Dao
interface TicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    @Query("SELECT * FROM tickets")
    suspend fun getAllTickets(): List<TicketEntity>

    @Query("SELECT * FROM tickets WHERE id = :ticketId LIMIT 1")
    suspend fun getTicketById(ticketId: String): TicketEntity?

    @Query("DELETE FROM tickets")
    suspend fun deleteAllTickets()
} 