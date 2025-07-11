package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.User
import com.eventticketing.backend.entity.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun findAllByRole(role: UserRole): List<User>
    fun findByEnabled(enabled: Boolean): List<User>
} 