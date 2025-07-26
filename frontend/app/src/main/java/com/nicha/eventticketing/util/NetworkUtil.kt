package com.nicha.eventticketing.util

import com.google.gson.Gson
import com.nicha.eventticketing.data.remote.dto.ApiResponse
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.util.concurrent.TimeUnit
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.Nullable
import com.nicha.eventticketing.EventTicketingApp
import org.json.JSONObject
import android.content.Context

/**
 * Tiện ích xử lý các yêu cầu mạng
 */
object NetworkUtil {
    
    /**
     * Kiểm tra kết nối mạng
     */
    fun isNetworkAvailable(): Boolean {
        return try {
            val context = EventTicketingApp.instance.applicationContext
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo?.isConnected == true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking network availability")
            false
        }
    }
    
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
     * Parse lỗi từ response API với ApiResponse wrapper
     *
     * @param response Response từ API
     * @return String? Thông báo lỗi hoặc null
     */
    fun <T> parseApiErrorResponse(response: Response<ApiResponse<T>>): String? {
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
    
    /**
     * Parse lỗi từ response API không có ApiResponse wrapper
     *
     * @param response Response từ API
     * @return String? Thông báo lỗi hoặc null
     */
    fun parseErrorResponse(response: Response<*>): String? {
        if (!response.isSuccessful) {
            try {
                val errorBodyString = response.errorBody()?.string() ?: return response.message()
                return try {
                    val errorResponse = Gson().fromJson(errorBodyString, ApiResponse::class.java)
                    if (errorResponse?.message != null) {
                        return errorResponse.message
                    }
                    errorBodyString
                } catch (e: Exception) {
                    errorBodyString
                }
            } catch (e: Exception) {
                return response.message()
            }
        }
        return null
    }

    /**
     * Kiểm tra kết nối mạng trực tiếp thông qua ConnectivityManager
     * @return true nếu có kết nối thực tế, false nếu không
     */
    fun isActuallyConnected(): Boolean {
        return try {
            val context = EventTicketingApp.instance.applicationContext
            
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
                
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                return hasInternet
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                val isConnected = networkInfo != null && networkInfo.isConnected
                return isConnected
            }
        } catch (e: Exception) {
            false
        }
    }
} 