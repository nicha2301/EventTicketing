package com.eventticketing.backend.service

import com.eventticketing.backend.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface UserService {
    
    /**
     * Đăng ký người dùng mới
     */
    fun registerUser(userCreateDto: UserCreateDto): UserDto
    
    /**
     * Xác thực người dùng và trả về JWT token
     */
    fun authenticateUser(loginRequest: LoginRequestDto): UserAuthResponseDto
    
    /**
     * Xác thực người dùng bằng Google và trả về JWT token
     */
    fun authenticateWithGoogle(googleAuthRequest: GoogleAuthRequestDto): UserAuthResponseDto
    
    /**
     * Refresh JWT token
     */
    fun refreshToken(refreshToken: String): UserAuthResponseDto
    
    /**
     * Đăng xuất người dùng
     */
    fun logout(token: String): Boolean
    
    /**
     * Lấy thông tin người dùng theo ID
     */
    fun getUserById(id: UUID): UserDto
    
    /**
     * Lấy thông tin người dùng theo email
     */
    fun getUserByEmail(email: String): UserDto
    
    /**
     * Lấy thông tin người dùng hiện tại
     */
    fun getCurrentUser(): UserDto
    
    /**
     * Cập nhật thông tin người dùng
     */
    fun updateUser(id: UUID, userUpdateDto: UserUpdateDto): UserDto
    
    /**
     * Đổi mật khẩu người dùng
     */
    fun changePassword(id: UUID, passwordChangeDto: PasswordChangeDto): Boolean
    
    /**
     * Yêu cầu đặt lại mật khẩu
     */
    fun requestPasswordReset(email: String): Boolean
    
    /**
     * Thực hiện đặt lại mật khẩu
     */
    fun resetPassword(token: String, newPassword: String): Boolean
    
    /**
     * Lấy danh sách người dùng (chỉ admin)
     */
    fun getAllUsers(pageable: Pageable): Page<UserDto>
    
    /**
     * Kích hoạt tài khoản người dùng
     */
    fun activateUser(activationToken: String): Boolean
    
    /**
     * Vô hiệu hóa tài khoản người dùng
     */
    fun deactivateUser(id: UUID): Boolean
    
    /**
     * Phân quyền cho người dùng
     */
    fun updateUserRole(id: UUID, role: String): UserDto
    
    /**
     * Xóa người dùng
     */
    fun deleteUser(id: UUID): Boolean
} 