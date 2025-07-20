package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.data.remote.dto.category.CategoryResponse
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository xử lý các chức năng liên quan đến danh mục sự kiện
 */
interface CategoryRepository {
    /**
     * Lấy danh sách danh mục
     * @param includeInactive Có bao gồm các danh mục không hoạt động không
     * @param page Số trang
     * @param size Số lượng mỗi trang
     * @return Flow<Resource<CategoryResponse>> Flow chứa danh sách danh mục theo trang
     */
    fun getCategories(
        includeInactive: Boolean = false, 
        page: Int = 0, 
        size: Int = 50
    ): Flow<Resource<CategoryResponse>>
    
    /**
     * Lấy thông tin chi tiết của một danh mục
     * @param categoryId ID của danh mục
     * @return Flow<Resource<CategoryDto>> Flow chứa thông tin danh mục
     */
    fun getCategoryById(categoryId: String): Flow<Resource<CategoryDto>>
    
    /**
     * Tạo danh mục mới
     * @param category Thông tin danh mục cần tạo
     * @return Flow<Resource<CategoryDto>> Flow chứa thông tin danh mục đã tạo
     */
    fun createCategory(category: CategoryDto): Flow<Resource<CategoryDto>>
    
    /**
     * Cập nhật thông tin danh mục
     * @param categoryId ID của danh mục
     * @param category Thông tin danh mục cần cập nhật
     * @return Flow<Resource<CategoryDto>> Flow chứa thông tin danh mục đã cập nhật
     */
    fun updateCategory(categoryId: String, category: CategoryDto): Flow<Resource<CategoryDto>>
    
    /**
     * Xóa danh mục
     * @param categoryId ID của danh mục
     * @return Flow<Resource<Boolean>> Flow chứa kết quả xóa
     */
    fun deleteCategory(categoryId: String): Flow<Resource<Boolean>>
} 