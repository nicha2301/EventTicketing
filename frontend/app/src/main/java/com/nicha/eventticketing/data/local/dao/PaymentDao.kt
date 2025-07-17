package com.nicha.eventticketing.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nicha.eventticketing.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<PaymentEntity>)

    @Update
    suspend fun update(payment: PaymentEntity)

    @Delete
    suspend fun delete(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getById(id: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE id = :id")
    fun getByIdAsFlow(id: String): Flow<PaymentEntity?>

    @Query("SELECT * FROM payments WHERE userId = :userId")
    suspend fun getByUserId(userId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE userId = :userId")
    fun getByUserIdAsFlow(userId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE orderCode = :orderCode")
    suspend fun getByOrderCode(orderCode: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE status = :status")
    suspend fun getByStatus(status: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE userId = :userId AND status = :status")
    suspend fun getByUserIdAndStatus(userId: String, status: String): List<PaymentEntity>

    @Query("SELECT * FROM payments")
    fun getAllAsFlow(): Flow<List<PaymentEntity>>
} 