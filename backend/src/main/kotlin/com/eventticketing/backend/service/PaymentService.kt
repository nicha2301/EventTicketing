package com.eventticketing.backend.service

import com.eventticketing.backend.dto.payment.PaymentCreateDto
import com.eventticketing.backend.dto.payment.PaymentDto
import com.eventticketing.backend.dto.payment.PaymentResponseDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

/**
 * Service interface cho xử lý thanh toán
 */
interface PaymentService {
    /**
     * Xử lý yêu cầu thanh toán
     */
    fun processPayment(paymentCreateDto: PaymentCreateDto, userId: UUID): PaymentResponseDto
    
    /**
     * Hoàn thành thanh toán sau khi nhận callback từ cổng thanh toán
     */
    fun completePayment(paymentId: UUID, transactionId: String, params: Map<String, String>): Boolean
    
    /**
     * Hủy thanh toán
     */
    fun cancelPayment(paymentId: UUID): Boolean
    
    /**
     * Lấy thông tin thanh toán theo ID
     */
    fun getPaymentById(paymentId: UUID): PaymentDto
    
    /**
     * Lấy danh sách thanh toán của người dùng
     */
    fun getPaymentsByUserId(userId: UUID, pageable: Pageable): Page<PaymentDto>
} 