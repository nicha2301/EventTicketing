package com.eventticketing.backend.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

/**
 * Lớp đại diện cho response API chuẩn
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val code: Int? = null,
    val errors: Map<String, String>? = null
) {
    companion object {
        private const val STATUS_SUCCESS = "success"
        private const val STATUS_ERROR = "error"
        private const val STATUS_WARNING = "warning"
        private const val STATUS_INFO = "info"
        
        /**
         * Tạo response thành công
         */
        fun <T> success(message: String, data: T? = null): ApiResponse<T> {
            return ApiResponse(
                status = STATUS_SUCCESS,
                message = message,
                data = data
            )
        }

        /**
         * Tạo response thành công không có message
         */
        fun <T> of(data: T): ApiResponse<T> {
            return success("Thành công", data)
        }

        /**
         * Tạo response lỗi với data tùy chỉnh
         */
        fun <T> error(message: String, data: T? = null, code: Int? = null): ApiResponse<T> {
            return ApiResponse(
                status = STATUS_ERROR,
                message = message,
                data = data,
                code = code
            )
        }
        
        /**
         * Tạo response lỗi với danh sách lỗi
         */
        fun errorWithErrors(message: String, errors: Map<String, String>? = null, code: Int? = null): ApiResponse<Nothing> {
            return ApiResponse(
                status = STATUS_ERROR,
                message = message,
                errors = errors,
                code = code
            )
        }
        
        /**
         * Tạo response lỗi từ HttpStatus
         */
        fun errorFromStatus(httpStatus: HttpStatus, message: String? = null): ApiResponse<Nothing> {
            return error(
                message = message ?: httpStatus.reasonPhrase,
                code = httpStatus.value()
            )
        }
        
        /**
         * Tạo response cảnh báo
         */
        fun warning(message: String, data: Any? = null): ApiResponse<Any> {
            return ApiResponse(
                status = STATUS_WARNING,
                message = message,
                data = data
            )
        }
        
        /**
         * Tạo response thông tin
         */
        fun info(message: String, data: Any? = null): ApiResponse<Any> {
            return ApiResponse(
                status = STATUS_INFO,
                message = message,
                data = data
            )
        }
    }
    
    /**
     * Chuyển đổi sang String để hiển thị
     */
    override fun toString(): String {
        return "ApiResponse(status=$status, message='$message', code=$code, hasData=${data != null})"
    }
} 