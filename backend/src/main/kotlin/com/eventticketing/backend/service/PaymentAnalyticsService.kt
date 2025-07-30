package com.eventticketing.backend.service

import com.eventticketing.backend.dto.analytics.PaymentMethodsDto
import java.util.*

interface PaymentAnalyticsService {
    fun getPaymentMethodsAnalysis(eventId: UUID): PaymentMethodsDto
}
