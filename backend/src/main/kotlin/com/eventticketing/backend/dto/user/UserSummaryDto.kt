package com.eventticketing.backend.dto.user

import com.eventticketing.backend.entity.UserRole
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserSummaryDto(
    val id: UUID,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val profileImageUrl: String?
) 