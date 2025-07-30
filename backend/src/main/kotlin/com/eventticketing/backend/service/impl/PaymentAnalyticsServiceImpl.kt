package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.analytics.PaymentMethodsDto
import com.eventticketing.backend.dto.analytics.PaymentMethodStats
import com.eventticketing.backend.repository.PaymentRepository
import com.eventticketing.backend.service.PaymentAnalyticsService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Service
class PaymentAnalyticsServiceImpl(
    private val paymentRepository: PaymentRepository
) : PaymentAnalyticsService {
    
    override fun getPaymentMethodsAnalysis(eventId: UUID): PaymentMethodsDto {
        val results = paymentRepository.getPaymentMethodStatsForEvent(eventId)
        
        val totalTransactions = results.sumOf { (it[1] as Number).toInt() }
        val totalAmount = results.fold(BigDecimal.ZERO) { acc, result ->
            acc + (result[2] as BigDecimal)
        }
        
        val averageTransactionAmount = if (totalTransactions > 0) {
            totalAmount.divide(BigDecimal(totalTransactions), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
        
        val paymentMethods = results.associate { result ->
            val paymentMethod = result[0] as String
            val transactionCount = (result[1] as Number).toInt()
            val methodTotalAmount = result[2] as BigDecimal
            val methodAverageAmount = if (transactionCount > 0) {
                methodTotalAmount.divide(BigDecimal(transactionCount), 2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }
            val percentage = if (totalAmount > BigDecimal.ZERO) {
                (methodTotalAmount.divide(totalAmount, 4, RoundingMode.HALF_UP) * BigDecimal(100)).toDouble()
            } else {
                0.0
            }
            
            paymentMethod to PaymentMethodStats(
                transactionCount = transactionCount,
                totalAmount = methodTotalAmount,
                averageAmount = methodAverageAmount,
                percentage = percentage
            )
        }
        
        return PaymentMethodsDto(
            paymentMethods = paymentMethods,
            totalTransactions = totalTransactions,
            totalAmount = totalAmount,
            averageTransactionAmount = averageTransactionAmount
        )
    }
}
