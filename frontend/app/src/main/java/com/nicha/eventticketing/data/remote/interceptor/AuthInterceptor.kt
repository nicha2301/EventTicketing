package com.nicha.eventticketing.data.remote.interceptor

import com.nicha.eventticketing.config.AppConfig
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import timber.log.Timber

/**
 * Interceptor để xử lý xác thực cho các yêu cầu HTTP.
 * - Thêm token vào header nếu có
 * - Xử lý token hết hạn và tự động refresh
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        
    private val refreshTokenJsonAdapter by lazy {
        moshi.adapter(RefreshTokenResponse::class.java)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Không thêm token cho các request liên quan đến xác thực
        if (isAuthenticationRequest(originalRequest)) {
            Timber.d("Bỏ qua xác thực cho request: ${originalRequest.url.encodedPath}")
            return chain.proceed(originalRequest)
        }
        
        // Thêm token vào request
        val requestWithToken = addTokenToRequest(originalRequest)
        var response = chain.proceed(requestWithToken)
        
        // Xử lý token hết hạn
        if (response.isUnauthorized() && !isRefreshTokenRequest(originalRequest)) {
            Timber.d("Token hết hạn, đang thử refresh token")
            response.close() // Đóng response trước khi tạo mới
            
            // Thử refresh token
            val refreshedToken = refreshToken(chain)
            if (refreshedToken != null) {
                Timber.d("Refresh token thành công, thử lại request ban đầu")
                // Thử lại request ban đầu với token mới
                val newRequestWithToken = originalRequest.newBuilder()
                    .header(AppConfig.Auth.TOKEN_HEADER, "${AppConfig.Auth.TOKEN_PREFIX}$refreshedToken")
                    .build()
                
                return chain.proceed(newRequestWithToken)
            } else {
                Timber.d("Refresh token thất bại, đăng xuất người dùng")
                // Xóa token và trả về lỗi 401
                runBlocking { preferencesManager.clearAuthToken() }
                return createUnauthorizedResponse(originalRequest, response.protocol)
            }
        }
        
        return response
    }
    
    /**
     * Kiểm tra xem request có phải là yêu cầu xác thực không (đăng nhập, đăng ký, v.v.)
     */
    private fun isAuthenticationRequest(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.contains("login") || path.contains("register") || path.contains("google")
    }
    
    /**
     * Kiểm tra xem request có phải là yêu cầu refresh token không
     */
    private fun isRefreshTokenRequest(request: Request): Boolean {
        return request.url.encodedPath.contains(AppConfig.Auth.REFRESH_TOKEN_PATH)
    }
    
    /**
     * Kiểm tra xem response có mã lỗi Unauthorized (401) không
     */
    private fun Response.isUnauthorized(): Boolean {
        return this.code == AppConfig.Auth.UNAUTHORIZED_CODE
    }
    
    /**
     * Tạo response unauthorized để trả về khi token không hợp lệ
     */
    private fun createUnauthorizedResponse(request: Request, protocol: okhttp3.Protocol): Response {
        return Response.Builder()
            .request(request)
            .protocol(protocol)
            .code(AppConfig.Auth.UNAUTHORIZED_CODE)
            .message("Token hết hạn và không thể refresh")
            .build()
    }
    
    /**
     * Thêm token vào header của request nếu có
     */
    private fun addTokenToRequest(request: Request): Request {
        val token = runBlocking {
            try {
                preferencesManager.getAuthToken().firstOrNull()
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi đọc token trong addTokenToRequest")
                null
            }
        }
        
        return if (!token.isNullOrEmpty()) {
            Timber.d("Thêm token vào request: ${request.url.encodedPath}")
            request.newBuilder()
                .header(AppConfig.Auth.TOKEN_HEADER, "${AppConfig.Auth.TOKEN_PREFIX}$token")
                .build()
        } else {
            Timber.d("Không có token cho request: ${request.url.encodedPath}")
            request
        }
    }
    
    /**
     * Thực hiện refresh token
     * @return Token mới nếu thành công, null nếu thất bại
     */
    private fun refreshToken(chain: Interceptor.Chain): String? {
        try {
            // Tạo URL cho refresh token endpoint
            val baseUrl = chain.request().url.newBuilder()
                .encodedPath("/${AppConfig.Auth.REFRESH_TOKEN_PATH}")
                .build()
            
            Timber.d("Gọi refresh token URL: $baseUrl")
            
            // Tạo refresh token request
            val refreshRequest = Request.Builder()
                .url(baseUrl)
                .get()
                .build()
            
            // Tạo một OkHttpClient mới với timeout ngắn hơn
            val refreshClient = createRefreshTokenClient()
                
            // Thực hiện request refresh token
            val response = refreshClient.newCall(refreshRequest).execute()
            
            return try {
                if (response.isSuccessful) {
                    processRefreshTokenResponse(response.body?.string())
                } else {
                    Timber.e("Refresh token thất bại với mã lỗi: ${response.code}")
                    null
                }
            } finally {
                response.close() // Đảm bảo response luôn được đóng
            }
        } catch (e: IOException) {
            Timber.e(e, "Lỗi IO khi refresh token")
            runBlocking { preferencesManager.clearAuthToken() }
            return null
        } catch (e: Exception) {
            Timber.e(e, "Lỗi không xác định khi refresh token")
            runBlocking { preferencesManager.clearAuthToken() }
            return null
        }
    }
    
    /**
     * Tạo client riêng cho request refresh token với timeout ngắn
     */
    private fun createRefreshTokenClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Xử lý response từ refresh token endpoint
     */
    private fun processRefreshTokenResponse(responseBody: String?): String? {
        if (responseBody == null) {
            Timber.e("Refresh token response body là null")
            return null
        }
        
        val newToken = parseTokenFromResponse(responseBody)
        
        return if (newToken != null) {
            val saveResult = runBlocking { 
                try {
                    preferencesManager.saveAuthToken(newToken)
                } catch (e: Exception) {
                    Timber.e(e, "Lỗi khi lưu token mới")
                    false
                }
            }
            
            if (saveResult) {
                Timber.d("Lưu token mới thành công")
                newToken
            } else {
                Timber.e("Không thể lưu token mới")
                null
            }
        } else {
            Timber.e("Không thể parse token từ response")
            null
        }
    }
    
    /**
     * Parse token từ JSON response
     */
    private fun parseTokenFromResponse(responseBody: String): String? {
        return try {
            // Sử dụng Moshi để parse JSON
            val response = refreshTokenJsonAdapter.fromJson(responseBody)
            response?.data?.token
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi parse token từ JSON")
            // Fallback sang regex nếu parse JSON thất bại
            val tokenPattern = "\"token\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val matchResult = tokenPattern.find(responseBody)
            matchResult?.groupValues?.get(1)
        }
    }
    
    // Class để parse response JSON
    private data class RefreshTokenResponse(
        val success: Boolean,
        val message: String?,
        val data: TokenData?
    )
    
    private data class TokenData(
        val token: String,
        val id: String?,
        val email: String?,
        val fullName: String?,
        val role: String?
    )
} 