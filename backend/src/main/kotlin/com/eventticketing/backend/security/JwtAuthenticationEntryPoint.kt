package com.eventticketing.backend.security

import com.eventticketing.backend.dto.ApiResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        
        val body = ApiResponse.error<String>(
            message = "Không được phép truy cập: ${authException.message}",
            code = HttpServletResponse.SC_UNAUTHORIZED
        )
        
        val objectMapper = ObjectMapper()
        objectMapper.writeValue(response.outputStream, body)
    }
} 