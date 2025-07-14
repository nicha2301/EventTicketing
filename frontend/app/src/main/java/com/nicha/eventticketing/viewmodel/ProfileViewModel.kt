package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.auth.LogoutRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.data.remote.dto.auth.UserUpdateDto
import com.nicha.eventticketing.data.remote.service.ApiService
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

/**
 * ViewModel để quản lý thông tin profile người dùng
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // Trạng thái profile
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()
    
    // Thông tin người dùng hiện tại
    private val _userProfile = MutableStateFlow<UserDto?>(null)
    val userProfile: StateFlow<UserDto?> = _userProfile.asStateFlow()
    
    // Khởi tạo với việc lấy thông tin người dùng
    init {
        fetchUserProfile()
    }
    
    /**
     * Lấy thông tin profile người dùng
     */
    fun fetchUserProfile() {
        _profileState.value = ProfileState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang lấy thông tin profile người dùng")
                val response = apiService.getCurrentUser()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data
                    if (user != null) {
                        _userProfile.value = user
                        _profileState.value = ProfileState.Success
                        Timber.d("Lấy thông tin profile thành công: ${user.fullName}")
                    } else {
                        Timber.e("Không thể lấy thông tin người dùng từ response")
                        _profileState.value = ProfileState.Error("Không thể lấy thông tin người dùng")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể lấy thông tin profile"
                    Timber.e("Lấy thông tin profile thất bại: $errorMessage")
                    _profileState.value = ProfileState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "lấy thông tin profile")
            }
        }
    }
    
    /**
     * Cập nhật thông tin profile
     */
    fun updateProfile(
        fullName: String,
        phoneNumber: String?,
        enabled: Boolean = true
    ) {
        _profileState.value = ProfileState.Loading
        
        viewModelScope.launch {
            try {
                Timber.d("Đang cập nhật thông tin profile")
                val updateDto = UserUpdateDto(
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    profilePictureUrl = null,
                    enabled = enabled
                )
                
                val response = apiService.updateUser(updateDto)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedUser = response.body()?.data
                    if (updatedUser != null) {
                        _userProfile.value = updatedUser
                        _profileState.value = ProfileState.Success
                        Timber.d("Cập nhật profile thành công: ${updatedUser.fullName}")
                    } else {
                        Timber.e("Không thể lấy thông tin người dùng đã cập nhật từ response")
                        _profileState.value = ProfileState.Error("Không thể cập nhật thông tin người dùng")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Không thể cập nhật thông tin profile"
                    Timber.e("Cập nhật profile thất bại: $errorMessage")
                    _profileState.value = ProfileState.Error(errorMessage)
                }
            } catch (e: Exception) {
                handleNetworkError(e, "cập nhật profile")
            }
        }
    }
    
    /**
     * Đăng xuất người dùng
     */
    fun logout() {
        viewModelScope.launch {
            try {
                Timber.d("Đang đăng xuất người dùng")
                // Lấy token hiện tại
                val token = preferencesManager.getAuthTokenSync()
                if (token != null) {
                    // Gửi request đăng xuất với token
                    val logoutRequest = LogoutRequestDto(token = token)
                    val response = apiService.logout(logoutRequest)
                    
                    if (response.isSuccessful) {
                        Timber.d("Đăng xuất thành công từ server")
                    } else {
                        Timber.w("Đăng xuất từ server không thành công: ${response.errorBody()?.string()}")
                    }
                } else {
                    Timber.w("Không tìm thấy token để đăng xuất")
                }
                
                // Luôn xóa token cục bộ bất kể kết quả từ server
                val result = preferencesManager.clearAuthToken()
                if (result) {
                    Timber.d("Đã xóa token thành công")
                } else {
                    Timber.e("Không thể xóa token")
                }
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi đăng xuất từ server")
                // Vẫn đăng xuất cục bộ
                preferencesManager.clearAuthToken()
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
                _profileState.value = ProfileState.Error("Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng của bạn.")
            }
            is SocketTimeoutException -> {
                Timber.e(exception, "Lỗi kết nối: Kết nối bị timeout")
                _profileState.value = ProfileState.Error("Kết nối bị timeout. Vui lòng thử lại sau.")
            }
            is IOException -> {
                Timber.e(exception, "Lỗi kết nối: IOException")
                _profileState.value = ProfileState.Error("Lỗi kết nối: ${exception.message ?: "Không xác định"}")
            }
            is HttpException -> {
                Timber.e(exception, "Lỗi HTTP: ${exception.code()}")
                _profileState.value = ProfileState.Error("Lỗi máy chủ: ${exception.message()}")
            }
            else -> {
                Timber.e(exception, "Lỗi không xác định khi $action")
                _profileState.value = ProfileState.Error("Lỗi không xác định: ${exception.message ?: "Unknown"}")
            }
        }
    }
    
    /**
     * Reset trạng thái lỗi
     */
    fun resetError() {
        if (_profileState.value is ProfileState.Error) {
            _profileState.value = ProfileState.Initial
        }
    }
}

/**
 * Trạng thái của profile
 */
sealed class ProfileState {
    object Initial : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
} 