package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.location.LocationDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.LocationRepository
import com.nicha.eventticketing.util.NetworkUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : LocationRepository {
    
    override fun getLocations(page: Int, size: Int): Flow<Resource<PageDto<LocationDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getLocations(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                val locations = response.body()?.data
                if (locations != null) {
                    emit(Resource.Success(locations))
                    Timber.d("Lấy danh sách vị trí thành công: ${locations.content?.size ?: 0} vị trí")
                } else {
                    emit(Resource.Error("Không tìm thấy vị trí"))
                    Timber.e("Không tìm thấy vị trí")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy danh sách vị trí"))
                Timber.e("Lấy danh sách vị trí thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách vị trí")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    // Chưa triển khai
    override fun getLocationById(locationId: String): Flow<Resource<LocationDto>> = flow {
        emit(Resource.Loading())
        try {
            
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
            Timber.e("API getLocationById chưa được hỗ trợ")
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy thông tin vị trí: $locationId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    // Chưa triển khai
    override fun getNearbyLocations(
        latitude: Double,
        longitude: Double,
        radius: Double,
        page: Int,
        size: Int
    ): Flow<Resource<PageDto<LocationDto>>> = flow {
        emit(Resource.Loading())
        try {

            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
            Timber.e("API getNearbyLocations chưa được hỗ trợ")
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách vị trí gần đây")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
} 