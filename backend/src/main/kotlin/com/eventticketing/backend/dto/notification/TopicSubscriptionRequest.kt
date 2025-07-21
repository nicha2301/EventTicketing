package com.eventticketing.backend.dto.notification

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class TopicSubscriptionRequest(
    @field:NotBlank(message = "Chủ đề không được để trống")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9-_.~%]{1,900}$",
        message = "Chủ đề chỉ được chứa các ký tự a-z, A-Z, 0-9, gạch ngang, gạch dưới, dấu chấm, ngã, phần trăm"
    )
    val topic: String
) 