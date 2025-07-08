package com.eventticketing.backend.service

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.dto.payment.PaymentCreateDto
import com.eventticketing.backend.dto.payment.PaymentResponseDto
import com.eventticketing.backend.dto.payment.RefundRequestDto
import com.eventticketing.backend.entity.Payment
import com.eventticketing.backend.entity.PaymentStatus
import java.util.UUID

/**
 * Service interface for handling payment operations
 */
interface PaymentService {

    /**
     * Create a new payment transaction
     * @param paymentCreateDto Payment creation data
     * @return Payment response with payment URL and transaction information
     */
    fun createPayment(paymentCreateDto: PaymentCreateDto): PaymentResponseDto
    
    /**
     * Process VNPay payment return callback
     * @param params Map of parameters returned from VNPay
     * @return API response with payment result
     */
    fun processVnPayReturn(params: Map<String, String>): ApiResponse<PaymentResponseDto>
    
    /**
     * Process VNPay IPN (Instant Payment Notification)
     * @param params Map of parameters sent from VNPay
     * @return API response with processing result
     */
    fun processVnPayIpn(params: Map<String, String>): ApiResponse<String>
    
    /**
     * Process Stripe webhook events
     * @param payload Raw payload from Stripe webhook
     * @param signature Stripe signature header
     * @return API response with processing result
     */
    fun processStripeWebhook(payload: String, signature: String): ApiResponse<String>
    
    /**
     * Get payment by ID
     * @param id Payment ID
     * @return Payment entity
     */
    fun getPaymentById(id: UUID): Payment
    
    /**
     * Get payments for current user
     * @return List of payment response DTOs
     */
    fun getCurrentUserPayments(): List<PaymentResponseDto>
    
    /**
     * Process refund request
     * @param paymentId Payment ID
     * @param refundRequestDto Refund request data
     * @return API response with refund result
     */
    fun processRefund(paymentId: UUID, refundRequestDto: RefundRequestDto): ApiResponse<PaymentResponseDto>
    
    /**
     * Update payment status
     * @param paymentId Payment ID
     * @param status New payment status
     * @return Updated payment
     */
    fun updatePaymentStatus(paymentId: UUID, status: PaymentStatus): Payment
} 