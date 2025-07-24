package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.local.dao.UserDao
import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.auth.ChangePasswordDto
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.data.remote.dto.auth.UserUpdateDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.mapper.UserMapper
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.UserRepository
import com.nicha.eventticketing.util.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val userMapper: UserMapper
) : UserRepository {

    private var isOnline = true

    fun setNetworkStatus(online: Boolean) {
        Timber.d("[UserRepository] Cập nhật trạng thái mạng: $online")
        isOnline = online
    }
    
    override fun getCurrentUser(): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())
        
        val actuallyOnline = isOnline && NetworkUtil.isActuallyConnected()
        
        if (actuallyOnline) {
            try {
                val response = apiService.getCurrentUser()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        withContext(Dispatchers.IO) {
                            val userEntity = userMapper.dtoToEntity(body.data)
                            userDao.insertUser(userEntity)
                        }
                        emit(Resource.Success(body.data))
                    } else {
                        emit(Resource.Error(body?.message ?: "Không thể lấy thông tin người dùng"))
                    }
                } else {
                    val errorBody = NetworkUtil.parseErrorResponse<ApiResponse<Any>>(response.errorBody())
                    emit(Resource.Error(errorBody?.message ?: "Lỗi mạng khi lấy thông tin người dùng"))
                }
            } catch (e: Exception) {
                val cachedUser = withContext(Dispatchers.IO) {
                    userDao.getUser()
                }
                if (cachedUser != null) {
                    emit(Resource.Success(userMapper.entityToDto(cachedUser)))
                } else {
                    emit(Resource.Error(e.message ?: "Đã xảy ra lỗi khi lấy thông tin người dùng"))
                }
            }
        } else {
            val cachedUser = withContext(Dispatchers.IO) {
                userDao.getUser()
            }
            if (cachedUser != null) {
                emit(Resource.Success(userMapper.entityToDto(cachedUser)))
            } else {
                emit(Resource.Error("Không có dữ liệu offline và không có kết nối mạng"))
            }
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