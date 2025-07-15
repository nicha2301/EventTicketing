package com.nicha.eventticketing.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "ticket_types",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("eventId")]
)
data class TicketTypeEntity(
    @PrimaryKey
    val id: String,
    val eventId: String,
    val name: String,
    val description: String?,
    val price: Double,
    val quantity: Int,
    val quantitySold: Int,
    val maxPerOrder: Int,
    val minPerOrder: Int = 1,
    val saleStartDate: Date?,
    val saleEndDate: Date?,
    val active: Boolean,
    val createdAt: Date,
    val updatedAt: Date,
    val lastUpdatedLocally: Date = Date()
) 