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
import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.data.remote.dto.category.CategoryResponse
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseResponseDto
import com.nicha.eventticketing.data.remote.dto.location.LocationDto
import com.nicha.eventticketing.data.remote.dto.rating.RatingDto
import com.nicha.eventticketing.data.remote.dto.notification.NotificationDto
import com.nicha.eventticketing.data.remote.dto.comment.CommentDto
import com.nicha.eventticketing.data.remote.dto.comment.CommentCreateDto
import com.nicha.eventticketing.data.remote.dto.organizer.OrganizerDto
import com.nicha.eventticketing.data.remote.dto.organizer.OrganizerCreateDto
import com.nicha.eventticketing.data.remote.dto.organizer.OrganizerUpdateDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentCreateDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentStatusUpdateDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentMethodDto
import com.nicha.eventticketing.data.remote.dto.promotion.PromotionDto
import com.nicha.eventticketing.data.remote.dto.promotion.PromotionCreateDto
import com.nicha.eventticketing.data.remote.dto.promotion.PromotionUpdateDto
import com.nicha.eventticketing.data.remote.dto.promotion.PromotionValidationDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentRequestDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentResponseDto
import com.nicha.eventticketing.data.remote.dto.event.EventImageDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypePageResponse
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
    
    @GET("api/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<ApiResponse<UserDto>>
    
    @GET("api/users")
    suspend fun getUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<UserDto>>>
    
    @PUT("api/users/me/password")
    suspend fun changePassword(@Body passwordChangeRequest: Map<String, String>): Response<ApiResponse<String>>
    
    @PUT("api/users/me/profile-image")
    suspend fun updateProfileImage(@Body imageUrlRequest: Map<String, String>): Response<ApiResponse<UserDto>>
    
    @DELETE("api/users/me")
    suspend fun deleteAccount(): Response<ApiResponse<String>>
    
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
    
    @GET("api/events/{eventId}/ticket-types/{ticketTypeId}")
    suspend fun getTicketTypeById(
        @Path("eventId") eventId: String,
        @Path("ticketTypeId") ticketTypeId: String
    ): Response<ApiResponse<TicketTypeDto>>
    
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
    
    @POST("api/tickets/{ticketId}/check-in")
    suspend fun checkInTicket(@Path("ticketId") ticketId: String): Response<ApiResponse<TicketDto>>
    
    @POST("api/tickets/check-in/{ticketNumber}")
    suspend fun checkInTicketByNumber(@Path("ticketNumber") ticketNumber: String): Response<ApiResponse<TicketDto>>
    
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
    
    @GET("api/locations/{id}")
    suspend fun getLocationById(@Path("id") locationId: String): Response<ApiResponse<LocationDto>>
    
    @GET("api/locations/search")
    suspend fun searchLocations(
        @Query("name") name: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<LocationDto>>>
    
    @GET("api/locations/city/{city}")
    suspend fun getLocationsByCity(
        @Path("city") city: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<LocationDto>>>
    
    @GET("api/locations/nearby")
    suspend fun getNearbyLocations(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Double = 10.0,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<LocationDto>>>
    
    @GET("api/locations/popular")
    suspend fun getPopularLocations(@Query("limit") limit: Int = 10): Response<ApiResponse<List<LocationDto>>>
    
    // Comments
    @POST("api/comments")
    suspend fun createComment(@Body comment: CommentCreateDto): Response<ApiResponse<CommentDto>>
    
    @PUT("api/comments/{commentId}")
    suspend fun updateComment(
        @Path("commentId") commentId: String,
        @Body comment: CommentCreateDto
    ): Response<ApiResponse<CommentDto>>
    
    @DELETE("api/comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") commentId: String): Response<ApiResponse<String>>
    
    @GET("api/comments/event/{eventId}")
    suspend fun getEventComments(
        @Path("eventId") eventId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<CommentDto>>>
    
    @GET("api/comments/user/{userId}")
    suspend fun getUserComments(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<CommentDto>>>
    
    // Ratings
    @POST("api/ratings")
    suspend fun createRating(@Body rating: RatingDto): Response<ApiResponse<RatingDto>>
    
    @PUT("api/ratings/{ratingId}")
    suspend fun updateRating(
        @Path("ratingId") ratingId: String,
        @Body rating: RatingDto
    ): Response<ApiResponse<RatingDto>>
    
    @DELETE("api/ratings/{ratingId}")
    suspend fun deleteRating(@Path("ratingId") ratingId: String): Response<ApiResponse<String>>
    
    @GET("api/ratings/event/{eventId}")
    suspend fun getEventRatings(
        @Path("eventId") eventId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<RatingDto>>>
    
    @GET("api/ratings/user/{userId}")
    suspend fun getUserRatings(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<RatingDto>>>
    
    @GET("api/ratings/event/{eventId}/summary")
    suspend fun getEventRatingSummary(@Path("eventId") eventId: String): Response<ApiResponse<Map<String, Any>>>
    
    // Notifications
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<NotificationDto>>>
    
    @PUT("api/notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: String): Response<ApiResponse<NotificationDto>>
    
    @PUT("api/notifications/preferences")
    suspend fun updateNotificationPreferences(
        @Body preferences: Map<String, Any>
    ): Response<ApiResponse<Map<String, Any>>>
    
    // Organizers
    @GET("api/organizers")
    suspend fun getOrganizers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<OrganizerDto>>>
    
    @GET("api/organizers/{organizerId}")
    suspend fun getOrganizerById(@Path("organizerId") organizerId: String): Response<ApiResponse<OrganizerDto>>
    
    @GET("api/organizers/user/{userId}")
    suspend fun getOrganizerByUserId(@Path("userId") userId: String): Response<ApiResponse<OrganizerDto>>
    
    @POST("api/organizers")
    suspend fun createOrganizer(@Body organizer: OrganizerCreateDto): Response<ApiResponse<OrganizerDto>>
    
    @PUT("api/organizers/{organizerId}")
    suspend fun updateOrganizer(
        @Path("organizerId") organizerId: String,
        @Body organizer: OrganizerUpdateDto
    ): Response<ApiResponse<OrganizerDto>>
    
    @GET("api/events/organizer/{organizerId}")
    suspend fun getOrganizerEvents(
        @Path("organizerId") organizerId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<EventDto>>>
    
    // Payments
    @GET("api/payments")
    suspend fun getPayments(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<PaymentDto>>>
    
    @GET("api/payments/{paymentId}")
    suspend fun getPaymentById(@Path("paymentId") paymentId: String): Response<ApiResponse<PaymentDto>>
    
    @POST("api/payments/create")
    suspend fun createPayment(@Body payment: PaymentRequestDto): Response<ApiResponse<PaymentResponseDto>>
    
    @PUT("api/payments/{paymentId}/status")
    suspend fun updatePaymentStatus(
        @Path("paymentId") paymentId: String,
        @Body statusUpdate: PaymentStatusUpdateDto
    ): Response<ApiResponse<PaymentDto>>
    
    @GET("api/payments/methods")
    suspend fun getPaymentMethods(): Response<ApiResponse<List<PaymentMethodDto>>>
    
    @GET("api/users/me/payments")
    suspend fun getCurrentUserPayments(): Response<ApiResponse<List<PaymentResponseDto>>>
    
    @POST("api/payments/{id}/refund")
    suspend fun refundPayment(
        @Path("id") paymentId: String,
        @Body refundRequest: Map<String, Any>
    ): Response<ApiResponse<PaymentResponseDto>>
    
    // Promotions
    @GET("api/promotions")
    suspend fun getPromotions(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<PromotionDto>>>
    
    @GET("api/promotions/{promotionId}")
    suspend fun getPromotionById(@Path("promotionId") promotionId: String): Response<ApiResponse<PromotionDto>>
    
    @GET("api/promotions/code/{code}")
    suspend fun getPromotionByCode(@Path("code") code: String): Response<ApiResponse<PromotionDto>>
    
    @POST("api/promotions")
    suspend fun createPromotion(@Body promotion: PromotionCreateDto): Response<ApiResponse<PromotionDto>>
    
    @PUT("api/promotions/{promotionId}")
    suspend fun updatePromotion(
        @Path("promotionId") promotionId: String,
        @Body promotion: PromotionUpdateDto
    ): Response<ApiResponse<PromotionDto>>
    
    @DELETE("api/promotions/{promotionId}")
    suspend fun deletePromotion(@Path("promotionId") promotionId: String): Response<ApiResponse<String>>
    
    @POST("api/promotions/validate")
    suspend fun validatePromotion(@Body validation: PromotionValidationDto): Response<ApiResponse<Map<String, Any>>>
    
    @GET("api/promotions/event/{eventId}")
    suspend fun getEventPromotions(
        @Path("eventId") eventId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageDto<PromotionDto>>>
} 