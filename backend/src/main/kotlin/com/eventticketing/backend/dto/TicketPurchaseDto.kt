package com.eventticketing.backend.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.*

data class TicketPurchaseDto(
    @field:NotNull(message = "ID sự kiện không được để trống")
    val eventId: UUID,
    
    @field:NotEmpty(message = "Danh sách vé không được để trống")
    @field:Valid
    val tickets: List<TicketItemDto>,
    
    @field:NotBlank(message = "Tên người mua không được để trống")
    @field:Size(min = 2, max = 100, message = "Tên người mua phải từ 2 đến 100 ký tự")
    val buyerName: String,
    
    @field:NotBlank(message = "Email không được để trống")
    @field:Email(message = "Email không hợp lệ")
    val buyerEmail: String,
    
    @field:Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    val buyerPhone: String? = null,
    
    @field:NotBlank(message = "Phương thức thanh toán không được để trống")
    val paymentMethod: String,
    
    val promoCode: String? = null
)

data class TicketItemDto(
    @field:NotNull(message = "ID loại vé không được để trống")
    val ticketTypeId: UUID,
    
    @field:NotNull(message = "Số lượng vé không được để trống")
    @field:Min(value = 1, message = "Số lượng vé phải lớn hơn 0")
    val quantity: Int
) 