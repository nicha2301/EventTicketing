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
     * Process Momo payment return callback
     * @param params Map of parameters returned from Momo
     * @return API response with payment result
     */
    fun processMomoReturn(params: Map<String, String>): ApiResponse<PaymentResponseDto>
    
    /**
     * Process Momo IPN (Instant Payment Notification)
     * @param params Map of parameters sent from Momo
     * @return API response with processing result
     */
    fun processMomoIpn(params: Map<String, String>): ApiResponse<String>
    
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