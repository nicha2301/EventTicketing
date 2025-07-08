package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CategoryRepository : JpaRepository<Category, UUID> {
    fun findByNameIgnoreCase(name: String): Optional<Category>
    fun findByIsActiveTrue(pageable: Pageable): Page<Category>
    fun existsByNameIgnoreCase(name: String): Boolean
} 