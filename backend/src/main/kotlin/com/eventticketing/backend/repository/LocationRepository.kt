package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.Location
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LocationRepository : JpaRepository<Location, UUID> {
    fun findByCityIgnoreCase(city: String, pageable: Pageable): Page<Location>
    
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Location>
    
    @Query(
        value = "SELECT l.*, " +
                "(6371 * acos(cos(radians(:latitude)) * cos(radians(l.latitude)) * cos(radians(l.longitude) - " +
                "radians(:longitude)) + sin(radians(:latitude)) * sin(radians(l.latitude)))) as distance " +
                "FROM locations l " +
                "WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(l.latitude)) * cos(radians(l.longitude) - " +
                "radians(:longitude)) + sin(radians(:latitude)) * sin(radians(l.latitude)))) < :radius " +
                "ORDER BY distance",
        nativeQuery = true
    )
    fun findNearbyLocations(
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
        @Param("radius") radius: Double,
        pageable: Pageable
    ): Page<Location>
    
    @Query(
        value = "SELECT l.* FROM locations l " +
                "JOIN events e ON l.id = e.location_id " +
                "GROUP BY l.id " +
                "ORDER BY COUNT(e.id) DESC",
        nativeQuery = true
    )
    fun findPopularLocations(pageable: Pageable): List<Location>
} 