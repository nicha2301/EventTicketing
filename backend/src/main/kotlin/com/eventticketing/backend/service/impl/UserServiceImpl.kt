package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.*
import com.eventticketing.backend.entity.User
import com.eventticketing.backend.entity.UserRole
import com.eventticketing.backend.exception.BadRequestException
import com.eventticketing.backend.exception.ResourceAlreadyExistsException
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.exception.UnauthorizedException
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.security.JwtProvider
import com.eventticketing.backend.service.UserService
import com.eventticketing.backend.util.EmailService
import com.eventticketing.backend.util.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtProvider: JwtProvider,
    private val emailService: EmailService,
    private val securityUtils: SecurityUtils
) : UserService {

    private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

    @Transactional
    override fun registerUser(userCreateDto: UserCreateDto): UserDto {
        if (userRepository.existsByEmail(userCreateDto.email)) {
            throw ResourceAlreadyExistsException("Email ${userCreateDto.email} đã được sử dụng")
        }

        val user = User(
            email = userCreateDto.email,
            password = passwordEncoder.encode(userCreateDto.password),
            fullName = userCreateDto.fullName,
            phoneNumber = userCreateDto.phoneNumber,
            role = userCreateDto.role,
            enabled = false // Tài khoản chưa kích hoạt
        )

        val savedUser = userRepository.save(user)
        
        // Tạo activation token
        val activationToken = UUID.randomUUID().toString()
        
        // Gửi email xác thực
        emailService.sendActivationEmail(savedUser.email, savedUser.fullName, activationToken)
        
        logger.info("Đã đăng ký người dùng mới: ${savedUser.email}")
        return mapToUserDto(savedUser)
    }

    override fun authenticateUser(loginRequest: LoginRequestDto): UserAuthResponseDto {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.email,
                    loginRequest.password
                )
            )
            SecurityContextHolder.getContext().authentication = authentication
            
            val user = userRepository.findByEmail(loginRequest.email)
                .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với email ${loginRequest.email}") }
            
            if (!user.enabled) {
                throw UnauthorizedException("Tài khoản chưa được kích hoạt")
            }
            
            val jwt = jwtProvider.generateJwtToken(authentication)
            
            return UserAuthResponseDto(
                id = user.id!!,
                email = user.email,
                fullName = user.fullName,
                role = user.role,
                token = jwt
            )
        } catch (e: Exception) {
            logger.error("Đăng nhập thất bại: ${e.message}")
            throw UnauthorizedException("Email hoặc mật khẩu không đúng")
        }
    }

    override fun getUserById(id: UUID): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với ID $id") }
        return mapToUserDto(user)
    }

    override fun getUserByEmail(email: String): UserDto {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với email $email") }
        return mapToUserDto(user)
    }

    override fun getCurrentUser(): UserDto {
        val currentUserId = securityUtils.getCurrentUserId()
            ?: throw UnauthorizedException("Không có người dùng đăng nhập")
        return getUserById(currentUserId)
    }

    @Transactional
    override fun updateUser(id: UUID, userUpdateDto: UserUpdateDto): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với ID $id") }
        
        // Chỉ cho phép người dùng cập nhật thông tin của chính họ hoặc admin có thể cập nhật tất cả
        if (!securityUtils.isCurrentUserOrAdmin(id)) {
            throw UnauthorizedException("Bạn không có quyền cập nhật thông tin người dùng này")
        }
        
        userUpdateDto.fullName?.let { user.fullName = it }
        userUpdateDto.phoneNumber?.let { user.phoneNumber = it }
        
        // Chỉ admin mới có thể thay đổi trạng thái enabled
        if (securityUtils.isAdmin() && userUpdateDto.enabled != null) {
            user.enabled = userUpdateDto.enabled
        }
        
        val updatedUser = userRepository.save(user)
        return mapToUserDto(updatedUser)
    }

    @Transactional
    override fun changePassword(id: UUID, passwordChangeDto: PasswordChangeDto): Boolean {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với ID $id") }
        
        // Chỉ cho phép người dùng đổi mật khẩu của chính họ
        if (!securityUtils.isCurrentUser(id)) {
            throw UnauthorizedException("Bạn không có quyền đổi mật khẩu người dùng này")
        }
        
        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(passwordChangeDto.currentPassword, user.password)) {
            throw BadRequestException("Mật khẩu hiện tại không đúng")
        }
        
        // Cập nhật mật khẩu mới
        user.password = passwordEncoder.encode(passwordChangeDto.newPassword)
        userRepository.save(user)
        
        logger.info("Đã đổi mật khẩu cho người dùng: ${user.email}")
        return true
    }

    @Transactional
    override fun requestPasswordReset(email: String): Boolean {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với email $email") }
        
        // Tạo reset token
        val resetToken = UUID.randomUUID().toString()
        
        // Gửi email reset mật khẩu
        emailService.sendPasswordResetEmail(user.email, user.fullName, resetToken)
        
        logger.info("Đã gửi yêu cầu đặt lại mật khẩu cho: ${user.email}")
        return true
    }

    @Transactional
    override fun resetPassword(token: String, newPassword: String): Boolean {
        // Trong thực tế, token nên được lưu trong database hoặc cache với thời hạn
        // Ở đây chúng ta giả định rằng token hợp lệ và lấy email từ token
        val email = "user@example.com" // Giả định lấy từ token
        
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với email $email") }
        
        user.password = passwordEncoder.encode(newPassword)
        userRepository.save(user)
        
        logger.info("Đã đặt lại mật khẩu cho người dùng: ${user.email}")
        return true
    }

    override fun getAllUsers(pageable: Pageable): Page<UserDto> {
        if (!securityUtils.isAdmin()) {
            throw UnauthorizedException("Chỉ admin mới có quyền xem danh sách người dùng")
        }
        
        return userRepository.findAll(pageable).map(this::mapToUserDto)
    }

    @Transactional
    override fun activateUser(activationToken: String): Boolean {
        // Trong thực tế, token nên được lưu trong database hoặc cache với thời hạn
        // Ở đây chúng ta giả định rằng token hợp lệ và lấy email từ token
        val email = "user@example.com" // Giả định lấy từ token
        
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với email $email") }
        
        user.enabled = true
        userRepository.save(user)
        
        logger.info("Đã kích hoạt tài khoản cho người dùng: ${user.email}")
        return true
    }

    @Transactional
    override fun deactivateUser(id: UUID): Boolean {
        if (!securityUtils.isAdmin()) {
            throw UnauthorizedException("Chỉ admin mới có quyền vô hiệu hóa tài khoản")
        }
        
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với ID $id") }
        
        user.enabled = false
        userRepository.save(user)
        
        logger.info("Đã vô hiệu hóa tài khoản người dùng: ${user.email}")
        return true
    }

    @Transactional
    override fun updateUserRole(id: UUID, role: String): UserDto {
        if (!securityUtils.isAdmin()) {
            throw UnauthorizedException("Chỉ admin mới có quyền phân quyền người dùng")
        }
        
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với ID $id") }
        
        try {
            val userRole = UserRole.valueOf(role.uppercase())
            user.role = userRole
            val updatedUser = userRepository.save(user)
            
            logger.info("Đã cập nhật vai trò ${userRole} cho người dùng: ${user.email}")
            return mapToUserDto(updatedUser)
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("Vai trò không hợp lệ: $role. Các vai trò hợp lệ: ${UserRole.values().joinToString()}")
        }
    }

    private fun mapToUserDto(user: User): UserDto {
        return UserDto(
            id = user.id,
            email = user.email,
            fullName = user.fullName,
            phoneNumber = user.phoneNumber,
            role = user.role,
            enabled = user.enabled,
            createdAt = user.createdAt
        )
    }
} 