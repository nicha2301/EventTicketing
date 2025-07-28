package com.nicha.eventticketing.data.remote.interceptor

import com.nicha.eventticketing.config.AppConfig
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import timber.log.Timber

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

        if (isAuthenticationRequest(originalRequest)) {
            return chain.proceed(originalRequest)
        }

        val requestWithToken = addTokenToRequest(originalRequest)
        val response = chain.proceed(requestWithToken)

        if (response.isUnauthorized() && !isRefreshTokenRequest(originalRequest)) {
            response.close()

            synchronized(this) {
                val newAccessToken = refreshToken()

                return if (newAccessToken != null) {
                    val newRequestWithToken = originalRequest.newBuilder()
                        .header(AppConfig.Auth.TOKEN_HEADER, "${AppConfig.Auth.TOKEN_PREFIX}$newAccessToken")
                        .build()
                    chain.proceed(newRequestWithToken)
                } else {
                    runBlocking { preferencesManager.clearAuthToken() }
                    createUnauthorizedResponse(originalRequest, response.protocol)
                }
            }
        }

        return response
    }

    private fun isAuthenticationRequest(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.contains("login") || path.contains("register") || path.contains("google")
    }

    private fun isRefreshTokenRequest(request: Request): Boolean {
        return request.url.encodedPath.contains(AppConfig.Auth.REFRESH_TOKEN_PATH)
    }

    private fun Response.isUnauthorized(): Boolean {
        return this.code == AppConfig.Auth.UNAUTHORIZED_CODE
    }

    private fun createUnauthorizedResponse(request: Request, protocol: okhttp3.Protocol): Response {
        return Response.Builder()
            .request(request)
            .protocol(protocol)
            .code(AppConfig.Auth.UNAUTHORIZED_CODE)
            .message("Unauthorized")
            .body("{}".toResponseBody(null))
            .build()
    }

    private fun addTokenToRequest(request: Request): Request {
        val token = runBlocking { preferencesManager.getAuthToken().firstOrNull() }
        return if (token != null) {
            request.newBuilder()
                .header(AppConfig.Auth.TOKEN_HEADER, "${AppConfig.Auth.TOKEN_PREFIX}$token")
                .build()
        } else {
            request
        }
    }

    private fun refreshToken(): String? {
        try {
            val currentToken = preferencesManager.getAuthTokenSync() ?: return null

            val refreshUrl = AppConfig.Api.API_BASE_URL + AppConfig.Auth.REFRESH_TOKEN_PATH

            val refreshRequest = Request.Builder()
                .url(refreshUrl)
                .post(okhttp3.RequestBody.create(null, "")) // Gửi body rỗng nếu cần
                .header("Authorization", "Bearer $currentToken")
                .build()

            val refreshClient = createRefreshTokenClient()
            val response = refreshClient.newCall(refreshRequest).execute()

            try {
                if (response.isSuccessful) {
                    return processRefreshTokenResponse(response.body?.string())
                } else {
                    return null
                }
            } finally {
                response.close()
            }
        } catch (e: Exception) {
            return null
        }
    }

    private fun createRefreshTokenClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private fun processRefreshTokenResponse(responseBody: String?): String? {
        if (responseBody == null) return null
        val newAccessToken = parseTokenFromResponse(responseBody)
        return if (newAccessToken != null) {
            runBlocking {
                preferencesManager.saveAuthToken(newAccessToken)
            }
            newAccessToken
        } else {
            null
        }
    }

    private fun parseTokenFromResponse(responseBody: String): String? {
        try {
            val parsedResponse = refreshTokenJsonAdapter.fromJson(responseBody)
            if (parsedResponse?.data?.token != null) {
                return parsedResponse.data.token
            }
        } catch (_: Exception) {
        }

        try {
            val tokenPattern = """"token"\s*:\s*"([^"]+)"""".toRegex()
            val matchResult = tokenPattern.find(responseBody)
            if (matchResult != null) {
                return matchResult.groupValues[1]
            }
        } catch (_: Exception) {
        }

        return null
    }

    private data class RefreshTokenResponse(
        val success: Boolean,
        val message: String?,
        val data: TokenData?
    )

    private data class TokenData(
        val token: String
    )
}