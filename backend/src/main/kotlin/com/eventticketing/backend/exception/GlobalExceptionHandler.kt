package com.eventticketing.backend.exception

import com.eventticketing.backend.dto.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime
import java.util.*

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<ErrorDetails>> {
        val errorDetails = ErrorDetails(
            timestamp = LocalDateTime.now(),
            message = ex.message ?: "Không tìm thấy tài nguyên",
            path = request.getDescription(false)
        )
        
        return ResponseEntity(ApiResponse(success = false, message = ex.message, data = errorDetails), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(ResourceAlreadyExistsException::class)
    fun handleResourceAlreadyExistsException(
        ex: ResourceAlreadyExistsException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<ErrorDetails>> {
        val errorDetails = ErrorDetails(
            timestamp = LocalDateTime.now(),
            message = ex.message ?: "Tài nguyên đã tồn tại",
            path = request.getDescription(false)
        )
        
        return ResponseEntity(
            ApiResponse(success = false, message = ex.message, data = errorDetails),
            HttpStatus.CONFLICT
        )
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(
        ex: BadRequestException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<ErrorDetails>> {
        val errorDetails = ErrorDetails(
            timestamp = LocalDateTime.now(),
            message = ex.message ?: "Yêu cầu không hợp lệ",
            path = request.getDescription(false)
        )
        
        return ResponseEntity(
            ApiResponse(success = false, message = ex.message, data = errorDetails),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(
        ex: UnauthorizedException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<ErrorDetails>> {
        val errorDetails = ErrorDetails(
            timestamp = LocalDateTime.now(),
            message = ex.message ?: "Không có quyền truy cập",
            path = request.getDescription(false)
        )
        
        return ResponseEntity(
            ApiResponse(success = false, message = ex.message, data = errorDetails),
            HttpStatus.UNAUTHORIZED
        )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: AccessDeniedException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<ErrorDetails>> {
        val errorDetails = ErrorDetails(
            timestamp = LocalDateTime.now(),
            message = "Không có quyền thực hiện hành động này",
            path = request.getDescription(false)
        )
        
        return ResponseEntity(
            ApiResponse(success = false, message = "Không có quyền thực hiện hành động này", data = errorDetails),
            HttpStatus.FORBIDDEN
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val errors = HashMap<String, String>()
        
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.getDefaultMessage()
            errors[fieldName] = errorMessage ?: "Lỗi không xác định"
        }
        
        val errorDetails = mapOf(
            "timestamp" to LocalDateTime.now(),
            "message" to "Dữ liệu đầu vào không hợp lệ",
            "path" to request.getDescription(false),
            "errors" to errors
        )
        
        return ResponseEntity(
            ApiResponse(success = false, message = "Dữ liệu đầu vào không hợp lệ", data = errorDetails),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiResponse<ErrorDetails>> {
        logger.error("Lỗi không mong đợi: ", ex)
        
        val errorDetails = ErrorDetails(
            timestamp = LocalDateTime.now(),
            message = "Đã xảy ra lỗi. Vui lòng thử lại sau",
            path = request.getDescription(false)
        )
        
        return ResponseEntity(
            ApiResponse(success = false, message = "Đã xảy ra lỗi. Vui lòng thử lại sau", data = errorDetails),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    data class ErrorDetails(
        val timestamp: LocalDateTime,
        val message: String,
        val path: String
    )
} 