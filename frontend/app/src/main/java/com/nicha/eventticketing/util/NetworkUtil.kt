package com.nicha.eventticketing.util

import com.google.gson.Gson
import com.nicha.eventticketing.data.remote.dto.ApiResponse
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber

/**
 * Tiện ích xử lý các yêu cầu mạng
 */
object NetworkUtil {
    
    /**
     * Parse response body từ lỗi
     *
     * @param T Type của dữ liệu trả về
     * @param errorBody Response body chứa thông tin lỗi
     * @return T? Object đã parse hoặc null nếu không thể parse
     */
    inline fun <reified T> parseErrorResponse(errorBody: ResponseBody?): T? {
        if (errorBody == null) return null
        
        return try {
            val errorString = errorBody.string()
            Timber.d("Error Body: $errorString")
            Gson().fromJson(errorString, T::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi parse error response")
            null
        }
    }
    
    /**
     * Parse lỗi từ response API
     *
     * @param response Response từ API
     * @return String? Thông báo lỗi hoặc null
     */
    fun <T> parseErrorResponse(response: Response<ApiResponse<T>>): String? {
        if (!response.isSuccessful) {
            try {
                val errorBodyString = response.errorBody()?.string() ?: return response.message()
                return try {
                    val errorResponse = Gson().fromJson(errorBodyString, ApiResponse::class.java)
                    errorResponse?.message ?: response.message()
                } catch (e: Exception) {
                    Timber.e(e, "Lỗi khi parse error response")
                    errorBodyString
                }
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi đọc error body")
                return response.message()
            }
        }
        
        // Nếu response thành công nhưng success = false
        val apiResponse = response.body()
        if (apiResponse?.success == false) {
            return apiResponse.message ?: "Lỗi không xác định"
        }
        
        return null
    }
} 