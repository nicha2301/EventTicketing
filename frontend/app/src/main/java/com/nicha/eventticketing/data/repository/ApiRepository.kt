package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.config.AppConfig
import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.auth.*
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.UUID
import com.nicha.eventticketing.data.remote.dto.category.CategoryResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber

/**
 * Repository for interacting with the API
 */
class ApiRepository {
    private val apiService: EventTicketingApi by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            
        val retrofit = Retrofit.Builder()
            .baseUrl(AppConfig.Api.API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        retrofit.create(EventTicketingApi::class.java)
    }

    // Auth
    suspend fun login(email: String, password: String): Result<UserAuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequestDto(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(user: UserCreateDto): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(user)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Events
    suspend fun getEvents(page: Int, size: Int): Result<List<EventDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEvents(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.content ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch events"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventById(eventId: String): Result<EventDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEventById(eventId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch event"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchEvents(
        keyword: String? = null,
        categoryId: String? = null,
        page: Int = 0,
        size: Int = 10
    ): Result<List<EventDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchEvents(keyword, categoryId, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.content ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to search events"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Tickets
    suspend fun getMyTickets(status: String? = null, page: Int = 0, size: Int = 10): Result<List<TicketDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyTickets(status, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.content ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch tickets"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTicketById(ticketId: String): Result<TicketDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTicketById(ticketId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch ticket"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun purchaseTickets(purchaseDto: TicketPurchaseDto): Result<TicketPurchaseResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.purchaseTickets(purchaseDto)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to purchase tickets"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Categories
    suspend fun getCategories(): Result<List<CategoryDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCategories()
            if (response.isSuccessful && response.body()?.success == true) {
                val categoryResponse = response.body()?.data
                if (categoryResponse != null) {
                    val categories = categoryResponse.content ?: emptyList()
                    return@withContext Result.success(categories)
                }
                return@withContext Result.success(emptyList())
            } else {
                return@withContext Result.failure(Exception(response.body()?.message ?: "Failed to fetch categories"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    // User
    suspend fun getCurrentUser(): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Retrofit API interface
 */
interface EventTicketingApi {
    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequestDto): Response<ApiResponse<UserAuthResponseDto>>
    
    @POST("api/auth/register")
    suspend fun register(@Body user: UserCreateDto): Response<ApiResponse<UserDto>>
    
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
        @Query("keyword") keyword: String?,
        @Query("categoryId") categoryId: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PageDto<EventDto>>>
    
    // Tickets
    @GET("api/tickets/my-tickets")
    suspend fun getMyTickets(
        @Query("status") status: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PageDto<TicketDto>>>
    
    @GET("api/tickets/{ticketId}")
    suspend fun getTicketById(@Path("ticketId") ticketId: String): Response<ApiResponse<TicketDto>>
    
    @POST("api/tickets/purchase")
    suspend fun purchaseTickets(@Body purchaseDto: TicketPurchaseDto): Response<ApiResponse<TicketPurchaseResponseDto>>
    
    // Categories
    @GET("api/categories")
    suspend fun getCategories(): Response<ApiResponse<CategoryResponse>>
    
    // User
    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>
} 