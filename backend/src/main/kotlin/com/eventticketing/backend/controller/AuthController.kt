package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API xác thực và đăng ký người dùng")
class AuthController(private val userService: UserService) {

    @PostMapping("/register")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Đăng ký người dùng mới")
    fun registerUser(@Valid @RequestBody userCreateDto: UserCreateDto): ResponseEntity<ApiResponse<UserDto>> {
        val createdUser = userService.registerUser(userCreateDto)
        
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Đăng ký thành công. Vui lòng kiểm tra email để kích hoạt tài khoản.",
            data = createdUser
        ))
    }

    @PostMapping("/login")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Đăng nhập và lấy token")
    fun authenticateUser(@Valid @RequestBody loginRequest: LoginRequestDto): ResponseEntity<ApiResponse<UserAuthResponseDto>> {
        val userAuthResponse = userService.authenticateUser(loginRequest)
        
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Đăng nhập thành công",
            data = userAuthResponse
        ))
    }
    
    @PostMapping("/google")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Đăng nhập bằng Google và lấy token")
    fun authenticateWithGoogle(@Valid @RequestBody googleAuthRequest: GoogleAuthRequestDto): ResponseEntity<ApiResponse<UserAuthResponseDto>> {
        val userAuthResponse = userService.authenticateWithGoogle(googleAuthRequest)
        
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Đăng nhập bằng Google thành công",
            data = userAuthResponse
        ))
    }

    @GetMapping("/activate")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Kích hoạt tài khoản người dùng")
    fun activateUser(@RequestParam token: String): ResponseEntity<ApiResponse<String>> {
        val result = userService.activateUser(token)
        
        return if (result) {
            ResponseEntity.ok(ApiResponse(
                success = true,
                message = "Tài khoản đã được kích hoạt thành công",
                data = "Kích hoạt thành công"
            ))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(
                success = false,
                message = "Không thể kích hoạt tài khoản. Token không hợp lệ hoặc đã hết hạn.",
                data = null
            ))
        }
    }

    @PostMapping("/password/forgot")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Yêu cầu đặt lại mật khẩu")
    fun requestPasswordReset(@RequestParam email: String): ResponseEntity<ApiResponse<String>> {
        val result = userService.requestPasswordReset(email)
        
        return if (result) {
            ResponseEntity.ok(ApiResponse(
                success = true,
                message = "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư của bạn.",
                data = "Yêu cầu thành công"
            ))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(
                success = false,
                message = "Không thể gửi yêu cầu đặt lại mật khẩu.",
                data = null
            ))
        }
    }

    @PostMapping("/password/reset")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Đặt lại mật khẩu với token")
    fun resetPassword(
        @RequestParam token: String,
        @RequestParam newPassword: String
    ): ResponseEntity<ApiResponse<String>> {
        val result = userService.resetPassword(token, newPassword)
        
        return if (result) {
            ResponseEntity.ok(ApiResponse(
                success = true,
                message = "Mật khẩu đã được đặt lại thành công",
                data = "Đặt lại mật khẩu thành công"
            ))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(
                success = false,
                message = "Không thể đặt lại mật khẩu. Token không hợp lệ hoặc đã hết hạn.",
                data = null
            ))
        }
    }
} 