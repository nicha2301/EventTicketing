package com.nicha.eventticketing.domain.repository

import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentMethodDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentRequestDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentResponseDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentStatusUpdateDto
import com.nicha.eventticketing.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository xử lý các chức năng liên quan đến thanh toán
 */
interface PaymentRepository {
    /**
     * Lấy danh sách các thanh toán
     * @param page Số trang
     * @param size Số lượng mỗi trang
     * @return Flow<Resource<PageDto<PaymentDto>>> Flow chứa danh sách thanh toán theo trang
     */
    fun getPayments(
        page: Int = 0,
        size: Int = 20
    ): Flow<Resource<PageDto<PaymentDto>>>
    
    /**
     * Lấy thông tin chi tiết của một thanh toán
     * @param paymentId ID của thanh toán
     * @return Flow<Resource<PaymentDto>> Flow chứa thông tin thanh toán
     */
    fun getPaymentById(paymentId: String): Flow<Resource<PaymentDto>>
    
    /**
     * Tạo thanh toán mới
     * @param paymentRequest Thông tin yêu cầu thanh toán
     * @return Flow<Resource<PaymentResponseDto>> Flow chứa thông tin thanh toán đã tạo
     */
    fun createPayment(paymentRequest: PaymentRequestDto): Flow<Resource<PaymentResponseDto>>
    
    /**
     * Cập nhật trạng thái thanh toán
     * @param paymentId ID của thanh toán
     * @param statusUpdate Thông tin cập nhật trạng thái
     * @return Flow<Resource<PaymentDto>> Flow chứa thông tin thanh toán đã cập nhật
     */
    fun updatePaymentStatus(
        paymentId: String, 
        statusUpdate: PaymentStatusUpdateDto
    ): Flow<Resource<PaymentDto>>
    
    /**
     * Lấy danh sách các phương thức thanh toán
     * @return Flow<Resource<List<PaymentMethodDto>>> Flow chứa danh sách phương thức thanh toán
     */
    fun getPaymentMethods(): Flow<Resource<List<PaymentMethodDto>>>
    
    /**
     * Lấy danh sách thanh toán của người dùng hiện tại
     * @return Flow<Resource<List<PaymentResponseDto>>> Flow chứa danh sách thanh toán
     */
    fun getCurrentUserPayments(): Flow<Resource<List<PaymentResponseDto>>>
    
    /**
     * Hoàn tiền cho một thanh toán
     * @param paymentId ID của thanh toán
     * @param refundRequest Thông tin yêu cầu hoàn tiền
     * @return Flow<Resource<PaymentResponseDto>> Flow chứa thông tin thanh toán đã hoàn tiền
     */
    fun refundPayment(
        paymentId: String, 
        refundRequest: Map<String, Any>
    ): Flow<Resource<PaymentResponseDto>>
} 