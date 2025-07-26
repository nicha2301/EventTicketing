package com.nicha.eventticketing.data.remote.dto.ticket

import com.google.gson.annotations.SerializedName

/**
 * DTO cho pending order
 */
data class PendingOrderDto(
    @SerializedName("orderId")
    val orderId: String,
    
    @SerializedName("eventId")
    val eventId: String,
    
    @SerializedName("eventTitle")
    val eventTitle: String,
    
    @SerializedName("tickets")
    val tickets: List<TicketDto>,
    
    @SerializedName("totalAmount")
    val totalAmount: Double,
    
    @SerializedName("paymentId")
    val paymentId: String?,
    
    @SerializedName("paymentStatus")
    val paymentStatus: String,
    
    @SerializedName("paymentUrl")
    val paymentUrl: String?,
    
    @SerializedName("purchaseDate")
    val purchaseDate: String,
    
    @SerializedName("buyerName")
    val buyerName: String,
    
    @SerializedName("buyerEmail")
    val buyerEmail: String,
    
    @SerializedName("buyerPhone")
    val buyerPhone: String,
    
    @SerializedName("discountAmount")
    val discountAmount: Double = 0.0,
    
    @SerializedName("promoCode")
    val promoCode: String?
)

/**
 * DTO cho API response của pending tickets
 */
data class PendingTicketsResponseDto(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<PendingOrderDto>
)
