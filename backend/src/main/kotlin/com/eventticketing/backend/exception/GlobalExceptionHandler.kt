package com.eventticketing.backend.exception

import com.eventticketing.backend.dto.ApiResponse
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.BindException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException
import java.util.*

/**
 * Xử lý tập trung các exception trong ứng dụng
 */
@RestControllerAdvice
class GlobalExceptionHandler(private val messageSource: MessageSource) {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    // Enum định nghĩa các mã lỗi
    enum class ErrorCode(val status: HttpStatus, val defaultMessage: String) {
        RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên"),
        BAD_REQUEST(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ"),
        UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Không được phép truy cập"),
        FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập tài nguyên này"),
        CONFLICT(HttpStatus.CONFLICT, "Lỗi toàn vẹn dữ liệu. Có thể dữ liệu đã tồn tại hoặc vi phạm ràng buộc."),
        PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "Kích thước tệp tải lên vượt quá giới hạn cho phép"),
        METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Phương thức HTTP không được hỗ trợ"),
        UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Kiểu dữ liệu không được hỗ trợ"),
        INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.")
    }
    
    /**
     * Phương thức chung để tạo ResponseEntity từ exception
     */
    private fun <T> createErrorResponse(
        errorCode: ErrorCode,
        message: String? = null,
        data: T? = null,
        ex: Exception? = null
    ): ResponseEntity<ApiResponse<T>> {
        val errorMessage = message ?: errorCode.defaultMessage
        
        // Log exception với mức độ phù hợp
        when (errorCode.status.series()) {
            HttpStatus.Series.SERVER_ERROR -> logger.error("${errorCode.name}: $errorMessage", ex)
            HttpStatus.Series.CLIENT_ERROR -> logger.warn("${errorCode.name}: $errorMessage")
            else -> logger.info("${errorCode.name}: $errorMessage")
        }
        
        return ResponseEntity
            .status(errorCode.status)
            .body(ApiResponse.error(errorMessage, data))
    }
    
    // Xử lý AppException và các lớp con
    @ExceptionHandler(AppException::class)
    fun handleAppException(ex: AppException): ResponseEntity<ApiResponse<Nothing>> {
        // Log exception với mức độ phù hợp
        when (ex.errorType.status.series()) {
            HttpStatus.Series.SERVER_ERROR -> logger.error("${ex.errorType.prefix}: ${ex.message}", ex)
            HttpStatus.Series.CLIENT_ERROR -> logger.warn("${ex.errorType.prefix}: ${ex.message}")
            else -> logger.info("${ex.errorType.prefix}: ${ex.message}")
        }
        
        return ResponseEntity
            .status(ex.errorType.status)
            .body(ApiResponse.error(ex.message))
    }
    
    // Xử lý các exception của Spring
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Map<String, String>>> {
        val locale = LocaleContextHolder.getLocale()
        val errors = ex.bindingResult.fieldErrors.associate { error ->
            val errorMessage = try {
                messageSource.getMessage(error, locale)
            } catch (e: Exception) {
                error.defaultMessage ?: "Lỗi xác thực"
            }
            error.field to errorMessage
        }
        
        return createErrorResponse(ErrorCode.BAD_REQUEST, "Dữ liệu không hợp lệ", errors, ex)
    }
    
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ApiResponse<Map<String, String>>> {
        val errors = ex.constraintViolations.associate { violation ->
            val path = violation.propertyPath.toString()
            val field = path.substring(path.lastIndexOf('.') + 1)
            field to violation.message
        }
        
        return createErrorResponse(ErrorCode.BAD_REQUEST, "Dữ liệu không hợp lệ", errors, ex)
    }
    
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ApiResponse<Nothing>> {
        return createErrorResponse(ErrorCode.FORBIDDEN, null, null, ex)
    }
    
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException): ResponseEntity<ApiResponse<Nothing>> {
        return createErrorResponse(ErrorCode.UNAUTHORIZED, "Thông tin đăng nhập không chính xác", null, ex)
    }
    
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: EntityNotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        return createErrorResponse(ErrorCode.RESOURCE_NOT_FOUND, ex.message, null, ex)
    }
    
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(ex: DataIntegrityViolationException): ResponseEntity<ApiResponse<Nothing>> {
        return createErrorResponse(ErrorCode.CONFLICT, null, null, ex)
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceededException(ex: MaxUploadSizeExceededException): ResponseEntity<ApiResponse<Nothing>> {
        return createErrorResponse(ErrorCode.PAYLOAD_TOO_LARGE, null, null, ex)
    }
    
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException): ResponseEntity<ApiResponse<Nothing>> {
        return createErrorResponse(
            ErrorCode.RESOURCE_NOT_FOUND,
            "Không tìm thấy API endpoint: ${ex.requestURL}",
            null,
            ex
        )
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Nothing>> {
        return createErrorResponse(
            ErrorCode.METHOD_NOT_ALLOWED,
            "Phương thức HTTP không được hỗ trợ: ${ex.method}",
            null,
            ex
        )
    }
    
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupportedException(ex: HttpMediaTypeNotSupportedException): ResponseEntity<ApiResponse<Nothing>> {
        return createErrorResponse(
            ErrorCode.UNSUPPORTED_MEDIA_TYPE,
            "Kiểu dữ liệu không được hỗ trợ: ${ex.contentType}",
            null,
            ex
        )
    }
    
    // Xử lý tất cả các exception khác
    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, null, null, ex)
    }
} 