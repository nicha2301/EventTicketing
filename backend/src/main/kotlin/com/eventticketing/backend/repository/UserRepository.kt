package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.User
import com.eventticketing.backend.entity.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun findAllByRole(role: UserRole): List<User>
    fun findByEnabled(enabled: Boolean): List<User>
    
    @Query("""
        SELECT u.gender, COUNT(u) 
        FROM User u 
        JOIN Ticket t ON u.id = t.user.id 
        WHERE t.event.id = :eventId 
        AND u.gender IS NOT NULL 
        GROUP BY u.gender
    """)
    fun getGenderDistributionForEvent(@Param("eventId") eventId: UUID): List<Array<Any>>
    
    @Query("""
        SELECT CASE 
                WHEN EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM u.birthDate) < 18 THEN 'Under 18'
                WHEN EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM u.birthDate) BETWEEN 18 AND 24 THEN '18-24'
                WHEN EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM u.birthDate) BETWEEN 25 AND 34 THEN '25-34'
                WHEN EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM u.birthDate) BETWEEN 35 AND 44 THEN '35-44'
                WHEN EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM u.birthDate) BETWEEN 45 AND 54 THEN '45-54'
                ELSE '55+'
               END as ageGroup,
               COUNT(u)
        FROM User u 
        JOIN Ticket t ON u.id = t.user.id 
        WHERE t.event.id = :eventId 
        AND u.birthDate IS NOT NULL 
        GROUP BY ageGroup
    """)
    fun getAgeDistributionForEvent(@Param("eventId") eventId: UUID): List<Array<Any>>
    
    @Query("""
        SELECT u.location, COUNT(u) 
        FROM User u 
        JOIN Ticket t ON u.id = t.user.id 
        WHERE t.event.id = :eventId 
        AND u.location IS NOT NULL 
        GROUP BY u.location
    """)
    fun getLocationDistributionForEvent(@Param("eventId") eventId: UUID): List<Array<Any>>
} 