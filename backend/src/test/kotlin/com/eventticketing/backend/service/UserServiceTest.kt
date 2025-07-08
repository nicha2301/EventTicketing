package com.eventticketing.backend.service

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.entity.User
import com.eventticketing.backend.entity.UserRole
import com.eventticketing.backend.exception.ResourceAlreadyExistsException
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.exception.UnauthorizedException
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.security.JwtProvider
import com.eventticketing.backend.service.impl.UserServiceImpl
import com.eventticketing.backend.util.EmailService
import com.eventticketing.backend.util.SecurityUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var authenticationManager: AuthenticationManager

    @Mock
    private lateinit var jwtProvider: JwtProvider

    @Mock
    private lateinit var emailService: EmailService

    @Mock
    private lateinit var securityUtils: SecurityUtils

    @Mock
    private lateinit var authentication: Authentication

    private lateinit var userService: UserService
    private lateinit var testUser: User
    private val testUserId = UUID.randomUUID()
    private val testEmail = "test@example.com"
    private val testPassword = "password"
    private val testEncodedPassword = "encodedPassword"

    @BeforeEach
    fun setup() {
        userService = UserServiceImpl(
            userRepository,
            passwordEncoder,
            authenticationManager,
            jwtProvider,
            emailService,
            securityUtils
        )

        testUser = User(
            id = testUserId,
            email = testEmail,
            password = testEncodedPassword,
            fullName = "Test User",
            phoneNumber = "+84123456789",
            role = UserRole.USER,
            enabled = true,
            createdAt = LocalDateTime.now()
        )

        `when`(passwordEncoder.encode(testPassword)).thenReturn(testEncodedPassword)
    }

    @Test
    fun `registerUser - successful registration`() {
        // Arrange
        val createDto = UserCreateDto(
            email = testEmail,
            password = testPassword,
            fullName = "Test User",
            phoneNumber = "+84123456789"
        )

        `when`(userRepository.existsByEmail(testEmail)).thenReturn(false)
        `when`(userRepository.save(any(User::class.java))).thenReturn(testUser)

        // Act
        val result = userService.registerUser(createDto)

        // Assert
        assertNotNull(result)
        assertEquals(testEmail, result.email)
        assertEquals("Test User", result.fullName)
        verify(emailService, times(1)).sendActivationEmail(eq(testEmail), eq("Test User"), any())
    }

    @Test
    fun `registerUser - throws exception when email already exists`() {
        // Arrange
        val createDto = UserCreateDto(
            email = testEmail,
            password = testPassword,
            fullName = "Test User",
            phoneNumber = "+84123456789"
        )

        `when`(userRepository.existsByEmail(testEmail)).thenReturn(true)

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException::class.java) {
            userService.registerUser(createDto)
        }
    }

    @Test
    fun `authenticateUser - successful authentication`() {
        // Arrange
        val loginRequest = LoginRequestDto(email = testEmail, password = testPassword)
        
        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenReturn(authentication)
        `when`(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser))
        `when`(jwtProvider.generateToken(authentication)).thenReturn("jwt-token")

        // Act
        val result = userService.authenticateUser(loginRequest)

        // Assert
        assertNotNull(result)
        assertEquals(testUserId, result.id)
        assertEquals(testEmail, result.email)
        assertEquals("jwt-token", result.token)
    }

    @Test
    fun `authenticateUser - throws exception when user is disabled`() {
        // Arrange
        val loginRequest = LoginRequestDto(email = testEmail, password = testPassword)
        val disabledUser = testUser.copy(enabled = false)
        
        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenReturn(authentication)
        `when`(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(disabledUser))

        // Act & Assert
        assertThrows(UnauthorizedException::class.java) {
            userService.authenticateUser(loginRequest)
        }
    }

    @Test
    fun `getUserById - returns user when exists`() {
        // Arrange
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))

        // Act
        val result = userService.getUserById(testUserId)

        // Assert
        assertNotNull(result)
        assertEquals(testUserId, result.id)
        assertEquals(testEmail, result.email)
    }

    @Test
    fun `getUserById - throws exception when user not found`() {
        // Arrange
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            userService.getUserById(testUserId)
        }
    }

    @Test
    fun `getCurrentUser - returns current user`() {
        // Arrange
        `when`(securityUtils.getCurrentUserId()).thenReturn(testUserId)
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))

        // Act
        val result = userService.getCurrentUser()

        // Assert
        assertNotNull(result)
        assertEquals(testUserId, result.id)
        assertEquals(testEmail, result.email)
    }

    @Test
    fun `getCurrentUser - throws exception when no current user`() {
        // Arrange
        `when`(securityUtils.getCurrentUserId()).thenReturn(null)

        // Act & Assert
        assertThrows(UnauthorizedException::class.java) {
            userService.getCurrentUser()
        }
    }

    @Test
    fun `updateUser - updates user successfully when current user`() {
        // Arrange
        val updateDto = UserUpdateDto(fullName = "Updated Name", phoneNumber = "+84987654321")
        
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        `when`(securityUtils.isCurrentUserOrAdmin(testUserId)).thenReturn(true)
        `when`(userRepository.save(any(User::class.java))).thenReturn(
            testUser.copy(fullName = "Updated Name", phoneNumber = "+84987654321")
        )

        // Act
        val result = userService.updateUser(testUserId, updateDto)

        // Assert
        assertNotNull(result)
        assertEquals("Updated Name", result.fullName)
        assertEquals("+84987654321", result.phoneNumber)
    }

    @Test
    fun `updateUser - throws exception when not authorized`() {
        // Arrange
        val updateDto = UserUpdateDto(fullName = "Updated Name")
        
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        `when`(securityUtils.isCurrentUserOrAdmin(testUserId)).thenReturn(false)

        // Act & Assert
        assertThrows(UnauthorizedException::class.java) {
            userService.updateUser(testUserId, updateDto)
        }
    }

    @Test
    fun `getAllUsers - returns paginated users for admin`() {
        // Arrange
        val pageable = PageRequest.of(0, 10)
        val users = listOf(
            testUser,
            testUser.copy(id = UUID.randomUUID(), email = "user2@example.com")
        )
        val page = PageImpl(users, pageable, users.size.toLong())
        
        `when`(securityUtils.isAdmin()).thenReturn(true)
        `when`(userRepository.findAll(pageable)).thenReturn(page)

        // Act
        val result = userService.getAllUsers(pageable)

        // Assert
        assertNotNull(result)
        assertEquals(2, result.totalElements)
        assertEquals(2, result.content.size)
    }

    @Test
    fun `getAllUsers - throws exception when not admin`() {
        // Arrange
        val pageable = PageRequest.of(0, 10)
        `when`(securityUtils.isAdmin()).thenReturn(false)

        // Act & Assert
        assertThrows(UnauthorizedException::class.java) {
            userService.getAllUsers(pageable)
        }
    }

    @Test
    fun `deactivateUser - deactivates user when admin`() {
        // Arrange
        `when`(securityUtils.isAdmin()).thenReturn(true)
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        val deactivatedUser = testUser.copy(enabled = false)
        `when`(userRepository.save(any())).thenReturn(deactivatedUser)

        // Act
        val result = userService.deactivateUser(testUserId)

        // Assert
        assertTrue(result)
        verify(userRepository, times(1)).save(argThat { !it.enabled })
    }

    @Test
    fun `updateUserRole - updates role when admin`() {
        // Arrange
        `when`(securityUtils.isAdmin()).thenReturn(true)
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        val updatedUser = testUser.copy(role = UserRole.ORGANIZER)
        `when`(userRepository.save(any())).thenReturn(updatedUser)

        // Act
        val result = userService.updateUserRole(testUserId, "ORGANIZER")

        // Assert
        assertEquals(UserRole.ORGANIZER, result.role)
        verify(userRepository, times(1)).save(argThat { it.role == UserRole.ORGANIZER })
    }

    @Test
    fun `updateUserRole - throws exception when not admin`() {
        // Arrange
        `when`(securityUtils.isAdmin()).thenReturn(false)

        // Act & Assert
        assertThrows(UnauthorizedException::class.java) {
            userService.updateUserRole(testUserId, "ORGANIZER")
        }
    }
} 