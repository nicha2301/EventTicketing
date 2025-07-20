package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.data.remote.dto.auth.UserUpdateDto
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

/**
 * Repository xử lý các chức năng liên quan đến người dùng
 */
interface UserRepository {
    /**
     * Lấy thông tin người dùng hiện tại
     * @return Flow<Resource<UserDto>> - Flow chứa thông tin người dùng
     */
    fun getCurrentUser(): Flow<Resource<UserDto>>

    /**
     * Cập nhật thông tin người dùng
     * @param userUpdateDto - Thông tin cần cập nhật
     * @return Flow<Resource<UserDto>> - Flow chứa thông tin người dùng đã cập nhật
     */
    fun updateUserProfile(userUpdateDto: UserUpdateDto): Flow<Resource<UserDto>>

    /**
     * Cập nhật ảnh đại diện người dùng
     * @param image - File ảnh dạng MultipartBody.Part
     * @return Flow<Resource<String>> - Flow chứa URL ảnh đại diện mới
     */
    fun updateUserAvatar(image: MultipartBody.Part): Flow<Resource<String>>

    /**
     * Thay đổi mật khẩu người dùng
     * @param oldPassword - Mật khẩu cũ
     * @param newPassword - Mật khẩu mới
     * @return Flow<Resource<String>> - Flow chứa thông báo kết quả
     */
    fun changePassword(oldPassword: String, newPassword: String): Flow<Resource<String>>
} 