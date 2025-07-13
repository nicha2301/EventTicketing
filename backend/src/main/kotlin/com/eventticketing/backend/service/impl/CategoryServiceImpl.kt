package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.CategoryDto
import com.eventticketing.backend.entity.Category
import com.eventticketing.backend.exception.BadRequestException
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.repository.CategoryRepository
import com.eventticketing.backend.repository.EventRepository
import com.eventticketing.backend.service.CategoryService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val eventRepository: EventRepository
) : CategoryService {

    private val logger = LoggerFactory.getLogger(CategoryServiceImpl::class.java)

    override fun getAllCategories(pageable: Pageable): Page<CategoryDto> {
        return categoryRepository.findAll(pageable).map { mapToCategoryDto(it) }
    }

    override fun getActiveCategories(pageable: Pageable): Page<CategoryDto> {
        return categoryRepository.findByIsActiveTrue(pageable).map { mapToCategoryDto(it) }
    }

    override fun getCategoryById(id: UUID): CategoryDto {
        val category = findCategoryById(id)
        return mapToCategoryDto(category)
    }

    @Transactional
    override fun createCategory(categoryDto: CategoryDto): CategoryDto {
        // Kiểm tra tên danh mục đã tồn tại chưa
        if (categoryRepository.existsByNameIgnoreCase(categoryDto.name)) {
            throw BadRequestException("Tên danh mục '${categoryDto.name}' đã tồn tại")
        }

        val category = Category(
            name = categoryDto.name,
            description = categoryDto.description,
            iconUrl = categoryDto.iconUrl,
            isActive = categoryDto.isActive
        )

        val savedCategory = categoryRepository.save(category)
        logger.info("Đã tạo danh mục mới: ${savedCategory.id}")
        
        return mapToCategoryDto(savedCategory)
    }

    @Transactional
    override fun updateCategory(id: UUID, categoryDto: CategoryDto): CategoryDto {
        val category = findCategoryById(id)
        
        // Kiểm tra nếu tên đã thay đổi và đã tồn tại
        if (categoryDto.name.lowercase() != category.name.lowercase() && 
            categoryRepository.existsByNameIgnoreCase(categoryDto.name)) {
            throw BadRequestException("Tên danh mục '${categoryDto.name}' đã tồn tại")
        }

        // Cập nhật thông tin
        category.name = categoryDto.name
        category.description = categoryDto.description
        category.iconUrl = categoryDto.iconUrl
        category.updatedAt = LocalDateTime.now()

        val updatedCategory = categoryRepository.save(category)
        logger.info("Đã cập nhật danh mục: $id")
        
        return mapToCategoryDto(updatedCategory)
    }

    @Transactional
    override fun deleteCategory(id: UUID): Boolean {
        val category = findCategoryById(id)
        
        // Kiểm tra xem có sự kiện nào đang sử dụng danh mục này không
        val eventsCount = eventRepository.countByCategoryId(id)
        if (eventsCount > 0) {
            throw BadRequestException("Không thể xóa danh mục đang được sử dụng bởi $eventsCount sự kiện")
        }
        
        categoryRepository.delete(category)
        logger.info("Đã xóa danh mục: $id")
        
        return true
    }

    @Transactional
    override fun setActiveStatus(id: UUID, isActive: Boolean): CategoryDto {
        val category = findCategoryById(id)
        
        // Nếu trạng thái không thay đổi, trả về ngay
        if (category.isActive == isActive) {
            return mapToCategoryDto(category)
        }
        
        category.isActive = isActive
        category.updatedAt = LocalDateTime.now()
        
        val updatedCategory = categoryRepository.save(category)
        logger.info("Đã ${if (isActive) "kích hoạt" else "vô hiệu hóa"} danh mục: $id")
        
        return mapToCategoryDto(updatedCategory)
    }

    /**
     * Tìm Category theo ID
     */
    private fun findCategoryById(id: UUID): Category {
        return categoryRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy danh mục với ID $id") }
    }

    /**
     * Chuyển đổi Category thành CategoryDto
     */
    private fun mapToCategoryDto(category: Category): CategoryDto {
        return CategoryDto(
            id = category.id,
            name = category.name,
            description = category.description,
            iconUrl = category.iconUrl,
            isActive = category.isActive,
            createdAt = category.createdAt,
            updatedAt = category.updatedAt
        )
    }
} 