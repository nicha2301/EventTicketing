package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.auth.ForgotPasswordRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.GoogleAuthRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.ResetPasswordRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.UserCreateDto
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow

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
     * Đăng nhập với Google
     * @param googleAuthRequest Request chứa thông tin xác thực Google
     * @return Flow của kết quả đăng nhập
     */
    suspend fun loginWithGoogle(googleAuthRequest: GoogleAuthRequestDto): Flow<Resource<Boolean>>

    /**
     * Gửi yêu cầu quên mật khẩu
     * @param email Email cần lấy lại mật khẩu
     * @return Flow của kết quả gửi yêu cầu
     */
    suspend fun forgotPassword(email: String): Flow<Resource<String>>

    /**
     * Đặt lại mật khẩu
     * @param token Token xác thực từ email
     * @param password Mật khẩu mới
     * @param confirmPassword Xác nhận mật khẩu mới
     * @return Flow của kết quả đặt lại mật khẩu
     */
    suspend fun resetPassword(token: String, password: String, confirmPassword: String): Flow<Resource<String>>

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