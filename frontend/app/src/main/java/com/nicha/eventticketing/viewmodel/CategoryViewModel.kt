package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel để quản lý dữ liệu danh mục
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _categoriesState = MutableStateFlow<ResourceState<List<CategoryDto>>>(ResourceState.Initial)
    val categoriesState: StateFlow<ResourceState<List<CategoryDto>>> = _categoriesState.asStateFlow()
    
    private val _categoryDetailState = MutableStateFlow<ResourceState<CategoryDto>>(ResourceState.Initial)
    val categoryDetailState: StateFlow<ResourceState<CategoryDto>> = _categoryDetailState.asStateFlow()
    
    fun getCategories(includeInactive: Boolean = false, page: Int = 0, size: Int = 50) {
        _categoriesState.value = ResourceState.Loading
        
        viewModelScope.launch {
            categoryRepository.getCategories(includeInactive, page, size).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val categoryResponse = result.data
                        if (categoryResponse != null) {
                            val categories = categoryResponse.content ?: emptyList()
                            Timber.d("Nhận được dữ liệu danh mục: ${categories.size} danh mục")
                            
                            _categoriesState.value = ResourceState.Success(categories)
                            Timber.d("Lấy danh sách danh mục thành công")
                        } else {
                            Timber.e("Không tìm thấy danh mục")
                            _categoriesState.value = ResourceState.Error("Không tìm thấy danh mục")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy danh sách danh mục thất bại: ${result.message}")
                        _categoriesState.value = ResourceState.Error(result.message ?: "Không thể lấy danh sách danh mục")
                    }
                    is Resource.Loading -> {
                        _categoriesState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    fun getCategoryById(categoryId: String) {
        _categoryDetailState.value = ResourceState.Loading
        
        viewModelScope.launch {
            categoryRepository.getCategoryById(categoryId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val category = result.data
                        if (category != null) {
                            _categoryDetailState.value = ResourceState.Success(category)
                            Timber.d("Lấy chi tiết danh mục thành công: ${category.name}")
                        } else {
                            Timber.e("Không tìm thấy danh mục")
                            _categoryDetailState.value = ResourceState.Error("Không tìm thấy danh mục")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy chi tiết danh mục thất bại: ${result.message}")
                        _categoryDetailState.value = ResourceState.Error(result.message ?: "Không thể lấy chi tiết danh mục")
                    }
                    is Resource.Loading -> {
                        _categoryDetailState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    fun resetError() {
        if (_categoriesState.value is ResourceState.Error) {
            _categoriesState.value = ResourceState.Initial
        }
        
        if (_categoryDetailState.value is ResourceState.Error) {
            _categoryDetailState.value = ResourceState.Initial
        }
    }
} 