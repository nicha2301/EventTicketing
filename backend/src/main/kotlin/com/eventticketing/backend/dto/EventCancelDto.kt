package com.eventticketing.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class EventCancelDto(
    @field:NotBlank(message = "Lý do hủy không được để trống")
    @field:Size(min = 10, max = 1000, message = "Lý do hủy phải từ 10 đến 1000 ký tự")
    val reason: String
) 