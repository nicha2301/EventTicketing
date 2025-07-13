package com.eventticketing.backend.service.payment

import com.eventticketing.backend.dto.payment.PaymentRequestDto
import com.eventticketing.backend.dto.payment.PaymentResponseDto
import com.eventticketing.backend.exception.PaymentException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class ZaloPayService(
    private val restTemplate: RestTemplate
) : PaymentGatewayService {

    private val logger = LoggerFactory.getLogger(ZaloPayService::class.java)

    @Value("\${payment.zalopay.endpoint}")
    private lateinit var zaloPayEndpoint: String

    @Value("\${payment.zalopay.app-id}")
    private lateinit var appId: String

    @Value("\${payment.zalopay.key1}")
    private lateinit var key1: String

    @Value("\${payment.zalopay.key2}")
    private lateinit var key2: String

    @Value("\${payment.zalopay.return-url}")
    private lateinit var returnUrl: String

    @Value("\${payment.zalopay.notify-url}")
    private lateinit var notifyUrl: String

    override fun getName(): String = "zalopay"

    override fun initiatePayment(paymentRequest: PaymentRequestDto): PaymentResponseDto {
        try {
            val appTransId = "${appId}${System.currentTimeMillis()}"
            val appTime = Instant.now().epochSecond.toString()
            val embedData = "{}"
            val amount = paymentRequest.amount.toString()
            val orderInfo = "Thanh toán vé sự kiện: ${paymentRequest.description}"
            
            // Tạo chuỗi để tạo chữ ký
            val dataToHash = "$appId|$appTransId|$amount|$appTime|$embedData|$orderInfo"
            val mac = hmacSHA256(key1, dataToHash)
            
            // Tạo request body
            val requestBody = mapOf(
                "app_id" to appId,
                "app_trans_id" to appTransId,
                "app_user" to "user_${UUID.randomUUID()}",
                "app_time" to appTime,
                "amount" to amount,
                "item" to "[]",
                "description" to orderInfo,
                "embed_data" to embedData,
                "bank_code" to "",
                "callback_url" to notifyUrl,
                "redirect_url" to returnUrl,
                "mac" to mac
            )
            
            // Gọi API ZaloPay
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val request = HttpEntity(requestBody, headers)
            
            val response = restTemplate.postForObject(
                zaloPayEndpoint,
                request,
                Map::class.java
            ) ?: throw PaymentException("Không nhận được phản hồi từ ZaloPay")
            
            // Kiểm tra kết quả
            val returnCode = response["return_code"] as Int
            if (returnCode != 1) {
                throw PaymentException("Lỗi từ ZaloPay: ${response["return_message"]}")
            }
            
            // Trả về kết quả
            return PaymentResponseDto(
                success = true,
                redirectUrl = response["order_url"] as String,
                transactionId = appTransId,
                message = "Yêu cầu thanh toán thành công"
            )
            
        } catch (e: Exception) {
            logger.error("Lỗi khi xử lý thanh toán ZaloPay", e)
            throw PaymentException("Lỗi khi xử lý thanh toán ZaloPay: ${e.message}")
        }
    }

    override fun verifyPayment(params: Map<String, String>): Boolean {
        try {
            val dataStr = params["data"] ?: return false
            val mac = params["mac"] ?: return false
            
            // Tạo chữ ký và so sánh
            val calculatedMac = hmacSHA256(key2, dataStr)
            
            if (calculatedMac != mac) {
                logger.error("Invalid ZaloPay signature")
                return false
            }
            
            // Parse data để kiểm tra thêm
            val data = parseData(dataStr)
            val returnCode = data["return_code"] as? Int ?: return false
            
            if (returnCode != 1) {
                logger.error("ZaloPay payment failed with return code: $returnCode")
                return false
            }
            
            return true
        } catch (e: Exception) {
            logger.error("Error verifying ZaloPay payment", e)
            return false
        }
    }
    
    /**
     * Tạo chữ ký HMAC SHA256
     */
    private fun hmacSHA256(key: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "HmacSHA256")
        mac.init(secretKeySpec)
        val rawHmac = mac.doFinal(data.toByteArray())
        return rawHmac.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Parse data string từ ZaloPay
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseData(dataStr: String): Map<String, Any> {
        // Trong thực tế, cần sử dụng thư viện JSON để parse chuỗi này
        // Đây chỉ là giả lập
        return mapOf(
            "return_code" to 1,
            "return_message" to "success",
            "zp_trans_id" to "123456789",
            "app_trans_id" to "app_123456789"
        )
    }
} 