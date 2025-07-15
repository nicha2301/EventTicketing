package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import retrofit2.HttpException
import com.google.gson.Gson

/**
 * ViewModel để quản lý dữ liệu danh mục
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    
    private val _categoriesState = MutableStateFlow<ResourceState<List<CategoryDto>>>(ResourceState.Initial)
    val categoriesState: StateFlow<ResourceState<List<CategoryDto>>> = _categoriesState.asStateFlow()
    
    private val _categoryDetailState = MutableStateFlow<ResourceState<CategoryDto>>(ResourceState.Initial)
    val categoryDetailState: StateFlow<ResourceState<CategoryDto>> = _categoryDetailState.asStateFlow()
    
    fun getCategories(includeInactive: Boolean = false, page: Int = 0, size: Int = 50) {
        _categoriesState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy danh sách danh mục")
                val response = apiService.getCategories(includeInactive, page, size)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val categoryResponse = response.body()?.data
                    if (categoryResponse != null) {
                        val categories = categoryResponse.content ?: emptyList()
                        Timber.d("Nhận được dữ liệu danh mục: ${categories.size} danh mục")
                        
                        _categoriesState.value = ResourceState.Success(categories)
                        Timber.d("Lấy danh sách danh mục thành công")
                    } else {
                        Timber.e("Không thể lấy danh sách danh mục từ response")
                        _categoriesState.value = ResourceState.Error("Không thể lấy danh sách danh mục")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy danh sách danh mục"
                    Timber.e("Lấy danh sách danh mục thất bại: $errorMessage")
                    _categoriesState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy danh sách danh mục", _categoriesState)
            }
        }
    }
    
    fun getCategoryById(categoryId: String) {
        _categoryDetailState.value = ResourceState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy chi tiết danh mục: $categoryId")
                val response = apiService.getCategoryById(categoryId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val category = response.body()?.data
                    if (category != null) {
                        _categoryDetailState.value = ResourceState.Success(category)
                        Timber.d("Lấy chi tiết danh mục thành công: ${category.name}")
                    } else {
                        Timber.e("Không thể lấy chi tiết danh mục từ response")
                        _categoryDetailState.value = ResourceState.Error("Không thể lấy chi tiết danh mục")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy chi tiết danh mục"
                    Timber.e("Lấy chi tiết danh mục thất bại: $errorMessage")
                    _categoryDetailState.value = ResourceState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy chi tiết danh mục", _categoryDetailState)
            }
        }
    }
    
    private fun <T> handleNetworkError(exception: Exception, action: String, stateFlow: MutableStateFlow<ResourceState<T>>) {
        when (exception) {
            is UnknownHostException -> {
                Timber.e(exception, "Lỗi kết nối: Không thể kết nối đến máy chủ")
                stateFlow.value = ResourceState.Error("Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng của bạn.")
            }
            is SocketTimeoutException -> {
                Timber.e(exception, "Lỗi kết nối: Kết nối bị timeout")
                stateFlow.value = ResourceState.Error("Kết nối bị timeout. Vui lòng thử lại sau.")
            }
            is IOException -> {
                Timber.e(exception, "Lỗi kết nối: IOException")
                stateFlow.value = ResourceState.Error("Lỗi kết nối: ${exception.message ?: "Không xác định"}")
            }
            is HttpException -> {
                Timber.e(exception, "Lỗi HTTP: ${exception.code()}")
                stateFlow.value = ResourceState.Error("Lỗi máy chủ: ${exception.message()}")
            }
            else -> {
                Timber.e(exception, "Lỗi không xác định khi $action")
                stateFlow.value = ResourceState.Error("Lỗi không xác định: ${exception.message ?: "Unknown"}")
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