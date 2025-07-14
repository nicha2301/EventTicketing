package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.TokenBlacklist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface TokenBlacklistRepository : JpaRepository<TokenBlacklist, UUID> {
    
    /**
     * Tìm token trong blacklist
     */
    fun findByToken(token: String): Optional<TokenBlacklist>
    
    /**
     * Kiểm tra token có tồn tại trong blacklist không
     */
    fun existsByToken(token: String): Boolean
    
    /**
     * Xóa token theo username
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.username = :username")
    fun deleteByUsername(@Param("username") username: String)
    
    /**
     * Xóa các token đã hết hạn
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiryDate < :currentTime")
    fun deleteExpiredTokens(@Param("currentTime") currentTime: LocalDateTime = LocalDateTime.now())
    
    /**
     * Đếm số token đã hết hạn
     */
    @Query("SELECT COUNT(t) FROM TokenBlacklist t WHERE t.expiryDate < :currentTime")
    fun countExpiredTokens(@Param("currentTime") currentTime: LocalDateTime = LocalDateTime.now()): Long
} 