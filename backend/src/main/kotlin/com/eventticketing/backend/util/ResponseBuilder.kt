package com.eventticketing.backend.util

import com.eventticketing.backend.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * Lớp tiện ích để xây dựng các response chuẩn hóa
 */
object ResponseBuilder {
    
    /**
     * Tạo response thành công
     */
    fun <T> success(data: T, message: String = "Thành công"): ResponseEntity<ApiResponse<T>> {
        val response = ApiResponse(
            success = true,
            message = message,
            data = data
        )
        return ResponseEntity.ok(response)
    }
    
    /**
     * Tạo response lỗi
     */
    fun <T> error(message: String, status: HttpStatus = HttpStatus.BAD_REQUEST, data: T? = null): ResponseEntity<ApiResponse<T>> {
        val response = ApiResponse(
            success = false,
            message = message,
            data = data
        )
        return ResponseEntity.status(status).body(response)
    }
    
    /**
     * Tạo response không tìm thấy
     */
    fun <T> notFound(message: String = "Không tìm thấy", data: T? = null): ResponseEntity<ApiResponse<T>> {
        return error(message, HttpStatus.NOT_FOUND, data)
    }
    
    /**
     * Tạo response lỗi xác thực
     */
    fun <T> unauthorized(message: String = "Không có quyền truy cập", data: T? = null): ResponseEntity<ApiResponse<T>> {
        return error(message, HttpStatus.UNAUTHORIZED, data)
    }
    
    /**
     * Tạo response lỗi cấm truy cập
     */
    fun <T> forbidden(message: String = "Bị cấm truy cập", data: T? = null): ResponseEntity<ApiResponse<T>> {
        return error(message, HttpStatus.FORBIDDEN, data)
    }
    
    /**
     * Tạo response lỗi máy chủ
     */
    fun <T> serverError(message: String = "Lỗi máy chủ", data: T? = null): ResponseEntity<ApiResponse<T>> {
        return error(message, HttpStatus.INTERNAL_SERVER_ERROR, data)
    }
    
    /**
     * Tạo response tạo thành công
     */
    fun <T> created(data: T, message: String = "Tạo thành công"): ResponseEntity<ApiResponse<T>> {
        val response = ApiResponse(
            success = true,
            message = message,
            data = data
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
} 