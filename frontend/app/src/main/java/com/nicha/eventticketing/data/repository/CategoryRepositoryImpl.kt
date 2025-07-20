package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.data.remote.dto.category.CategoryResponse
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : CategoryRepository {
    
    override fun getCategories(
        includeInactive: Boolean, 
        page: Int, 
        size: Int
    ): Flow<Resource<CategoryResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getCategories(includeInactive, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                val categories = response.body()?.data
                if (categories != null) {
                    emit(Resource.Success(categories))
                } else {
                    emit(Resource.Error("Không tìm thấy danh mục"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách danh mục"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách danh mục")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getCategoryById(categoryId: String): Flow<Resource<CategoryDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getCategoryById(categoryId)
            if (response.isSuccessful && response.body()?.success == true) {
                val category = response.body()?.data
                if (category != null) {
                    emit(Resource.Success(category))
                } else {
                    emit(Resource.Error("Không tìm thấy danh mục"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Không thể lấy thông tin danh mục"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy thông tin danh mục: $categoryId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    //Chưa triển khai
    override fun createCategory(category: CategoryDto): Flow<Resource<CategoryDto>> = flow {
        emit(Resource.Loading())
        try {

            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi tạo danh mục")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    //Chưa triển khai
    override fun updateCategory(
        categoryId: String, 
        category: CategoryDto
    ): Flow<Resource<CategoryDto>> = flow {
        emit(Resource.Loading())
        try {
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật danh mục: $categoryId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    //Chưa triển khai
    override fun deleteCategory(categoryId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xóa danh mục: $categoryId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
} 