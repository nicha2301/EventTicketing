package com.eventticketing.backend.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class TicketTypeDto(
    val id: UUID? = null,
    
    @field:NotBlank(message = "Tên loại vé không được để trống")
    @field:Size(min = 2, max = 100, message = "Tên loại vé phải từ 2 đến 100 ký tự")
    val name: String,
    
    @field:Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    val description: String? = null,
    
    @field:NotNull(message = "Giá vé không được để trống")
    @field:Min(value = 0, message = "Giá vé phải lớn hơn hoặc bằng 0")
    val price: BigDecimal,
    
    @field:NotNull(message = "Số lượng vé không được để trống")
    @field:Min(value = 1, message = "Số lượng vé phải lớn hơn 0")
    val quantity: Int,
    
    val availableQuantity: Int? = null,
    
    @field:NotNull(message = "ID sự kiện không được để trống")
    val eventId: UUID? = null,
    
    val salesStartDate: LocalDateTime? = null,
    
    val salesEndDate: LocalDateTime? = null,
    
    val maxTicketsPerCustomer: Int? = null,
    
    val minTicketsPerOrder: Int? = 1,
    
    val isEarlyBird: Boolean = false,
    
    val isVIP: Boolean = false,
    
    val isActive: Boolean = true,
    
    val createdAt: LocalDateTime? = null,
    
    val updatedAt: LocalDateTime? = null
)

data class TicketTypeCreateDto(
    @field:NotBlank(message = "Ticket name is required")
    val name: String,
    
    val description: String?,
    
    @field:NotNull(message = "Price is required")
    val price: BigDecimal,
    
    @field:NotNull(message = "Quantity is required")
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int,
    
    @field:NotNull(message = "Sale start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val saleStartDate: LocalDateTime,
    
    @field:NotNull(message = "Sale end date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val saleEndDate: LocalDateTime
)

data class TicketTypeUpdateDto(
    val name: String?,
    val description: String?,
    val price: BigDecimal?,
    val quantity: Int?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val saleStartDate: LocalDateTime?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val saleEndDate: LocalDateTime?
) 