package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.dto.CategoryDto
import com.eventticketing.backend.service.CategoryService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getAllCategories(
        @PageableDefault(size = 20, sort = ["name"], direction = Sort.Direction.ASC) pageable: Pageable,
        @RequestParam(defaultValue = "false") includeInactive: Boolean
    ): ResponseEntity<ApiResponse<Page<CategoryDto>>> {
        val categories = if (includeInactive) {
            categoryService.getAllCategories(pageable)
        } else {
            categoryService.getActiveCategories(pageable)
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy danh sách danh mục thành công",
                categories
            )
        )
    }

    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: UUID): ResponseEntity<ApiResponse<CategoryDto>> {
        val category = categoryService.getCategoryById(id)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã lấy thông tin danh mục thành công",
                category
            )
        )
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createCategory(@Valid @RequestBody categoryDto: CategoryDto): ResponseEntity<ApiResponse<CategoryDto>> {
        val createdCategory = categoryService.createCategory(categoryDto)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                "Đã tạo danh mục thành công",
                createdCategory
            )
        )
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateCategory(
        @PathVariable id: UUID,
        @Valid @RequestBody categoryDto: CategoryDto
    ): ResponseEntity<ApiResponse<CategoryDto>> {
        val updatedCategory = categoryService.updateCategory(id, categoryDto)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã cập nhật danh mục thành công",
                updatedCategory
            )
        )
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteCategory(@PathVariable id: UUID): ResponseEntity<ApiResponse<Boolean>> {
        val result = categoryService.deleteCategory(id)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã xóa danh mục thành công",
                result
            )
        )
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    fun activateCategory(@PathVariable id: UUID): ResponseEntity<ApiResponse<CategoryDto>> {
        val activatedCategory = categoryService.setActiveStatus(id, true)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã kích hoạt danh mục thành công",
                activatedCategory
            )
        )
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    fun deactivateCategory(@PathVariable id: UUID): ResponseEntity<ApiResponse<CategoryDto>> {
        val deactivatedCategory = categoryService.setActiveStatus(id, false)
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Đã vô hiệu hóa danh mục thành công",
                deactivatedCategory
            )
        )
    }
} 