package com.eventticketing.backend.controller

import com.eventticketing.backend.annotation.RateLimited
import com.eventticketing.backend.dto.*
import com.eventticketing.backend.service.UserService
import com.eventticketing.backend.util.Constants.ApiPaths
import com.eventticketing.backend.util.ResponseBuilder
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ApiPaths.AUTH_BASE)
@Tag(name = "Authentication", description = "API xác thực và đăng ký người dùng")
class AuthController(private val userService: UserService) {

    @PostMapping("/register")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Đăng ký người dùng mới")
    @RateLimited(maxRequests = 3, windowSeconds = 60)
    fun registerUser(@Valid @RequestBody userCreateDto: UserCreateDto): ResponseEntity<ApiResponse<UserDto>> {
        val createdUser = userService.registerUser(userCreateDto)
        return ResponseBuilder.success(
            createdUser, 
            "Đăng ký thành công. Vui lòng kiểm tra email để kích hoạt tài khoản."
        )
    }

    @PostMapping("/login")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Đăng nhập")
    @RateLimited(maxRequests = 5, windowSeconds = 60)
    fun authenticateUser(@Valid @RequestBody loginRequest: LoginRequestDto): ResponseEntity<ApiResponse<UserAuthResponseDto>> {
        val userAuthResponse = userService.authenticateUser(loginRequest)
        return ResponseBuilder.success(userAuthResponse, "Đăng nhập thành công")
    }
    
    @PostMapping("/google")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Đăng nhập bằng Google")
    @RateLimited(maxRequests = 5, windowSeconds = 60)
    fun authenticateWithGoogle(@Valid @RequestBody googleAuthRequest: GoogleAuthRequestDto): ResponseEntity<ApiResponse<UserAuthResponseDto>> {
        val userAuthResponse = userService.authenticateWithGoogle(googleAuthRequest)
        return ResponseBuilder.success(userAuthResponse, "Đăng nhập bằng Google thành công")
    }

    @PostMapping("/refresh-token")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Refresh JWT token")
    @RateLimited(maxRequests = 10, windowSeconds = 60)
    fun refreshToken(@Valid @RequestBody refreshTokenRequest: RefreshTokenRequest): ResponseEntity<ApiResponse<UserAuthResponseDto>> {
        val userAuthResponse = userService.refreshToken(refreshTokenRequest.refreshToken)
        return ResponseBuilder.success(userAuthResponse, "Token đã được refresh thành công")
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất người dùng")
    fun logout(@Valid @RequestBody logoutRequest: LogoutRequest): ResponseEntity<ApiResponse<String>> {
        val result = userService.logout(logoutRequest.token)
        return if (result) {
            ResponseBuilder.success("Đã đăng xuất", "Đăng xuất thành công")
        } else {
            ResponseBuilder.error("Không thể đăng xuất")
        }
    }

    @GetMapping("/activate")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Kích hoạt tài khoản người dùng")
    fun activateUser(@RequestParam token: String): ResponseEntity<ApiResponse<String>> {
        val result = userService.activateUser(token)
        return if (result) {
            ResponseBuilder.success("Kích hoạt thành công", "Tài khoản đã được kích hoạt thành công")
        } else {
            ResponseBuilder.error("Không thể kích hoạt tài khoản. Token không hợp lệ hoặc đã hết hạn.")
        }
    }

    @PostMapping("/password/forgot")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Yêu cầu đặt lại mật khẩu")
    @RateLimited(maxRequests = 3, windowSeconds = 60)
    fun requestPasswordReset(@RequestParam email: String): ResponseEntity<ApiResponse<String>> {
        val result = userService.requestPasswordReset(email)
        return if (result) {
            ResponseBuilder.success(
                "Yêu cầu thành công", 
                "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư của bạn."
            )
        } else {
            ResponseBuilder.error("Không thể gửi yêu cầu đặt lại mật khẩu.")
        }
    }

    @PostMapping("/password/reset")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Đặt lại mật khẩu với token")
    fun resetPassword(@Valid @RequestBody passwordResetDto: PasswordResetDto): ResponseEntity<ApiResponse<String>> {
        val result = userService.resetPassword(passwordResetDto.token, passwordResetDto.newPassword)
        return if (result) {
            ResponseBuilder.success("Đặt lại mật khẩu thành công", "Mật khẩu đã được đặt lại thành công")
        } else {
            ResponseBuilder.error("Không thể đặt lại mật khẩu. Token không hợp lệ hoặc đã hết hạn.")
        }
    }
    
    @GetMapping("/health")
    @SecurityRequirements // Không yêu cầu xác thực
    @Operation(summary = "Kiểm tra trạng thái API")
    fun healthCheck(): ResponseEntity<ApiResponse<String>> {
        return ResponseBuilder.success("OK", "API hoạt động bình thường")
    }
} 