package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.dto.auth.ChangePasswordDto
import com.nicha.eventticketing.data.remote.dto.auth.ForgotPasswordRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.GoogleAuthRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.LoginRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.ResetPasswordRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.UserCreateDto
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.data.remote.service.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import retrofit2.HttpException

/**
 * ViewModel để quản lý trạng thái xác thực và người dùng trong ứng dụng
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // Trạng thái xác thực
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Thông tin người dùng hiện tại
    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()
    
    // Kiểm tra trạng thái xác thực khi khởi động
    init {
        checkAuthentication()
    }
    
    /**
     * Kiểm tra trạng thái xác thực từ token lưu trữ
     */
    private fun checkAuthentication() {
        viewModelScope.launch {
            _authState.value = AuthState.Initial
            
            try {
                Timber.d("Đang kiểm tra trạng thái xác thực")
                
                val token = preferencesManager.getAuthToken().firstOrNull()
                
            if (!token.isNullOrEmpty()) {
                    Timber.d("Tìm thấy token, đang tải thông tin người dùng")
                    _authState.value = AuthState.Loading
                fetchCurrentUser()
            } else {
                    Timber.d("Không tìm thấy token, chuyển sang trạng thái chưa xác thực")
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi kiểm tra token")
                _authState.value = AuthState.Error("Lỗi khi kiểm tra trạng thái xác thực: ${e.message}")
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    /**
     * Đăng nhập với email và mật khẩu
     */
    fun login(email: String, password: String, rememberMe: Boolean) {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang đăng nhập với email: $email")
                val response = apiService.login(LoginRequestDto(email = email, password = password))
                
                if (response.isSuccessful && response.body()?.success == true) {
                    handleSuccessfulLogin(response.body()?.data, rememberMe)
                } else {
                    val errorMessage = response.body()?.message ?: "Đăng nhập thất bại"
                    Timber.e("Đăng nhập thất bại: $errorMessage")
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "đăng nhập")
            }
        }
    }
    
    /**
     * Xử lý đăng nhập thành công
     */
    private suspend fun handleSuccessfulLogin(authResponse: Any?, rememberMe: Boolean) {
        if (authResponse != null) {
            val userAuth = authResponse as com.nicha.eventticketing.data.remote.dto.auth.UserAuthResponseDto
            
            // Lưu token
            if (rememberMe) {
                Timber.d("Lưu token dài hạn (remember me)")
            } else {
                Timber.d("Lưu token cho phiên hiện tại")
            }
            
            val saveResult = preferencesManager.saveAuthToken(userAuth.token)
            if (!saveResult) {
                Timber.e("Không thể lưu token")
                _authState.value = AuthState.Error("Đăng nhập thành công nhưng không thể lưu token")
                return
            }
            
            // Lưu userId
            val saveUserIdResult = preferencesManager.saveUserId(userAuth.id)
            if (!saveUserIdResult) {
                Timber.e("Không thể lưu userId: ${userAuth.id}")
                // Không cần dừng luồng vì đây không phải là lỗi nghiêm trọng
            } else {
                Timber.d("Đã lưu userId: ${userAuth.id}")
            }
            
            // Cập nhật thông tin người dùng
            val userDto = mapToUserDto(userAuth)
            _currentUser.value = userDto
            _authState.value = AuthState.Authenticated
            Timber.d("Đăng nhập thành công: ${userDto.fullName}")
        } else {
            Timber.e("Không thể lấy thông tin người dùng từ response")
            _authState.value = AuthState.Error("Không thể lấy thông tin người dùng")
            }
        }
    
    /**
     * Chuyển đổi dữ liệu từ UserAuthResponseDto sang UserDto
     */
    private fun mapToUserDto(authResponse: com.nicha.eventticketing.data.remote.dto.auth.UserAuthResponseDto): UserDto {
        return UserDto(
            id = authResponse.id,
            email = authResponse.email,
            fullName = authResponse.fullName,
            phoneNumber = null, // Không có trong response
            role = authResponse.role,
            enabled = true, // Giả định đã active vì đã đăng nhập thành công
            profilePictureUrl = authResponse.profilePictureUrl,
            createdAt = null // Không có trong response
        )
    }
    
    /**
     * Đăng nhập bằng Google
     */
    fun loginWithGoogle(
        idToken: String,
        email: String,
        name: String,
        profilePictureUrl: String?,
        rememberMe: Boolean
    ) {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang đăng nhập bằng Google với email: $email")
                val googleAuthRequest = GoogleAuthRequestDto(
                    idToken = idToken,
                    email = email,
                    name = name,
                    profilePictureUrl = profilePictureUrl
                )
                
                val response = apiService.loginWithGoogle(googleAuthRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    handleSuccessfulLogin(response.body()?.data, rememberMe)
                } else {
                    val errorMessage = response.body()?.message ?: "Đăng nhập Google thất bại"
                    Timber.e("Đăng nhập Google thất bại: $errorMessage")
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "đăng nhập Google")
            }
        }
    }
    
    /**
     * Đăng ký người dùng mới
     */
    fun register(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String?
    ) {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang đăng ký với email: $email")
                val userCreateDto = UserCreateDto(
                    email = email,
                    password = password,
                    fullName = fullName,
                    phoneNumber = phoneNumber
                )
                
                val response = apiService.register(userCreateDto)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Timber.d("Đăng ký thành công")
                    _authState.value = AuthState.RegistrationSuccess(
                        response.body()?.message ?: "Đăng ký thành công"
                    )
                } else {
                    val errorMessage = response.body()?.message ?: "Đăng ký thất bại"
                    Timber.e("Đăng ký thất bại: $errorMessage")
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "đăng ký")
            }
        }
    }
    
    /**
     * Đăng xuất người dùng
     */
    fun logout() {
        viewModelScope.launch {
            Timber.d("Đang đăng xuất người dùng")
            val result = preferencesManager.clearAuthToken()
            if (result) {
                Timber.d("Đã xóa token thành công")
            } else {
                Timber.e("Không thể xóa token")
            }
            
            // Xóa userId
            try {
                val saveUserIdResult = preferencesManager.saveUserId("")
                if (saveUserIdResult) {
                    Timber.d("Đã xóa userId thành công")
                } else {
                    Timber.e("Không thể xóa userId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi xóa userId")
            }
            
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    /**
     * Lấy thông tin người dùng hiện tại
     */
    fun fetchCurrentUser() {
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy thông tin người dùng hiện tại")
                val response = apiService.getCurrentUser()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data
                    if (user != null) {
                        _currentUser.value = user
                        _authState.value = AuthState.Authenticated
                        Timber.d("Lấy thông tin người dùng thành công: ${user.fullName}")
                    } else {
                        Timber.e("Không thể lấy thông tin người dùng từ response")
                        _authState.value = AuthState.Error("Không thể lấy thông tin người dùng")
                        logout()
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy thông tin người dùng"
                    Timber.e("Lấy thông tin người dùng thất bại: $errorMessage")
                    _authState.value = AuthState.Error(errorMessage)
                    logout()
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy thông tin người dùng")
                logout()
            }
        }
    }
    
    /**
     * Xử lý lỗi mạng chung cho tất cả các API call
     */
    private fun handleNetworkError(exception: Exception, action: String) {
        when (exception) {
            is UnknownHostException -> {
                Timber.e(exception, "Lỗi kết nối: Không thể kết nối đến máy chủ")
                _authState.value = AuthState.Error("Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng của bạn.")
            }
            is SocketTimeoutException -> {
                Timber.e(exception, "Lỗi kết nối: Kết nối bị timeout")
                _authState.value = AuthState.Error("Kết nối bị timeout. Vui lòng thử lại sau.")
            }
            is IOException -> {
                Timber.e(exception, "Lỗi kết nối: IOException")
                _authState.value = AuthState.Error("Lỗi kết nối: ${exception.message ?: "Không xác định"}")
            }
            is HttpException -> {
                Timber.e(exception, "Lỗi HTTP: ${exception.code()}")
                _authState.value = AuthState.Error("Lỗi máy chủ: ${exception.message()}")
            }
            else -> {
                Timber.e(exception, "Lỗi không xác định khi $action")
                _authState.value = AuthState.Error("Lỗi không xác định: ${exception.message ?: "Unknown"}")
            }
        }
    }
    
    /**
     * Reset trạng thái lỗi
     */
    fun resetError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Initial
        }
    }
    
    /**
     * Thử kết nối lại với máy chủ
     */
    fun retryConnection() {
        val currentUser = _currentUser.value
        if (currentUser != null) {
            fetchCurrentUser()
        } else {
            viewModelScope.launch {
                try {
                    val token = preferencesManager.getAuthToken().firstOrNull()
                    if (!token.isNullOrEmpty()) {
                        fetchCurrentUser()
                    } else {
                        _authState.value = AuthState.Unauthenticated
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Lỗi khi kiểm tra token trong retryConnection")
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    /**
     * Gửi yêu cầu quên mật khẩu
     */
    fun forgotPassword(email: String) {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang gửi yêu cầu quên mật khẩu cho email: $email")
                val request = ForgotPasswordRequestDto(email)
                val response = apiService.forgotPassword(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Timber.d("Gửi yêu cầu quên mật khẩu thành công")
                    _authState.value = AuthState.ForgotPasswordSuccess(
                        response.body()?.message ?: "Liên kết đặt lại mật khẩu đã được gửi đến email của bạn"
                    )
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể gửi yêu cầu đặt lại mật khẩu"
                    Timber.e("Gửi yêu cầu quên mật khẩu thất bại: $errorMessage")
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "gửi yêu cầu quên mật khẩu")
            }
        }
    }
    
    /**
     * Đặt lại mật khẩu
     */
    fun resetPassword(token: String, password: String, confirmPassword: String) {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang đặt lại mật khẩu")
                val request = ResetPasswordRequestDto(token, password, confirmPassword)
                val response = apiService.resetPassword(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Timber.d("Đặt lại mật khẩu thành công")
                    _authState.value = AuthState.ResetPasswordSuccess(
                        response.body()?.message ?: "Đặt lại mật khẩu thành công"
                    )
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể đặt lại mật khẩu"
                    Timber.e("Đặt lại mật khẩu thất bại: $errorMessage")
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "đặt lại mật khẩu")
            }
        }
    }
}

/**
 * Trạng thái xác thực người dùng
 */
sealed class AuthState {
    /** Trạng thái ban đầu khi khởi động */
    object Initial : AuthState()
    
    /** Đang tải/xử lý */
    object Loading : AuthState()
    
    /** Đã xác thực thành công */
    object Authenticated : AuthState()
    
    /** Chưa xác thực */
    object Unauthenticated : AuthState()
    
    /** Đăng ký thành công */
    data class RegistrationSuccess(val message: String) : AuthState()
    
    /** Gửi yêu cầu quên mật khẩu thành công */
    data class ForgotPasswordSuccess(val message: String) : AuthState()
    
    /** Đặt lại mật khẩu thành công */
    data class ResetPasswordSuccess(val message: String) : AuthState()
    
    /** Gặp lỗi */
    data class Error(val message: String) : AuthState()
} 