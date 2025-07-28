package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.auth.GoogleAuthManager
import com.nicha.eventticketing.data.preferences.PreferencesManager
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
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Triển khai của AuthRepository
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager,
    private val googleAuthManager: GoogleAuthManager
) : AuthRepository {

    /**
     * Đăng ký người dùng mới
     */
    override suspend fun register(userCreateDto: UserCreateDto): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.register(userCreateDto)

            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                if (userDto != null) {
                    emit(Resource.Success(userDto))
                } else {
                    emit(Resource.Error("Đăng ký thành công nhưng không nhận được dữ liệu người dùng"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Đăng ký thất bại"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    /**
     * Đăng nhập với email và mật khẩu
     */
    override suspend fun login(email: String, password: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            val loginRequest = LoginRequestDto(email, password)
            val response = apiService.login(loginRequest)

            if (response.isSuccessful && response.body()?.success == true) {
                val authResponse = response.body()?.data
                if (authResponse != null) {
                    val saveResult = preferencesManager.saveAuthToken(authResponse.token)
                    if (!saveResult) {
                        emit(Resource.Error("Không thể lưu token xác thực"))
                        return@flow
                    }

                    emit(Resource.Success(true))
                } else {
                    emit(Resource.Error("Không nhận được dữ liệu người dùng"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Đăng nhập thất bại"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    /**
     * Đăng nhập với Google
     */
    override suspend fun loginWithGoogle(googleAuthRequest: GoogleAuthRequestDto): Flow<Resource<Boolean>> =
        flow {
            emit(Resource.Loading())

            try {
                val response = apiService.loginWithGoogle(googleAuthRequest)

                if (response.isSuccessful && response.body()?.success == true) {
                    val authResponse = response.body()?.data
                    if (authResponse != null) {
                        val saveResult = preferencesManager.saveAuthToken(authResponse.token)
                        if (!saveResult) {
                            emit(Resource.Error("Không thể lưu token xác thực"))
                            return@flow
                        }

                        emit(Resource.Success(true))
                    } else {
                        emit(Resource.Error("Không nhận được dữ liệu người dùng"))
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Đăng nhập Google thất bại"
                    emit(Resource.Error(errorMessage))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định"))
            }
        }

    /**
     * Gửi yêu cầu quên mật khẩu
     */
    override suspend fun forgotPassword(email: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.forgotPassword(email)

            if (response.isSuccessful && response.body()?.success == true) {
                val message = response.body()?.message
                    ?: "Liên kết đặt lại mật khẩu đã được gửi đến email của bạn"
                emit(Resource.Success(message))
            } else {
                val errorMessage =
                    response.body()?.message ?: "Không thể gửi yêu cầu đặt lại mật khẩu"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    /**
     * Đặt lại mật khẩu
     */
    override suspend fun resetPassword(
        token: String,
        password: String,
        confirmPassword: String
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        try {
            val request = ResetPasswordRequestDto(token, password, confirmPassword)
            val response = apiService.resetPassword(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val message = response.body()?.message ?: "Đặt lại mật khẩu thành công"
                emit(Resource.Success(message))
            } else {
                val errorMessage = response.body()?.message ?: "Không thể đặt lại mật khẩu"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định"))
        }
    }

    /**
     * Đăng xuất
     */
    override suspend fun logout(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            googleAuthManager.signOut()
            try {
                val token = preferencesManager.getAuthToken().firstOrNull()
                if (!token.isNullOrBlank()) {
                    val request = LogoutRequestDto(token)
                    apiService.logout(request)
                }
            } catch (e: Exception) {
            }

            val tokenCleared = preferencesManager.clearAuthToken()
            preferencesManager.saveUserId("")

            if (tokenCleared) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Success(true))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi không xác định khi đăng xuất"))
        }
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    override suspend fun getCurrentUser(): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())

        try {
            val token = preferencesManager.getAuthToken().firstOrNull()

            if (token.isNullOrBlank()) {
                emit(Resource.Error("Bạn chưa đăng nhập"))
                return@flow
            }

            val response = apiService.getCurrentUser()

            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                if (userDto != null) {
                    emit(Resource.Success(userDto))
                } else {
                    emit(Resource.Error("Không nhận được dữ liệu người dùng"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Lấy thông tin người dùng thất bại"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
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