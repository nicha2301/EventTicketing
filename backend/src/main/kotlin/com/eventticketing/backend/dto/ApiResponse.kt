package com.eventticketing.backend.dto

/**
 * Lớp generic đại diện cho định dạng response chuẩn của API
 */
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null
) {
    companion object {
        /**
         * Tạo response thành công với message và data
         */
        fun <T> success(message: String, data: T): ApiResponse<T> {
            return ApiResponse(true, message, data)
        }

        /**
         * Tạo response thành công chỉ với data
         */
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(true, null, data)
        }

        /**
         * Tạo response lỗi với message
         */
        fun <T> error(message: String): ApiResponse<T> {
            return ApiResponse(false, message, null)
        }

        /**
         * Tạo response lỗi với message và data
         */
        fun <T> error(message: String, data: T): ApiResponse<T> {
            return ApiResponse(false, message, data)
        }
    }
}
