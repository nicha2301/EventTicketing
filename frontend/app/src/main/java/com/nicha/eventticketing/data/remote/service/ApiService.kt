package com.nicha.eventticketing.data.remote.service

import com.nicha.eventticketing.data.remote.dto.ApiResponse
import com.nicha.eventticketing.data.remote.dto.auth.GoogleAuthRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.LoginRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.LogoutRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.ResetPasswordRequestDto
import com.nicha.eventticketing.data.remote.dto.auth.UserAuthResponseDto
import com.nicha.eventticketing.data.remote.dto.auth.UserCreateDto
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.data.remote.dto.auth.UserUpdateDto
import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.data.remote.dto.category.CategoryResponse
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.location.LocationDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationPreferencesDto
import com.nicha.eventticketing.data.remote.dto.notification.DeviceTokenDto
import com.nicha.eventticketing.data.remote.dto.notification.DeviceTokenRequestDto
import com.nicha.eventticketing.data.remote.dto.notification.TopicSubscriptionDto
import com.nicha.eventticketing.data.remote.dto.notification.UnreadCountDto
import com.nicha.eventticketing.data.remote.dto.notification.MarkAllReadResultDto
import com.nicha.eventticketing.data.remote.dto.notification.DeleteAllResultDto
import com.nicha.eventticketing.data.remote.dto.organizer.OrganizerDto
import com.nicha.eventticketing.data.remote.dto.organizer.OrganizerCreateDto
import com.nicha.eventticketing.data.remote.dto.organizer.OrganizerUpdateDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseResponseDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseRequestDto
import com.nicha.eventticketing.data.remote.dto.ticket.PendingTicketsResponseDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentRequestDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentResponseDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentMethodDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentStatusUpdateDto
import com.nicha.eventticketing.data.remote.dto.event.EventImageDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypePageResponse
import com.nicha.eventticketing.data.remote.dto.ticket.CheckInRequestDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Multipart
import retrofit2.http.Part

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
    
    @POST("api/auth/password/forgot")
    suspend fun forgotPassword(@Query("email") email: String): Response<ApiResponse<String>>
    
    @POST("api/auth/password/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): Response<ApiResponse<String>>
    
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

    @POST("api/events")
    suspend fun createEvent(@Body event: EventDto): Response<ApiResponse<EventDto>>
    
    @PUT("api/events/{id}")
    suspend fun updateEvent(@Path("id") eventId: String, @Body event: EventDto): Response<ApiResponse<EventDto>>
    
    @DELETE("api/events/{id}")
    suspend fun deleteEvent(@Path("id") eventId: String): Response<ApiResponse<Boolean>>
    
    @PUT("api/events/{id}/publish")
    suspend fun publishEvent(@Path("id") eventId: String): Response<ApiResponse<EventDto>>
    
    @PUT("api/events/{id}/cancel")
    suspend fun cancelEvent(@Path("id") eventId: String, @Body reason: Map<String, String>): Response<ApiResponse<EventDto>>
    
    // Event Images
    @GET("api/events/{eventId}/images")
    suspend fun getEventImages(@Path("eventId") eventId: String): Response<ApiResponse<List<EventImageDto>>>
    
    @Multipart
    @POST("api/events/{id}/images")
    suspend fun uploadEventImage(
        @Path("id") eventId: String,
        @Part image: MultipartBody.Part,
        @Query("isPrimary") isPrimary: Boolean = false
    ): Response<ApiResponse<EventImageDto>>
    
    @PUT("api/events/{eventId}/images/{imageId}/primary")
    suspend fun setImageAsPrimary(
        @Path("eventId") eventId: String,
        @Path("imageId") imageId: String
    ): Response<ApiResponse<EventImageDto>>
    
    @DELETE("api/events/{eventId}/images/{imageId}")
    suspend fun deleteEventImage(
        @Path("eventId") eventId: String,
        @Path("imageId") imageId: String
    ): Response<ApiResponse<Boolean>>
    
    // Ticket Types
    @GET("api/events/{eventId}/ticket-types")
    suspend fun getTicketTypes(
        @Path("eventId") eventId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<TicketTypePageResponse>>
    
    @POST("api/events/{eventId}/ticket-types")
    suspend fun createTicketType(
        @Path("eventId") eventId: String,
        @Body ticketType: TicketTypeDto
    ): Response<ApiResponse<TicketTypeDto>>
    
    @PUT("api/ticket-types/{ticketTypeId}")
    suspend fun updateTicketType(
        @Path("ticketTypeId") ticketTypeId: String,
        @Body ticketType: TicketTypeDto
    ): Response<ApiResponse<TicketTypeDto>>
    
    @DELETE("api/ticket-types/{ticketTypeId}")
    suspend fun deleteTicketType(
        @Path("ticketTypeId") ticketTypeId: String
    ): Response<ApiResponse<Boolean>>
    
    // Tickets
    @POST("api/tickets/purchase")
    suspend fun purchaseTickets(@Body purchaseDto: TicketPurchaseDto): Response<ApiResponse<TicketPurchaseResponseDto>>
    
    @GET("api/tickets/{ticketId}")
    suspend fun getTicketById(@Path("ticketId") ticketId: String): Response<ApiResponse<TicketDto>>
    
    @GET("api/tickets/number/{ticketNumber}")
    suspend fun getTicketByNumber(@Path("ticketNumber") ticketNumber: String): Response<ApiResponse<TicketDto>>
    
    @GET("api/tickets/my-tickets")
    suspend fun getMyTickets(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PageDto<TicketDto>>>
    
    @GET("api/tickets/my-pending-tickets")
    suspend fun getMyPendingTickets(): Response<PendingTicketsResponseDto>
    
    @POST("api/tickets/check-in")
    suspend fun checkInTicket(@Body request: CheckInRequestDto): Response<ApiResponse<TicketDto>>
    
    @POST("api/tickets/{ticketId}/cancel")
    suspend fun cancelTicket(@Path("ticketId") ticketId: String): Response<ApiResponse<TicketDto>>
    
    // Categories
    @GET("api/categories")
    suspend fun getCategories(
        @Query("includeInactive") includeInactive: Boolean = false,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<ApiResponse<CategoryResponse>>
    
    @GET("api/categories/{id}")
    suspend fun getCategoryById(@Path("id") categoryId: String): Response<ApiResponse<CategoryDto>>
    
    // Locations
    @GET("api/locations")
    suspend fun getLocations(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<LocationDto>>>
    
    // Notifications
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<PageDto<NotificationDto>>
    
    @GET("api/notifications/unread")
    suspend fun getUnreadNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<PageDto<NotificationDto>>
    
    @GET("api/notifications/type/{type}")
    suspend fun getNotificationsByType(
        @Path("type") type: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<PageDto<NotificationDto>>
    
    @GET("api/notifications/count")
    suspend fun getUnreadNotificationCount(): Response<UnreadCountDto>
    
    @PUT("api/notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(@Path("notificationId") id: String): Response<NotificationDto>
    
    @PUT("api/notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<MarkAllReadResultDto>
    
    @DELETE("api/notifications/{notificationId}")
    suspend fun deleteNotification(@Path("notificationId") id: String): Response<Void>
    
    @DELETE("api/notifications")
    suspend fun deleteAllNotifications(): Response<DeleteAllResultDto>
    
    @GET("api/notifications/preferences")
    suspend fun getNotificationPreferences(): Response<NotificationPreferencesDto>
    
    @PUT("api/notifications/preferences")
    suspend fun updateNotificationPreferences(
        @Body preferences: NotificationPreferencesDto
    ): Response<NotificationPreferencesDto>
    
    // Device Tokens
    @POST("api/devices/tokens")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequestDto): Response<ApiResponse<DeviceTokenDto>>
    
    @GET("api/devices/tokens")
    suspend fun getDeviceTokens(): Response<ApiResponse<List<DeviceTokenDto>>>
    
    @DELETE("api/devices/tokens/{tokenId}")
    suspend fun deleteDeviceToken(@Path("tokenId") tokenId: String): Response<ApiResponse<Boolean>>
    
    @DELETE("api/devices/tokens")
    suspend fun deleteAllDeviceTokens(): Response<ApiResponse<Int>>
    
    @POST("api/devices/topic/subscribe")
    suspend fun subscribeToTopic(@Body request: TopicSubscriptionDto): Response<ApiResponse<Boolean>>
    
    @POST("api/devices/topic/unsubscribe")
    suspend fun unsubscribeFromTopic(@Body request: TopicSubscriptionDto): Response<ApiResponse<Boolean>>
    
    // Organizers
    @GET("api/events/organizer/{organizerId}")
    suspend fun getOrganizerEvents(
        @Path("organizerId") organizerId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<EventDto>>>
    
    // Tickets
    @POST("api/tickets/purchase")
    suspend fun purchaseTickets(@Body request: TicketPurchaseRequestDto): Response<ApiResponse<TicketPurchaseResponseDto>>
    
    // Payments  
    @POST("api/payments/create")
    suspend fun createPayment(@Body payment: PaymentRequestDto): Response<PaymentResponseDto>
    
    @GET("api/payments/momo-return")
    suspend fun processMomoReturn(
        @Query("partnerCode") partnerCode: String,
        @Query("orderId") orderId: String,
        @Query("requestId") requestId: String,
        @Query("amount") amount: String,
        @Query("orderInfo") orderInfo: String,
        @Query("orderType") orderType: String,
        @Query("transId") transId: String,
        @Query("resultCode") resultCode: String,
        @Query("message") message: String,
        @Query("payType") payType: String,
        @Query("responseTime") responseTime: String,
        @Query("extraData") extraData: String,
        @Query("signature") signature: String
    ): Response<ApiResponse<PaymentResponseDto>>
    
    @GET("api/users/me/payments")
    suspend fun getUserPayments(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("status") status: String? = null,
        @Query("paymentMethod") paymentMethod: String? = null
    ): Response<List<PaymentResponseDto>>  // Trả về List trực tiếp thay vì PageDto
    
    @POST("api/payments/{id}/refund")
    suspend fun refundPayment(
        @Path("id") paymentId: String,
        @Body refundRequest: Map<String, Any>
    ): Response<ApiResponse<PaymentResponseDto>>
} 