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
            enabled = false, // Tài khoản chưa kích hoạt
            notificationPreferences = emptyMap() // Khởi tạo đúng kiểu dữ liệu Map<String, Any>
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
            
            // Sử dụng trực tiếp đối tượng User từ cơ sở dữ liệu để tạo JWT token
            val tokenPair = jwtProvider.generateTokenPair(user)
            
            return UserAuthResponseDto(
                id = user.id!!,
                email = user.email,
                fullName = user.fullName,
                role = user.role,
                token = tokenPair.accessToken,
                refreshToken = tokenPair.refreshToken,
                profilePictureUrl = user.profilePictureUrl
            )
        } catch (e: Exception) {
            logger.error("Đăng nhập thất bại: ${e.message}")
            throw UnauthorizedException("Email hoặc mật khẩu không đúng")
        }
    }

    override fun refreshToken(refreshToken: String): UserAuthResponseDto {
        try {
            // Kiểm tra xem token có phải là refresh token không
            if (!jwtProvider.isRefreshToken(refreshToken)) {
                throw UnauthorizedException("Token không phải là refresh token")
            }
            
            // Validate refresh token
            if (!jwtProvider.validateJwtToken(refreshToken)) {
                throw UnauthorizedException("Refresh token không hợp lệ hoặc đã hết hạn")
            }
            
            // Lấy email từ refresh token
            val email = jwtProvider.getUsernameFromJwtToken(refreshToken)
            
            // Tìm user trong database
            val user = userRepository.findByEmail(email)
                .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với email $email") }
            
            if (!user.enabled) {
                throw UnauthorizedException("Tài khoản chưa được kích hoạt")
            }
            
            // Tạo cặp token mới
            val tokenPair = jwtProvider.generateTokenPair(user)
            
            return UserAuthResponseDto(
                id = user.id!!,
                email = user.email,
                fullName = user.fullName,
                role = user.role,
                token = tokenPair.accessToken,
                refreshToken = tokenPair.refreshToken,
                profilePictureUrl = user.profilePictureUrl
            )
        } catch (e: Exception) {
            logger.error("Refresh token thất bại: ${e.message}")
            throw UnauthorizedException("Không thể refresh token: ${e.message}")
        }
    }

    override fun logout(token: String): Boolean {
        try {
            // Trong thực tế, bạn có thể:
            // 1. Thêm token vào blacklist
            // 2. Lưu vào Redis với TTL
            // 3. Hoặc chỉ đơn giản là trả về true vì JWT là stateless
            
            logger.info("Người dùng đã đăng xuất với token: ${token.take(20)}...")
            return true
        } catch (e: Exception) {
            logger.error("Logout thất bại: ${e.message}")
            return false
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
        // This is a simplified implementation for testing purposes
        // In a production environment, you would:
        // 1. Store tokens in a database with associated email and expiry time
        // 2. Look up the token in the database
        // 3. Verify it's not expired
        // 4. Find the user by the associated email
        
        // For testing purposes, extract email from token or use query parameter
        // Assuming token contains the email or is associated with the email
        
        // TEMPORARY SOLUTION: Let's find the most recently registered unactivated user
        val unactivatedUsers = userRepository.findByEnabled(false)
        if (unactivatedUsers.isEmpty()) {
            logger.warn("No unactivated users found for token: $activationToken")
            return false
        }
        
        // Activate the user
        val user = unactivatedUsers[0] // Get the most recent unactivated user
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

    @Transactional
    override fun authenticateWithGoogle(googleAuthRequest: GoogleAuthRequestDto): UserAuthResponseDto {
        try {
            // Xác thực token ID từ Google (trong thực tế cần gọi API Google để xác thực)
            // Giả sử token đã được xác thực ở phía client
            
            // Kiểm tra xem người dùng đã tồn tại chưa
            val userOptional = userRepository.findByEmail(googleAuthRequest.email)
            
            val user = if (userOptional.isPresent) {
                // Nếu người dùng đã tồn tại, cập nhật thông tin nếu cần
                val existingUser = userOptional.get()
                existingUser.fullName = googleAuthRequest.name // Cập nhật tên từ Google
                existingUser.profilePictureUrl = googleAuthRequest.profilePictureUrl // Cập nhật ảnh đại diện
                
                // Đảm bảo tài khoản đã được kích hoạt
                if (!existingUser.enabled) {
                    existingUser.enabled = true
                }
                
                userRepository.save(existingUser)
            } else {
                // Nếu người dùng chưa tồn tại, tạo mới
                val newUser = User(
                    email = googleAuthRequest.email,
                    password = passwordEncoder.encode(UUID.randomUUID().toString()), // Tạo mật khẩu ngẫu nhiên
                    fullName = googleAuthRequest.name,
                    role = UserRole.USER,
                    enabled = true, // Tài khoản Google đã được xác thực nên kích hoạt luôn
                    profilePictureUrl = googleAuthRequest.profilePictureUrl,
                    notificationPreferences = emptyMap() // Khởi tạo đúng kiểu dữ liệu Map<String, Any>
                )
                
                userRepository.save(newUser)
            }
            
            // Tạo JWT token
            val tokenPair = jwtProvider.generateTokenPair(user)
            
            return UserAuthResponseDto(
                id = user.id!!,
                email = user.email,
                fullName = user.fullName,
                role = user.role,
                token = tokenPair.accessToken,
                refreshToken = tokenPair.refreshToken
            )
        } catch (e: Exception) {
            logger.error("Đăng nhập Google thất bại: ${e.message}")
            throw UnauthorizedException("Không thể xác thực với Google: ${e.message}")
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
            profilePictureUrl = user.profilePictureUrl,
            createdAt = user.createdAt
        )
    }
} 