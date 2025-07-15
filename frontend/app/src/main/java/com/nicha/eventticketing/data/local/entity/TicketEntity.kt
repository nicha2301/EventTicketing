package com.nicha.eventticketing.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(
            entity = TicketTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["ticketTypeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("ticketTypeId"),
        Index("userId"),
        Index("eventId")
    ]
)
data class TicketEntity(
    @PrimaryKey
    val id: String,
    val ticketTypeId: String,
    val userId: String,
    val eventId: String,
    val orderCode: String,
    val qrCode: String?,
    val price: Double,
    val status: String, // RESERVED, PAID, CANCELLED, USED
    val checkedIn: Boolean = false,
    val checkedInAt: Date? = null,
    val purchasedAt: Date,
    val createdAt: Date,
    val updatedAt: Date,
    val lastUpdatedLocally: Date = Date()
) 