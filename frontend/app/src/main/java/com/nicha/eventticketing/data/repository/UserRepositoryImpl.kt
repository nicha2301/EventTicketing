package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.auth.ChangePasswordDto
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.data.remote.dto.auth.UserUpdateDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.UserRepository
import com.nicha.eventticketing.util.NetworkUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override fun getCurrentUser(): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Không thể lấy thông tin người dùng"))
                }
            } else {
                val errorBody = NetworkUtil.parseErrorResponse<ApiResponse<Any>>(response.errorBody())
                emit(Resource.Error(errorBody?.message ?: "Lỗi mạng khi lấy thông tin người dùng"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy thông tin người dùng")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi khi lấy thông tin người dùng"))
        }
    }

    override fun updateUserProfile(userUpdateDto: UserUpdateDto): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.updateUser(userUpdateDto)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Không thể cập nhật thông tin người dùng"))
                }
            } else {
                val errorBody = NetworkUtil.parseErrorResponse<ApiResponse<Any>>(response.errorBody())
                emit(Resource.Error(errorBody?.message ?: "Lỗi mạng khi cập nhật thông tin"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật thông tin người dùng")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi khi cập nhật thông tin"))
        }
    }

    override fun updateUserAvatar(image: MultipartBody.Part): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {

            emit(Resource.Error("Chức năng đang được phát triển"))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật ảnh đại diện")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi khi cập nhật ảnh đại diện"))
        }
    }

    override fun changePassword(oldPassword: String, newPassword: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val changePasswordDto = ChangePasswordDto(
                currentPassword = oldPassword,
                newPassword = newPassword
            )

            emit(Resource.Error("Chức năng đang được phát triển"))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi thay đổi mật khẩu")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi khi thay đổi mật khẩu"))
        }
    }
} 