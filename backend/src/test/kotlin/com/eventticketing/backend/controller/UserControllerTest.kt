package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.entity.UserRole
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.exception.UnauthorizedException
import com.eventticketing.backend.security.JwtAuthenticationFilter
import com.eventticketing.backend.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(UserController::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var userService: UserService

    @MockBean
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    private val testUserId = UUID.randomUUID()

    @Test
    @WithMockUser(username = "user@example.com")
    fun `get current user - success`() {
        // Arrange
        val userDto = UserDto(
            id = testUserId,
            email = "user@example.com",
            fullName = "Test User",
            phoneNumber = "+84123456789",
            role = UserRole.USER,
            enabled = true,
            createdAt = LocalDateTime.now()
        )

        `when`(userService.getCurrentUser()).thenReturn(userDto)

        // Act & Assert
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("user@example.com"))
    }

    @Test
    @WithMockUser(username = "user@example.com")
    fun `update current user - success`() {
        // Arrange
        val updateDto = UserUpdateDto(fullName = "Updated Name", phoneNumber = "+84987654321")
        
        val currentUserDto = UserDto(
            id = testUserId,
            email = "user@example.com",
            fullName = "Test User",
            phoneNumber = "+84123456789",
            role = UserRole.USER,
            enabled = true,
            createdAt = LocalDateTime.now()
        )
        
        val updatedUserDto = UserDto(
            id = testUserId,
            email = "user@example.com",
            fullName = "Updated Name",
            phoneNumber = "+84987654321",
            role = UserRole.USER,
            enabled = true,
            createdAt = LocalDateTime.now()
        )

        `when`(userService.getCurrentUser()).thenReturn(currentUserDto)
        `when`(userService.updateUser(eq(testUserId), any())).thenReturn(updatedUserDto)

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
            .andExpect(jsonPath("$.data.phoneNumber").value("+84987654321"))
    }

    @Test
    @WithMockUser(username = "user@example.com")
    fun `change password - success`() {
        // Arrange
        val passwordChangeDto = PasswordChangeDto(
            currentPassword = "oldPassword",
            newPassword = "newPassword",
            confirmPassword = "newPassword"
        )
        
        val currentUserDto = UserDto(
            id = testUserId,
            email = "user@example.com",
            fullName = "Test User",
            phoneNumber = "+84123456789",
            role = UserRole.USER,
            enabled = true,
            createdAt = LocalDateTime.now()
        )

        `when`(userService.getCurrentUser()).thenReturn(currentUserDto)
        `when`(userService.changePassword(eq(testUserId), any())).thenReturn(true)

        // Act & Assert
        mockMvc.perform(post("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordChangeDto)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `get user by id - admin only - success`() {
        // Arrange
        val userDto = UserDto(
            id = testUserId,
            email = "user@example.com",
            fullName = "Test User",
            phoneNumber = "+84123456789",
            role = UserRole.USER,
            enabled = true,
            createdAt = LocalDateTime.now()
        )

        `when`(userService.getUserById(testUserId)).thenReturn(userDto)

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", testUserId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = ["USER"])
    fun `get user by id - non-admin - forbidden`() {
        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", testUserId))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `get all users - admin only - success`() {
        // Arrange
        val users = listOf(
            UserDto(
                id = testUserId,
                email = "user1@example.com",
                fullName = "User One",
                phoneNumber = "+84123456789",
                role = UserRole.USER,
                enabled = true,
                createdAt = LocalDateTime.now()
            ),
            UserDto(
                id = UUID.randomUUID(),
                email = "user2@example.com",
                fullName = "User Two",
                phoneNumber = "+84987654321",
                role = UserRole.ORGANIZER,
                enabled = true,
                createdAt = LocalDateTime.now()
            )
        )
        
        val pageResponse: Page<UserDto> = PageImpl(users)

        `when`(userService.getAllUsers(any(Pageable::class.java))).thenReturn(pageResponse)

        // Act & Assert
        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content.length()").value(2))
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = ["USER"])
    fun `get all users - non-admin - forbidden`() {
        // Act & Assert
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `deactivate user - admin only - success`() {
        // Arrange
        `when`(userService.deactivateUser(testUserId)).thenReturn(true)

        // Act & Assert
        mockMvc.perform(post("/api/users/{id}/deactivate", testUserId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `deactivate user - user not found`() {
        // Arrange
        `when`(userService.deactivateUser(testUserId)).thenThrow(
            ResourceNotFoundException("Không tìm thấy người dùng với ID $testUserId")
        )

        // Act & Assert
        mockMvc.perform(post("/api/users/{id}/deactivate", testUserId))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Không tìm thấy người dùng với ID $testUserId"))
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `update user role - admin only - success`() {
        // Arrange
        val updatedUserDto = UserDto(
            id = testUserId,
            email = "user@example.com",
            fullName = "Test User",
            phoneNumber = "+84123456789",
            role = UserRole.ORGANIZER,
            enabled = true,
            createdAt = LocalDateTime.now()
        )

        `when`(userService.updateUserRole(eq(testUserId), eq("ORGANIZER"))).thenReturn(updatedUserDto)

        // Act & Assert
        mockMvc.perform(post("/api/users/{id}/role", testUserId)
                .param("role", "ORGANIZER"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.data.role").value("ORGANIZER"))
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `update user role - invalid role`() {
        // Arrange
        `when`(userService.updateUserRole(eq(testUserId), eq("INVALID_ROLE"))).thenThrow(
            IllegalArgumentException("Vai trò không hợp lệ: INVALID_ROLE")
        )

        // Act & Assert
        mockMvc.perform(post("/api/users/{id}/role", testUserId)
                .param("role", "INVALID_ROLE"))
            .andExpect(status().isBadRequest)
    }
} 