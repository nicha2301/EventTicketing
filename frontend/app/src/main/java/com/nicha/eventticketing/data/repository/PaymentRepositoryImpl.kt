package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentMethodDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentRequestDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentResponseDto
import com.nicha.eventticketing.data.remote.dto.payment.PaymentStatusUpdateDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.PaymentRepository
import com.nicha.eventticketing.util.NetworkUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PaymentRepository {
    
    override fun getPayments(page: Int, size: Int): Flow<Resource<PageDto<PaymentDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getPayments(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                val paymentPage = response.body()?.data
                if (paymentPage != null) {
                    emit(Resource.Success(paymentPage))
                    Timber.d("Lấy danh sách thanh toán thành công: ${paymentPage.content?.size ?: 0} mục")
                } else {
                    emit(Resource.Error("Không tìm thấy dữ liệu thanh toán"))
                    Timber.e("Không tìm thấy dữ liệu thanh toán")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy danh sách thanh toán"))
                Timber.e("Lấy danh sách thanh toán thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách thanh toán")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getPaymentById(paymentId: String): Flow<Resource<PaymentDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getPaymentById(paymentId)
            if (response.isSuccessful && response.body()?.success == true) {
                val payment = response.body()?.data
                if (payment != null) {
                    emit(Resource.Success(payment))
                    Timber.d("Lấy thông tin thanh toán thành công: ${payment.id}")
                } else {
                    emit(Resource.Error("Không tìm thấy thông tin thanh toán"))
                    Timber.e("Không tìm thấy thông tin thanh toán")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy thông tin thanh toán"))
                Timber.e("Lấy thông tin thanh toán thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy thông tin thanh toán: $paymentId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun createPayment(paymentRequest: PaymentRequestDto): Flow<Resource<PaymentResponseDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.createPayment(paymentRequest)
            if (response.isSuccessful && response.body()?.success == true) {
                val payment = response.body()?.data
                if (payment != null) {
                    emit(Resource.Success(payment))
                    Timber.d("Tạo thanh toán thành công: ${payment.id}")
                } else {
                    emit(Resource.Error("Không tạo được thanh toán"))
                    Timber.e("Không tạo được thanh toán")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể tạo thanh toán"))
                Timber.e("Tạo thanh toán thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi tạo thanh toán")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun updatePaymentStatus(
        paymentId: String,
        statusUpdate: PaymentStatusUpdateDto
    ): Flow<Resource<PaymentDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.updatePaymentStatus(paymentId, statusUpdate)
            if (response.isSuccessful && response.body()?.success == true) {
                val payment = response.body()?.data
                if (payment != null) {
                    emit(Resource.Success(payment))
                    Timber.d("Cập nhật trạng thái thanh toán thành công: ${payment.id}, trạng thái: ${payment.status}")
                } else {
                    emit(Resource.Error("Không cập nhật được trạng thái thanh toán"))
                    Timber.e("Không cập nhật được trạng thái thanh toán")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể cập nhật trạng thái thanh toán"))
                Timber.e("Cập nhật trạng thái thanh toán thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật trạng thái thanh toán: $paymentId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getPaymentMethods(): Flow<Resource<List<PaymentMethodDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getPaymentMethods()
            if (response.isSuccessful && response.body()?.success == true) {
                val paymentMethods = response.body()?.data
                if (paymentMethods != null) {
                    emit(Resource.Success(paymentMethods))
                    Timber.d("Lấy danh sách phương thức thanh toán thành công: ${paymentMethods.size} mục")
                } else {
                    emit(Resource.Error("Không tìm thấy phương thức thanh toán"))
                    Timber.e("Không tìm thấy phương thức thanh toán")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy danh sách phương thức thanh toán"))
                Timber.e("Lấy danh sách phương thức thanh toán thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách phương thức thanh toán")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getCurrentUserPayments(): Flow<Resource<List<PaymentResponseDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getCurrentUserPayments()
            if (response.isSuccessful && response.body()?.success == true) {
                val payments = response.body()?.data
                if (payments != null) {
                    emit(Resource.Success(payments))
                    Timber.d("Lấy danh sách thanh toán của người dùng thành công: ${payments.size} mục")
                } else {
                    emit(Resource.Error("Không tìm thấy thanh toán của người dùng"))
                    Timber.e("Không tìm thấy thanh toán của người dùng")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy danh sách thanh toán của người dùng"))
                Timber.e("Lấy danh sách thanh toán của người dùng thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lấy danh sách thanh toán của người dùng")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun refundPayment(
        paymentId: String,
        refundRequest: Map<String, Any>
    ): Flow<Resource<PaymentResponseDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.refundPayment(paymentId, refundRequest)
            if (response.isSuccessful && response.body()?.success == true) {
                val payment = response.body()?.data
                if (payment != null) {
                    emit(Resource.Success(payment))
                    Timber.d("Hoàn tiền cho thanh toán thành công: ${payment.id}")
                } else {
                    emit(Resource.Error("Không hoàn tiền được cho thanh toán"))
                    Timber.e("Không hoàn tiền được cho thanh toán")
                }
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể hoàn tiền cho thanh toán"))
                Timber.e("Hoàn tiền cho thanh toán thất bại: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi hoàn tiền cho thanh toán: $paymentId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
} 