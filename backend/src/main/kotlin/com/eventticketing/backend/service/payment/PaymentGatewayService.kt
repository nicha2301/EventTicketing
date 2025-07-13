package com.eventticketing.backend.service.payment

import com.eventticketing.backend.dto.payment.PaymentRequestDto
import com.eventticketing.backend.dto.payment.PaymentResponseDto

/**
 * Interface cho các payment gateway
 */
interface PaymentGatewayService {
    /**
     * Lấy tên của payment gateway
     */
    fun getName(): String
    
    /**
     * Khởi tạo thanh toán
     */
    fun initiatePayment(paymentRequest: PaymentRequestDto): PaymentResponseDto
    
    /**
     * Xác thực thanh toán
     */
    fun verifyPayment(params: Map<String, String>): Boolean
} 