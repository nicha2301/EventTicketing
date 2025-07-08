package com.eventticketing.backend.service

import com.eventticketing.backend.dto.CategoryDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface CategoryService {
    /**
     * Lấy tất cả danh mục, kể cả danh mục không hoạt động
     */
    fun getAllCategories(pageable: Pageable): Page<CategoryDto>
    
    /**
     * Lấy các danh mục đang hoạt động
     */
    fun getActiveCategories(pageable: Pageable): Page<CategoryDto>
    
    /**
     * Lấy thông tin danh mục theo ID
     */
    fun getCategoryById(id: UUID): CategoryDto
    
    /**
     * Tạo danh mục mới
     */
    fun createCategory(categoryDto: CategoryDto): CategoryDto
    
    /**
     * Cập nhật danh mục
     */
    fun updateCategory(id: UUID, categoryDto: CategoryDto): CategoryDto
    
    /**
     * Xóa danh mục
     */
    fun deleteCategory(id: UUID): Boolean
    
    /**
     * Kích hoạt hoặc vô hiệu hóa danh mục
     */
    fun setActiveStatus(id: UUID, isActive: Boolean): CategoryDto
} 