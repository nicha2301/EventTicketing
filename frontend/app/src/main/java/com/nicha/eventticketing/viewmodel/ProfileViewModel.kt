package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.data.remote.dto.auth.UserUpdateDto
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.AuthRepository
import com.nicha.eventticketing.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel để quản lý thông tin profile người dùng
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
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
            userRepository.getCurrentUser().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val user = result.data
                        if (user != null) {
                            _userProfile.value = user
                            _profileState.value = ProfileState.Success
                            Timber.d("Lấy thông tin profile thành công: ${user.fullName}")
                        } else {
                            Timber.e("Không thể lấy thông tin người dùng từ repository")
                            _profileState.value = ProfileState.Error("Không thể lấy thông tin người dùng")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy thông tin profile thất bại: ${result.message}")
                        _profileState.value = ProfileState.Error(result.message ?: "Không thể lấy thông tin profile")
                    }
                    is Resource.Loading -> {
                        _profileState.value = ProfileState.Loading
                    }
                }
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
            val updateDto = UserUpdateDto(
                fullName = fullName,
                phoneNumber = phoneNumber,
                profilePictureUrl = null,
                enabled = enabled
            )
            
            userRepository.updateUserProfile(updateDto).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val updatedUser = result.data
                        if (updatedUser != null) {
                            _userProfile.value = updatedUser
                            _profileState.value = ProfileState.Success
                            Timber.d("Cập nhật profile thành công: ${updatedUser.fullName}")
                        } else {
                            Timber.e("Không thể lấy thông tin người dùng đã cập nhật từ repository")
                            _profileState.value = ProfileState.Error("Không thể cập nhật thông tin người dùng")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Cập nhật profile thất bại: ${result.message}")
                        _profileState.value = ProfileState.Error(result.message ?: "Không thể cập nhật thông tin profile")
                    }
                    is Resource.Loading -> {
                        _profileState.value = ProfileState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Đăng xuất người dùng
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Timber.d("Đăng xuất thành công")
                        _userProfile.value = null
                        _profileState.value = ProfileState.Initial
                    }
                    is Resource.Error -> {
                        Timber.e("Đăng xuất thất bại: ${result.message}")
                        // Vẫn đăng xuất ở local ngay cả khi có lỗi
                        _userProfile.value = null
                        _profileState.value = ProfileState.Initial
                    }
                    is Resource.Loading -> {
                        _profileState.value = ProfileState.Loading
                    }
                }
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