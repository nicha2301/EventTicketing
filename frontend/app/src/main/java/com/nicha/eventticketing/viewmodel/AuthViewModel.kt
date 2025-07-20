package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.auth.GoogleAuthRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.UserCreateDto
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    private val authRepository: AuthRepository
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
                
                authRepository.isLoggedIn()
                    .collect { isLoggedIn ->
                        if (isLoggedIn) {
                            Timber.d("Người dùng đã đăng nhập, đang tải thông tin người dùng")
                            _authState.value = AuthState.Loading
                            fetchCurrentUser()
                        } else {
                            Timber.d("Người dùng chưa đăng nhập")
                            _authState.value = AuthState.Unauthenticated
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi kiểm tra trạng thái đăng nhập")
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
            authRepository.login(email, password)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Timber.d("Đăng nhập thành công, đang tải thông tin người dùng")
                            fetchCurrentUser()
                        }
                        is Resource.Error -> {
                            Timber.e("Đăng nhập thất bại: ${result.message}")
                            _authState.value = AuthState.Error(result.message ?: "Đăng nhập thất bại")
                        }
                        is Resource.Loading -> {
                            _authState.value = AuthState.Loading
                        }
                    }
                }
        }
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
            val googleAuthRequest = GoogleAuthRequestDto(
                idToken = idToken,
                email = email,
                name = name,
                profilePictureUrl = profilePictureUrl
            )
            
            authRepository.loginWithGoogle(googleAuthRequest)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Timber.d("Đăng nhập Google thành công, đang tải thông tin người dùng")
                            fetchCurrentUser()
                        }
                        is Resource.Error -> {
                            Timber.e("Đăng nhập Google thất bại: ${result.message}")
                            _authState.value = AuthState.Error(result.message ?: "Đăng nhập Google thất bại")
                        }
                        is Resource.Loading -> {
                            _authState.value = AuthState.Loading
                        }
                    }
                }
        }
    }
    
    /**
     * Đăng ký người dùng mới
     */
    fun register(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String?
    ) {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            val userCreateDto = UserCreateDto(
                email = email,
                password = password,
                fullName = fullName,
                phoneNumber = phoneNumber
            )
            
            authRepository.register(userCreateDto)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Timber.d("Đăng ký thành công: ${result.data?.email}")
                            _authState.value = AuthState.RegistrationSuccess(
                                "Đăng ký thành công. Vui lòng đăng nhập để tiếp tục."
                            )
                        }
                        is Resource.Error -> {
                            Timber.e("Đăng ký thất bại: ${result.message}")
                            _authState.value = AuthState.Error(result.message ?: "Đăng ký thất bại")
                        }
                        is Resource.Loading -> {
                            _authState.value = AuthState.Loading
                        }
                    }
                }
        }
    }
    
    /**
     * Đăng xuất người dùng
     */
    fun logout() {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            authRepository.logout()
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Timber.d("Đăng xuất thành công")
                            _currentUser.value = null
                            _authState.value = AuthState.Unauthenticated
                        }
                        is Resource.Error -> {
                            Timber.e("Đăng xuất thất bại: ${result.message}")
                            // Vẫn đăng xuất ở local ngay cả khi có lỗi
                            _currentUser.value = null
                            _authState.value = AuthState.Unauthenticated
                        }
                        is Resource.Loading -> {
                            _authState.value = AuthState.Loading
                        }
                    }
                }
        }
    }
    
    /**
     * Lấy thông tin người dùng hiện tại
     */
    fun fetchCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val user = result.data
                            if (user != null) {
                                _currentUser.value = user
                                _authState.value = AuthState.Authenticated
                                Timber.d("Lấy thông tin người dùng thành công: ${user.fullName}")
                            } else {
                                Timber.e("Không thể lấy thông tin người dùng từ response")
                                _authState.value = AuthState.Error("Không thể lấy thông tin người dùng")
                                logout()
                            }
                        }
                        is Resource.Error -> {
                            Timber.e("Lấy thông tin người dùng thất bại: ${result.message}")
                            _authState.value = AuthState.Error(result.message ?: "Không thể lấy thông tin người dùng")
                            logout()
                        }
                        is Resource.Loading -> {
                            _authState.value = AuthState.Loading
                        }
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
            authRepository.forgotPassword(email)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Timber.d("Gửi yêu cầu quên mật khẩu thành công")
                            _authState.value = AuthState.ForgotPasswordSuccess(
                                result.data ?: "Mật khẩu tạm thời đã được gửi đến email của bạn"
                            )
                        }
                        is Resource.Error -> {
                            Timber.e("Gửi yêu cầu quên mật khẩu thất bại: ${result.message}")
                            _authState.value = AuthState.Error(result.message ?: "Không thể gửi yêu cầu đặt lại mật khẩu")
                        }
                        is Resource.Loading -> {
                            _authState.value = AuthState.Loading
                        }
                    }
                }
        }
    }
    
    /**
     * Đặt lại mật khẩu
     */
    fun resetPassword(token: String, password: String, confirmPassword: String) {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            authRepository.resetPassword(token, password, confirmPassword)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Timber.d("Đặt lại mật khẩu thành công")
                            _authState.value = AuthState.ResetPasswordSuccess(
                                result.data ?: "Đặt lại mật khẩu thành công"
                            )
                        }
                        is Resource.Error -> {
                            Timber.e("Đặt lại mật khẩu thất bại: ${result.message}")
                            _authState.value = AuthState.Error(result.message ?: "Không thể đặt lại mật khẩu")
                        }
                        is Resource.Loading -> {
                            _authState.value = AuthState.Loading
                        }
                    }
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
            checkAuthentication()
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