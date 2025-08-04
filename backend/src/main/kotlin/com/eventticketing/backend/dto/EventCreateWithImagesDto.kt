package com.eventticketing.backend.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.*

data class EventCreateWithImagesDto(
    @field:NotBlank(message = "Tiêu đề không được để trống")
    val title: String,
    
    @field:NotBlank(message = "Mô tả không được để trống")
    val description: String,
    
    @field:NotBlank(message = "Mô tả ngắn không được để trống")
    val shortDescription: String,
    
    @field:NotNull(message = "Danh mục không được để trống")
    val categoryId: UUID,
    
    @field:NotNull(message = "Địa điểm không được để trống")
    val locationId: UUID,
    
    @field:NotBlank(message = "Địa chỉ không được để trống")
    val address: String,
    
    @field:NotBlank(message = "Thành phố không được để trống")
    val city: String,
    
    @field:NotNull(message = "Vĩ độ không được để trống")
    val latitude: Double,
    
    @field:NotNull(message = "Kinh độ không được để trống")
    val longitude: Double,
    
    @field:NotNull(message = "Số lượng người tham dự tối đa không được để trống")
    @field:Min(value = 1, message = "Số lượng người tham dự tối đa phải lớn hơn 0")
    val maxAttendees: Int,
    
    @field:NotNull(message = "Ngày bắt đầu không được để trống")
    @field:Future(message = "Ngày bắt đầu phải là ngày trong tương lai")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val startDate: LocalDateTime,
    
    @field:NotNull(message = "Ngày kết thúc không được để trống")
    @field:Future(message = "Ngày kết thúc phải là ngày trong tương lai")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val endDate: LocalDateTime,
    
    val isPrivate: Boolean = false,
    val isDraft: Boolean = true,
    val isFree: Boolean = false,
    
    val images: List<EventImageCreateDto>? = null
)

data class EventImageCreateDto(
    @field:NotBlank(message = "Public ID không được để trống")
    val publicId: String,
    
    @field:NotBlank(message = "Secure URL không được để trống")
    val secureUrl: String,
    
    val width: Int = 0,
    val height: Int = 0,
    val isPrimary: Boolean = false
)
