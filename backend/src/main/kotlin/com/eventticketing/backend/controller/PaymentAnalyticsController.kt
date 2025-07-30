package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.analytics.PaymentMethodsDto
import com.eventticketing.backend.security.CurrentUser
import com.eventticketing.backend.security.UserPrincipal
import com.eventticketing.backend.service.PaymentAnalyticsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/analytics/payment")
class PaymentAnalyticsController(
    private val paymentAnalyticsService: PaymentAnalyticsService
) {
    
    @GetMapping("/methods/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    fun getPaymentMethodsAnalysis(
        @PathVariable eventId: UUID,
        @CurrentUser userPrincipal: UserPrincipal?
    ): ResponseEntity<PaymentMethodsDto> {
        val paymentAnalysis = paymentAnalyticsService.getPaymentMethodsAnalysis(eventId)
        return ResponseEntity(paymentAnalysis, HttpStatus.OK)
    }
}
