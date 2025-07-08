package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "API quản lý người dùng")
@SecurityRequirement(name = "bearer-jwt")
class UserController(private val userService: UserService) {

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng hiện tại")
    fun getCurrentUser(): ResponseEntity<ApiResponse<UserDto>> {
        val currentUser = userService.getCurrentUser()
        
        return ResponseEntity.ok(ApiResponse(
            success = true,
            data = currentUser
        ))
    }

    @PutMapping("/me")
    @Operation(summary = "Cập nhật thông tin người dùng hiện tại")
    fun updateCurrentUser(@Valid @RequestBody userUpdateDto: UserUpdateDto): ResponseEntity<ApiResponse<UserDto>> {
        val currentUser = userService.getCurrentUser()
        val updatedUser = userService.updateUser(currentUser.id!!, userUpdateDto)
        
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Thông tin người dùng đã được cập nhật",
            data = updatedUser
        ))
    }

    @PostMapping("/me/password")
    @Operation(summary = "Đổi mật khẩu người dùng hiện tại")
    fun changePassword(@Valid @RequestBody passwordChangeDto: PasswordChangeDto): ResponseEntity<ApiResponse<String>> {
        val currentUser = userService.getCurrentUser()
        val result = userService.changePassword(currentUser.id!!, passwordChangeDto)
        
        return if (result) {
            ResponseEntity.ok(ApiResponse(
                success = true,
                message = "Mật khẩu đã được thay đổi thành công",
                data = "Thay đổi thành công"
            ))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(
                success = false,
                message = "Không thể thay đổi mật khẩu",
                data = null
            ))
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy thông tin người dùng theo ID (Admin only)")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserDto>> {
        val user = userService.getUserById(id)
        
        return ResponseEntity.ok(ApiResponse(
            success = true,
            data = user
        ))
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách người dùng (Admin only)")
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String
    ): ResponseEntity<ApiResponse<PagedResponse<UserDto>>> {
        val direction = if (sortDir.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(direction, sortBy))
        
        val usersPage = userService.getAllUsers(pageable)
        val pagedResponse = PagedResponse.from(usersPage)
        
        return ResponseEntity.ok(ApiResponse(
            success = true,
            data = pagedResponse
        ))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thông tin người dùng (Admin only)")
    fun updateUser(
        @PathVariable id: UUID,
        @Valid @RequestBody userUpdateDto: UserUpdateDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        val updatedUser = userService.updateUser(id, userUpdateDto)
        
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Thông tin người dùng đã được cập nhật",
            data = updatedUser
        ))
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Vô hiệu hóa tài khoản người dùng (Admin only)")
    fun deactivateUser(@PathVariable id: UUID): ResponseEntity<ApiResponse<String>> {
        val result = userService.deactivateUser(id)
        
        return if (result) {
            ResponseEntity.ok(ApiResponse(
                success = true,
                message = "Tài khoản người dùng đã bị vô hiệu hóa",
                data = "Vô hiệu hóa thành công"
            ))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(
                success = false,
                message = "Không thể vô hiệu hóa tài khoản người dùng",
                data = null
            ))
        }
    }

    @PostMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật vai trò người dùng (Admin only)")
    fun updateUserRole(
        @PathVariable id: UUID,
        @RequestParam role: String
    ): ResponseEntity<ApiResponse<UserDto>> {
        val updatedUser = userService.updateUserRole(id, role)
        
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Vai trò người dùng đã được cập nhật",
            data = updatedUser
        ))
    }
} 