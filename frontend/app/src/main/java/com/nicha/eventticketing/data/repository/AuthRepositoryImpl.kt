package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.dto.auth.ForgotPasswordRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.GoogleAuthRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.LoginRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.LogoutRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.ResetPasswordRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.UserCreateDto
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Triển khai của AuthRepository
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
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
                    
                    // Lưu userId
                    val saveUserIdResult = preferencesManager.saveUserId(authResponse.id)
                    if (!saveUserIdResult) {
                        Timber.e("Không thể lưu userId: ${authResponse.id}")
                    } else {
                        Timber.d("Đã lưu userId: ${authResponse.id}")
                    }

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
     * Đăng nhập với Google
     */
    override suspend fun loginWithGoogle(googleAuthRequest: GoogleAuthRequestDto): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        
        try {
            Timber.d("Đang đăng nhập bằng Google với email: ${googleAuthRequest.email}")
            val response = apiService.loginWithGoogle(googleAuthRequest)
            
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
                    
                    // Lưu userId
                    val saveUserIdResult = preferencesManager.saveUserId(authResponse.id)
                    if (!saveUserIdResult) {
                        Timber.e("Không thể lưu userId: ${authResponse.id}")
                    } else {
                        Timber.d("Đã lưu userId: ${authResponse.id}")
                    }
                    
                    Timber.d("Đăng nhập Google thành công: ${googleAuthRequest.email}")
                    emit(Resource.Success(true))
                } else {
                    Timber.e("Đăng nhập Google thành công nhưng không nhận được dữ liệu người dùng")
                    emit(Resource.Error("Không nhận được dữ liệu người dùng"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Đăng nhập Google thất bại"
                Timber.e("Đăng nhập Google thất bại: $errorMessage")
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đăng nhập Google: ${e.message}")
            emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    /**
     * Gửi yêu cầu quên mật khẩu
     */
    override suspend fun forgotPassword(email: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        
        try {
            Timber.d("Đang gửi yêu cầu quên mật khẩu cho email: $email")
            val response = apiService.forgotPassword(email)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val message = response.body()?.message ?: "Liên kết đặt lại mật khẩu đã được gửi đến email của bạn"
                Timber.d("Gửi yêu cầu quên mật khẩu thành công")
                emit(Resource.Success(message))
            } else {
                val errorMessage = response.body()?.message ?: "Không thể gửi yêu cầu đặt lại mật khẩu"
                Timber.e("Gửi yêu cầu quên mật khẩu thất bại: $errorMessage")
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi gửi yêu cầu quên mật khẩu: ${e.message}")
            emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    /**
     * Đặt lại mật khẩu
     */
    override suspend fun resetPassword(token: String, password: String, confirmPassword: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        
        try {
            Timber.d("Đang đặt lại mật khẩu")
            val request = ResetPasswordRequestDto(token, password, confirmPassword)
            val response = apiService.resetPassword(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val message = response.body()?.message ?: "Đặt lại mật khẩu thành công"
                Timber.d("Đặt lại mật khẩu thành công")
                emit(Resource.Success(message))
            } else {
                val errorMessage = response.body()?.message ?: "Không thể đặt lại mật khẩu"
                Timber.e("Đặt lại mật khẩu thất bại: $errorMessage")
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi đặt lại mật khẩu: ${e.message}")
            emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    /**
     * Đăng xuất
     */
    override suspend fun logout(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            val tokenResult = preferencesManager.clearAuthToken()
            val userIdResult = preferencesManager.saveUserId("")
            
            // Gọi API đăng xuất nếu có token
            try {
                val token = preferencesManager.getAuthToken().firstOrNull()
                if (!token.isNullOrBlank()) {
                    val request = LogoutRequestDto(token)
                    apiService.logout(request)
                    Timber.d("Đã gọi API đăng xuất")
                }
            } catch (e: Exception) {
                // Bỏ qua lỗi API, vì đã xóa token local
                Timber.e(e, "Lỗi khi gọi API đăng xuất: ${e.message}")
            }
            
            if (tokenResult) {
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
            val token = preferencesManager.getAuthToken().firstOrNull()
            
            if (token.isNullOrBlank()) {
                Timber.e("Không có token để lấy thông tin người dùng")
                emit(Resource.Error("Bạn chưa đăng nhập"))
                return@flow
            }
            
            val response = apiService.getCurrentUser()

            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                if (userDto != null) {
                    Timber.d("Lấy thông tin người dùng thành công: ${userDto.email}")
                    emit(Resource.Success(userDto))
                } else {
                    Timber.e("Không nhận được dữ liệu người dùng")
                    emit(Resource.Error("Không nhận được dữ liệu người dùng"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Lấy thông tin người dùng thất bại"
                Timber.e("Lấy thông tin người dùng thất bại: $errorMessage")
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy thông tin người dùng: ${e.message}")
            
            // Xử lý lỗi cụ thể
            val errorMessage = when (e) {
                is IOException -> "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet của bạn."
                else -> e.localizedMessage ?: "Đã xảy ra lỗi không xác định"
            }
            
            emit(Resource.Error(errorMessage))
        }
    }

    /**
     * Kiểm tra trạng thái đăng nhập
     */
    override fun isLoggedIn(): Flow<Boolean> {
        return flow {
            val token = preferencesManager.getAuthToken().firstOrNull()
            emit(token != null && token.isNotBlank())
        }
    }
} 