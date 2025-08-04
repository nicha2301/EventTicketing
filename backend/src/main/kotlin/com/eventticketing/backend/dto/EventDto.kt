package com.eventticketing.backend.dto

import com.eventticketing.backend.entity.EventStatus
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * DTO đại diện cho thông tin sự kiện trả về
 */
data class EventDto(
    val id: UUID? = null,
    val title: String,
    val description: String,
    val shortDescription: String,
    val organizerId: UUID,
    val organizerName: String,
    val categoryId: UUID,
    val categoryName: String,
    val locationId: UUID,
    val locationName: String,
    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val status: EventStatus,
    val maxAttendees: Int,
    val currentAttendees: Int,
    val featuredImageUrl: String?,
    val imageUrls: List<String>?,
    val minTicketPrice: BigDecimal?,
    val maxTicketPrice: BigDecimal?,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val startDate: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val endDate: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime? = null,
    
    val isPrivate: Boolean = false,
    val isFeatured: Boolean = false,
    val isFree: Boolean = false,
    
    val ticketTypes: List<TicketTypeDto>? = null
)

/**
 * DTO cho việc tạo sự kiện mới
 */
data class EventCreateDto(
    @field:NotBlank(message = "Tiêu đề không được để trống")
    val title: String = "",
    
    @field:NotBlank(message = "Mô tả không được để trống")
    val description: String = "",
    
    @field:NotBlank(message = "Mô tả ngắn không được để trống")
    val shortDescription: String = "",
    
    @field:NotNull(message = "Danh mục không được để trống")
    val categoryId: UUID = UUID.randomUUID(),
    
    @field:NotNull(message = "Địa điểm không được để trống")
    val locationId: UUID = UUID.randomUUID(),
    
    @field:NotBlank(message = "Địa chỉ không được để trống")
    val address: String = "",
    
    @field:NotBlank(message = "Thành phố không được để trống")
    val city: String = "",
    
    @field:NotNull(message = "Vĩ độ không được để trống")
    val latitude: Double = 0.0,
    
    @field:NotNull(message = "Kinh độ không được để trống")
    val longitude: Double = 0.0,
    
    @field:NotNull(message = "Số lượng người tham dự tối đa không được để trống")
    @field:Min(value = 1, message = "Số lượng người tham dự tối đa phải lớn hơn 0")
    val maxAttendees: Int = 1,
    
    @field:NotNull(message = "Ngày bắt đầu không được để trống")
    @field:Future(message = "Ngày bắt đầu phải là ngày trong tương lai")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startDate: LocalDateTime = LocalDateTime.now().plusDays(1),
    
    @field:NotNull(message = "Ngày kết thúc không được để trống")
    @field:Future(message = "Ngày kết thúc phải là ngày trong tương lai")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val endDate: LocalDateTime = LocalDateTime.now().plusDays(2),
    
    @JsonProperty("isPrivate")
    val isPrivate: Boolean = false,
    
    @JsonProperty("isDraft")
    val isDraft: Boolean = true,
    
    @JsonProperty("isFree")
    val isFree: Boolean = false
)

/**
 * DTO cho việc cập nhật thông tin sự kiện
 */
data class EventUpdateDto(
    val title: String? = null,
    val description: String? = null,
    val shortDescription: String? = null,
    val categoryId: UUID? = null,
    val locationId: UUID? = null,
    val address: String? = null,
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val maxAttendees: Int? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val startDate: LocalDateTime? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val endDate: LocalDateTime? = null,
    
    val isPrivate: Boolean? = null,
    val isFree: Boolean? = null
)

/**
 * DTO cho tìm kiếm sự kiện
 */
data class EventSearchDto(
    val keyword: String? = null,
    val categoryId: UUID? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val startDate: LocalDateTime? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val endDate: LocalDateTime? = null,
    
    val locationId: UUID? = null,
    val city: String? = null,
    val radius: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val status: String? = null,
    val isFree: Boolean? = null,
    val isFeatured: Boolean? = null
)

/**
 * DTO cho hình ảnh
 */
data class ImageDto(
    val id: UUID? = null,
    val url: String,
    val eventId: UUID? = null,
    val isPrimary: Boolean = false,
    val width: Int? = null,
    val height: Int? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime? = null
)

/**
 * DTO cho việc hủy sự kiện
 */
data class EventCancellationDto(
    @field:NotBlank(message = "Lý do hủy không được để trống")
    val reason: String
) 