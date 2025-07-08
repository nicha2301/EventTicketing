package com.eventticketing.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.*

data class LocationDto(
    val id: UUID? = null,
    
    @field:NotBlank(message = "Tên địa điểm không được để trống")
    @field:Size(min = 2, max = 100, message = "Tên địa điểm phải từ 2 đến 100 ký tự")
    val name: String,
    
    @field:NotBlank(message = "Địa chỉ không được để trống")
    @field:Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    val address: String,
    
    @field:NotBlank(message = "Thành phố không được để trống")
    @field:Size(max = 100, message = "Thành phố không được vượt quá 100 ký tự")
    val city: String,
    
    @field:Size(max = 100, message = "Tỉnh/bang không được vượt quá 100 ký tự")
    val state: String? = null,
    
    @field:NotBlank(message = "Quốc gia không được để trống")
    @field:Size(max = 100, message = "Quốc gia không được vượt quá 100 ký tự")
    val country: String,
    
    @field:Size(max = 20, message = "Mã bưu điện không được vượt quá 20 ký tự")
    val postalCode: String? = null,
    
    @field:NotNull(message = "Vĩ độ không được để trống")
    val latitude: Double,
    
    @field:NotNull(message = "Kinh độ không được để trống")
    val longitude: Double,
    
    val capacity: Int? = null,
    
    @field:Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    val description: String? = null,
    
    @field:Size(max = 255, message = "Website không được vượt quá 255 ký tự")
    val website: String? = null,
    
    @field:Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    val phoneNumber: String? = null,
    
    val createdAt: LocalDateTime? = null,
    
    val updatedAt: LocalDateTime? = null
) 