package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentRequestDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentResponseDto
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository xử lý các chức năng liên quan đến thanh toán
 */
interface PaymentRepository {
    
    /**
     * Tạo payment mới
     */
    fun createPayment(paymentRequest: PaymentRequestDto): Flow<Resource<PaymentResponseDto>>
    
    /**
     * Lấy danh sách payments của user
     */
    fun getUserPayments(
        page: Int,
        size: Int,
        status: String? = null,
        paymentMethod: String? = null
    ): Flow<Resource<List<PaymentResponseDto>>>
    
    /**
     * Lấy thông tin payment theo ID
     */
    fun getPaymentById(paymentId: String): Flow<Resource<PaymentResponseDto>>
    
    /**
     * Xử lý MoMo return callback
     */
    fun processMomoReturn(
        partnerCode: String,
        orderId: String,
        requestId: String,
        amount: String,
        orderInfo: String,
        orderType: String,
        transId: String,
        resultCode: String,
        message: String,
        payType: String,
        responseTime: String,
        extraData: String,
        signature: String
    ): Flow<Resource<PaymentResponseDto>>
} 