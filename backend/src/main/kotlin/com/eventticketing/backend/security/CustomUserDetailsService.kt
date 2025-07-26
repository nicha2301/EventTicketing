package com.eventticketing.backend.security

import com.eventticketing.backend.entity.UserRole
import com.eventticketing.backend.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("Không tìm thấy người dùng với email: $username") }

        val authorities = mutableListOf<SimpleGrantedAuthority>().apply {
            add(SimpleGrantedAuthority("ROLE_${user.role}"))
            
            when (user.role) {
                UserRole.ADMIN -> {
                    add(SimpleGrantedAuthority("PRIVILEGE_READ"))
                    add(SimpleGrantedAuthority("PRIVILEGE_WRITE"))
                    add(SimpleGrantedAuthority("PRIVILEGE_DELETE"))
                }
                UserRole.ORGANIZER -> {
                    add(SimpleGrantedAuthority("PRIVILEGE_READ"))
                    add(SimpleGrantedAuthority("PRIVILEGE_WRITE"))
                }
                UserRole.USER -> {
                    add(SimpleGrantedAuthority("PRIVILEGE_READ"))
                }
            }
        }

        return UserPrincipal(
            id = user.id!!,
            email = user.email,
            password = user.password,
            authorities = authorities,
            fullName = user.fullName
        )
    }
} 