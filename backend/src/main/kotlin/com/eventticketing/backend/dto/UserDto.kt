package com.eventticketing.backend.dto

import com.eventticketing.backend.entity.UserRole
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.*

/**
 * DTO đại diện cho thông tin người dùng trả về
 */
data class UserDto(
    val id: UUID? = null,
    val email: String,
    val fullName: String,
    val phoneNumber: String?,
    val role: UserRole,
    val enabled: Boolean,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime? = null
)

/**
 * DTO cho việc tạo người dùng mới
 */
data class UserCreateDto(
    @field:NotBlank(message = "Email không được để trống")
    @field:Email(message = "Email không hợp lệ")
    val email: String,
    
    @field:NotBlank(message = "Mật khẩu không được để trống")
    @field:Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    val password: String,
    
    @field:NotBlank(message = "Họ tên không được để trống")
    val fullName: String,
    
    @field:Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Số điện thoại không hợp lệ")
    val phoneNumber: String? = null,
    
    val role: UserRole = UserRole.USER
)

/**
 * DTO cho việc cập nhật thông tin người dùng
 */
data class UserUpdateDto(
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val enabled: Boolean? = null
)

/**
 * DTO cho request đăng nhập
 */
data class LoginRequestDto(
    @field:NotBlank(message = "Email không được để trống")
    @field:Email(message = "Email không hợp lệ")
    val email: String,
    
    @field:NotBlank(message = "Mật khẩu không được để trống")
    val password: String
)

/**
 * DTO cho response sau khi đăng nhập thành công
 */
data class UserAuthResponseDto(
    val id: UUID,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val token: String
)

/**
 * DTO cho việc đổi mật khẩu
 */
data class PasswordChangeDto(
    @field:NotBlank(message = "Mật khẩu hiện tại không được để trống")
    val currentPassword: String,
    
    @field:NotBlank(message = "Mật khẩu mới không được để trống")
    @field:Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    val newPassword: String,
    
    @field:NotBlank(message = "Xác nhận mật khẩu không được để trống")
    val confirmPassword: String
) 