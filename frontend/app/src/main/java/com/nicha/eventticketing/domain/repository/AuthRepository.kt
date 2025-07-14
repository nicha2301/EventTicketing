package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.local.dao.UserDao
import com.nicha.eventticketing.data.local.entity.UserEntity
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.dto.auth.LoginRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.UserCreateDto
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Repository cho chức năng xác thực của ứng dụng
 */
interface AuthRepository {
    /**
     * Đăng ký người dùng mới
     * @param userCreateDto Thông tin đăng ký
     * @return Flow của kết quả đăng ký
     */
    suspend fun register(userCreateDto: UserCreateDto): Flow<Resource<UserDto>>
    
    /**
     * Đăng nhập với email và mật khẩu
     * @param email Email
     * @param password Mật khẩu
     * @return Flow của kết quả đăng nhập
     */
    suspend fun login(email: String, password: String): Flow<Resource<Boolean>>
    
    /**
     * Đăng xuất
     * @return Flow của kết quả đăng xuất
     */
    suspend fun logout(): Flow<Resource<Boolean>>
    
    /**
     * Lấy thông tin người dùng hiện tại
     * @return Flow của thông tin người dùng
     */
    suspend fun getCurrentUser(): Flow<Resource<UserDto>>
    
    /**
     * Kiểm tra trạng thái đăng nhập
     * @return Flow của trạng thái đăng nhập
     */
    fun isLoggedIn(): Flow<Boolean>
}

/**
 * Triển khai của AuthRepository
 */
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    /**
     * Đăng ký người dùng mới
     */
    override suspend fun register(userCreateDto: UserCreateDto): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())
        
        try {
            Timber.d("Đang đăng ký người dùng: ${userCreateDto.email}")
            val response = apiService.register(userCreateDto)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                if (userDto != null) {
                    Timber.d("Đăng ký thành công: ${userDto.email}")
                    emit(Resource.Success(userDto))
                } else {
                    Timber.e("Đăng ký thành công nhưng không nhận được dữ liệu người dùng")
                    emit(Resource.Error("Đăng ký thành công nhưng không nhận được dữ liệu người dùng"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Đăng ký thất bại"
                Timber.e("Đăng ký thất bại: $errorMessage")
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đăng ký: ${e.message}")
            emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    /**
     * Đăng nhập với email và mật khẩu
     */
    override suspend fun login(email: String, password: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        
        try {
            Timber.d("Đang đăng nhập với email: $email")
            val loginRequest = LoginRequestDto(email, password)
            val response = apiService.login(loginRequest)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val authResponse = response.body()?.data
                if (authResponse != null) {
                    // Lưu token
                    val saveResult = preferencesManager.saveAuthToken(authResponse.token)
                    if (!saveResult) {
                        Timber.e("Không thể lưu token xác thực")
                        emit(Resource.Error("Không thể lưu token xác thực"))
                        return@flow
                    }
                    
                    // Lưu thông tin user vào database local
                    saveUserToLocalDb(authResponse)
                    
                    Timber.d("Đăng nhập thành công: $email")
                    emit(Resource.Success(true))
                } else {
                    Timber.e("Đăng nhập thành công nhưng không nhận được dữ liệu người dùng")
                    emit(Resource.Error("Không nhận được dữ liệu người dùng"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Đăng nhập thất bại"
                Timber.e("Đăng nhập thất bại: $errorMessage")
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đăng nhập: ${e.message}")
            emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    /**
     * Lưu thông tin người dùng vào cơ sở dữ liệu local
     */
    private suspend fun saveUserToLocalDb(authResponse: com.nicha.eventticketing.data.remote.dto.auth.UserAuthResponseDto) {
        try {
            // Tạo UserDto từ dữ liệu trong UserAuthResponseDto
            val userDto = UserDto(
                id = authResponse.id,
                email = authResponse.email,
                fullName = authResponse.fullName,
                phoneNumber = null,  // Không có trong response
                role = authResponse.role,
                enabled = true,      // Giả định đã active vì đã đăng nhập thành công
                profilePictureUrl = authResponse.profilePictureUrl,
                createdAt = null     // Không có trong response
            )
            
            // Lưu thông tin user vào database local
            val userEntity = UserEntity(
                id = userDto.id,
                email = userDto.email,
                fullName = userDto.fullName,
                phoneNumber = userDto.phoneNumber,
                address = null,
                bio = null,
                role = userDto.role,
                enabled = userDto.enabled,
                createdAt = Date() // Sử dụng thời gian hiện tại thay vì null
            )
            userDao.insertUser(userEntity)
            Timber.d("Đã lưu thông tin người dùng vào local database: ${userEntity.email}")
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lưu thông tin người dùng vào database local")
        }
    }

    /**
     * Đăng xuất
     */
    override suspend fun logout(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        
        try {
            val result = preferencesManager.clearAuthToken()
            if (result) {
                Timber.d("Đăng xuất thành công")
            emit(Resource.Success(true))
            } else {
                Timber.e("Không thể xóa token khi đăng xuất")
                emit(Resource.Error("Không thể đăng xuất hoàn toàn"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đăng xuất: ${e.message}")
            emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định khi đăng xuất"))
        }
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    override suspend fun getCurrentUser(): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())
        
        try {
            Timber.d("Đang lấy thông tin người dùng hiện tại")
            val response = apiService.getCurrentUser()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                userDto?.let {
                    // Cập nhật thông tin user trong database local
                    updateUserInLocalDb(it)
                    
                    Timber.d("Lấy thông tin người dùng thành công: ${it.email}")
                    emit(Resource.Success(it))
                } ?: run {
                    Timber.e("Không nhận được dữ liệu người dùng")
                    emit(Resource.Error("Không nhận được dữ liệu người dùng"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Không thể lấy thông tin người dùng"
                Timber.e("Lấy thông tin người dùng thất bại: $errorMessage")
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy thông tin người dùng: ${e.message}")
            emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    /**
     * Cập nhật thông tin người dùng trong cơ sở dữ liệu local
     */
    private suspend fun updateUserInLocalDb(userDto: UserDto) {
        try {
            val userEntity = UserEntity(
                id = userDto.id,
                email = userDto.email,
                fullName = userDto.fullName,
                phoneNumber = userDto.phoneNumber,
                address = null,
                bio = null,
                role = userDto.role,
                enabled = userDto.enabled,
                createdAt = parseDate(userDto.createdAt)
            )
            userDao.insertUser(userEntity)
            Timber.d("Đã cập nhật thông tin người dùng trong local database: ${userEntity.email}")
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật thông tin người dùng trong database local")
        }
    }

    /**
     * Kiểm tra trạng thái đăng nhập
     */
    override fun isLoggedIn(): Flow<Boolean> {
        return preferencesManager.getAuthToken()
            .map { token -> token != null && token.isNotBlank() }
            .catch { 
                Timber.e(it, "Lỗi khi kiểm tra đăng nhập")
                emit(false)
        }
    }
    
    /**
     * Chuyển đổi chuỗi ngày thành đối tượng Date
     */
    private fun parseDate(dateString: String?): Date {
        return if (dateString.isNullOrEmpty()) {
            Date()
        } else {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                format.parse(dateString) ?: Date()
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi parse date: $dateString")
                Date()
            }
        }
    }
} 