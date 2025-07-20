package com.nicha.eventticketing.util

import com.google.gson.Gson
import okhttp3.ResponseBody
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
} 