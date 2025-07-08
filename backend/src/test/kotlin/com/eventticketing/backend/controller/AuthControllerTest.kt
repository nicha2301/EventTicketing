package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.LoginRequestDto
import com.eventticketing.backend.dto.UserAuthResponseDto
import com.eventticketing.backend.dto.UserCreateDto
import com.eventticketing.backend.dto.UserDto
import com.eventticketing.backend.entity.UserRole
import com.eventticketing.backend.exception.ResourceAlreadyExistsException
import com.eventticketing.backend.exception.UnauthorizedException
import com.eventticketing.backend.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(AuthController::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var userService: UserService

    @Test
    fun `register user - success`() {
        // Arrange
        val userId = UUID.randomUUID()
        val userCreateDto = UserCreateDto(
            email = "test@example.com",
            password = "password123",
            fullName = "Test User",
            phoneNumber = "+84123456789"
        )

        val userDto = UserDto(
            id = userId,
            email = "test@example.com",
            fullName = "Test User",
            phoneNumber = "+84123456789",
            role = UserRole.USER,
            enabled = false,
            createdAt = LocalDateTime.now()
        )

        `when`(userService.registerUser(any())).thenReturn(userDto)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDto))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.data.id").value(userId.toString()))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
    }

    @Test
    fun `register user - email already exists`() {
        // Arrange
        val userCreateDto = UserCreateDto(
            email = "existing@example.com",
            password = "password123",
            fullName = "Existing User",
            phoneNumber = "+84123456789"
        )

        `when`(userService.registerUser(any())).thenThrow(
            ResourceAlreadyExistsException("Email existing@example.com đã được sử dụng")
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDto))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Email existing@example.com đã được sử dụng"))
    }

    @Test
    fun `login - success`() {
        // Arrange
        val userId = UUID.randomUUID()
        val loginRequest = LoginRequestDto(
            email = "test@example.com",
            password = "password123"
        )

        val authResponse = UserAuthResponseDto(
            id = userId,
            email = "test@example.com",
            fullName = "Test User",
            role = UserRole.USER,
            token = "jwt-token"
        )

        `when`(userService.authenticateUser(any())).thenReturn(authResponse)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").value("jwt-token"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
    }

    @Test
    fun `login - invalid credentials`() {
        // Arrange
        val loginRequest = LoginRequestDto(
            email = "test@example.com",
            password = "wrongpassword"
        )

        `when`(userService.authenticateUser(any())).thenThrow(
            UnauthorizedException("Email hoặc mật khẩu không đúng")
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Email hoặc mật khẩu không đúng"))
    }

    @Test
    fun `activate user - success`() {
        // Arrange
        val token = "valid-activation-token"

        `when`(userService.activateUser(token)).thenReturn(true)

        // Act & Assert
        mockMvc.perform(
            get("/api/auth/activate")
                .param("token", token)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("Kích hoạt thành công"))
    }

    @Test
    fun `activate user - invalid token`() {
        // Arrange
        val token = "invalid-activation-token"

        `when`(userService.activateUser(token)).thenReturn(false)

        // Act & Assert
        mockMvc.perform(
            get("/api/auth/activate")
                .param("token", token)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `request password reset - success`() {
        // Arrange
        val email = "test@example.com"

        `when`(userService.requestPasswordReset(email)).thenReturn(true)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/password/forgot")
                .param("email", email)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `reset password - success`() {
        // Arrange
        val token = "valid-reset-token"
        val newPassword = "newPassword123"

        `when`(userService.resetPassword(token, newPassword)).thenReturn(true)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/password/reset")
                .param("token", token)
                .param("newPassword", newPassword)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `reset password - invalid token`() {
        // Arrange
        val token = "invalid-reset-token"
        val newPassword = "newPassword123"

        `when`(userService.resetPassword(token, newPassword)).thenReturn(false)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/password/reset")
                .param("token", token)
                .param("newPassword", newPassword)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }
} 