package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentRequestDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentResponseDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.PaymentRepository
import com.nicha.eventticketing.util.NetworkUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PaymentRepository {
    
    override fun createPayment(paymentRequest: PaymentRequestDto): Flow<Resource<PaymentResponseDto>> = flow {
        emit(Resource.Loading())
        
        try {
            if (!NetworkUtil.isNetworkAvailable()) {
                emit(Resource.Error("Không có kết nối mạng"))
                return@flow
            }
            
            val response = apiService.createPayment(paymentRequest)
            
            if (response.isSuccessful) {
                response.body()?.let { paymentResponse ->
                    emit(Resource.Success(paymentResponse))
                } ?: run {
                    emit(Resource.Error("Phản hồi từ server không hợp lệ"))
                }
            } else {
                val errorMsg = "Tạo payment thất bại: ${response.code()} - ${response.message()}"
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Lỗi khi tạo payment: ${e.message}"
            emit(Resource.Error(errorMsg))
        }
    }
    
    override fun getUserPayments(
        page: Int,
        size: Int,
        status: String?,
        paymentMethod: String?
    ): Flow<Resource<List<PaymentResponseDto>>> = flow {
        emit(Resource.Loading())
        
        try {
            if (!NetworkUtil.isNetworkAvailable()) {
                emit(Resource.Error("Không có kết nối mạng"))
                return@flow
            }
            
            val response = apiService.getUserPayments(page, size, status, paymentMethod)
            
            if (response.isSuccessful) {
                response.body()?.let { paymentsList ->
                    emit(Resource.Success(paymentsList))
                } ?: run {
                    emit(Resource.Error("Phản hồi từ server không hợp lệ"))
                }
            } else {
                val errorMsg = "Lấy payments thất bại: ${response.code()} - ${response.message()}"
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Lỗi khi lấy payments: ${e.message}"
            emit(Resource.Error(errorMsg))
        }
    }
    
    override fun getPaymentById(paymentId: String): Flow<Resource<PaymentResponseDto>> = flow {
        emit(Resource.Loading())
        
        try {
            if (!NetworkUtil.isNetworkAvailable()) {
                emit(Resource.Error("Không có kết nối mạng"))
                return@flow
            }
            
            val response = apiService.getUserPayments(page = 0, size = 100) // Lấy nhiều hơn để tìm được payment
            
            if (response.isSuccessful) {
                response.body()?.let { paymentsList ->
                    val payment = paymentsList.find { it.id == paymentId }
                    
                    if (payment != null) {
                        emit(Resource.Success(payment))
                    } else {
                        val errorMsg = "Không tìm thấy payment với ID: $paymentId"
                        emit(Resource.Error(errorMsg))
                    }
                } ?: run {
                    emit(Resource.Error("Phản hồi từ server không hợp lệ"))
                }
            } else {
                val errorMsg = "Lấy payment thất bại: ${response.code()} - ${response.message()}"
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Lỗi khi lấy payment: ${e.message}"
            emit(Resource.Error(errorMsg))
        }
    }
    
    override fun processMomoReturn(
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
    ): Flow<Resource<PaymentResponseDto>> = flow {
        emit(Resource.Loading())
        
        try {
            if (!NetworkUtil.isNetworkAvailable()) {
                emit(Resource.Error("Không có kết nối mạng"))
                return@flow
            }
            
            val response = apiService.processMomoReturn(
                partnerCode, orderId, requestId, amount, orderInfo, orderType,
                transId, resultCode, message, payType, responseTime, extraData, signature
            )
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        emit(Resource.Success(apiResponse.data))
                    } else {
                        val errorMsg = apiResponse.message ?: "Xử lý MoMo return thất bại"
                        emit(Resource.Error(errorMsg))
                    }
                } ?: run {
                    emit(Resource.Error("Phản hồi từ server không hợp lệ"))
                }
            } else {
                val errorMsg = "Xử lý MoMo return thất bại: ${response.code()} - ${response.message()}"
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Lỗi khi xử lý MoMo return: ${e.message}"
            emit(Resource.Error(errorMsg))
        }
    }
}
