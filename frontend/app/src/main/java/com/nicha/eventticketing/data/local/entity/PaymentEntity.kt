package com.nicha.eventticketing.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val amount: Double,
    val currency: String = "VND",
    val paymentMethod: String, // VNPAY, STRIPE, CASH
    val status: String, // PENDING, COMPLETED, FAILED, REFUNDED
    val transactionCode: String?,
    val orderCode: String,
    val refundAmount: Double? = null,
    val refundedAt: Date? = null,
    val createdAt: Date,
    val updatedAt: Date,
    val lastUpdatedLocally: Date = Date()
) 