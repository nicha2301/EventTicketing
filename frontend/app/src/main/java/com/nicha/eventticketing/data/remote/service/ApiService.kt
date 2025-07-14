package com.nicha.eventticketing.data.remote.service

import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.auth.ForgotPasswordRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.GoogleAuthRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.LoginRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.LogoutRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.ResetPasswordRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.UserAuthResponseDto
import com.nicha.eventticketing.data.remote.dto.auth.UserCreateDto
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.data.remote.dto.auth.UserUpdateDto
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface cho các API endpoints
 */
interface ApiService {
    
    // Authentication
    @POST("api/auth/register")
    suspend fun register(@Body user: UserCreateDto): Response<ApiResponse<UserDto>>
    
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequestDto): Response<ApiResponse<UserAuthResponseDto>>
    
    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body googleAuthRequest: GoogleAuthRequestDto): Response<ApiResponse<UserAuthResponseDto>>
    
    @GET("api/auth/activate")
    suspend fun activateAccount(@Query("token") token: String): Response<ApiResponse<String>>
    
    @POST("api/auth/password/forgot")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): Response<ApiResponse<String>>
    
    @POST("api/auth/password/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): Response<ApiResponse<String>>
    
    @GET("api/auth/refresh-token")
    suspend fun refreshToken(): Response<ApiResponse<UserAuthResponseDto>>
    
    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequestDto): Response<ApiResponse<String>>
    
    // User
    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>
    
    @PUT("api/users/me")
    suspend fun updateUser(@Body user: UserUpdateDto): Response<ApiResponse<UserDto>>
    
    // Events
    @GET("api/events")
    suspend fun getEvents(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PageDto<EventDto>>>
    
    @GET("api/events/{id}")
    suspend fun getEventById(@Path("id") eventId: String): Response<ApiResponse<EventDto>>
    
    @GET("api/events/search")
    suspend fun searchEvents(
        @Query("keyword") keyword: String? = null,
        @Query("categoryId") categoryId: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("locationId") locationId: String? = null,
        @Query("radius") radius: Double? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PageDto<EventDto>>>
    
    @GET("api/events/featured")
    suspend fun getFeaturedEvents(@Query("limit") limit: Int = 10): Response<ApiResponse<List<EventDto>>>
    
    @GET("api/events/upcoming")
    suspend fun getUpcomingEvents(@Query("limit") limit: Int = 10): Response<ApiResponse<List<EventDto>>>
    
    @GET("api/events/nearby")
    suspend fun getNearbyEvents(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Double = 10.0,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PageDto<EventDto>>>
} 