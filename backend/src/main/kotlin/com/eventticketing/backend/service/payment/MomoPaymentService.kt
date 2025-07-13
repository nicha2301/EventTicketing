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
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class MomoPaymentService(
    private val restTemplate: RestTemplate
) : PaymentGatewayService {

    private val logger = LoggerFactory.getLogger(MomoPaymentService::class.java)

    @Value("\${payment.momo.endpoint}")
    private lateinit var momoEndpoint: String

    @Value("\${payment.momo.partner-code}")
    private lateinit var partnerCode: String

    @Value("\${payment.momo.access-key}")
    private lateinit var accessKey: String

    @Value("\${payment.momo.secret-key}")
    private lateinit var secretKey: String

    @Value("\${payment.momo.return-url}")
    private lateinit var returnUrl: String

    @Value("\${payment.momo.notify-url}")
    private lateinit var notifyUrl: String

    override fun getName(): String = "momo"

    override fun initiatePayment(paymentRequest: PaymentRequestDto): PaymentResponseDto {
        try {
            val requestId = UUID.randomUUID().toString()
            val orderId = "ORDER_${System.currentTimeMillis()}"
            val amount = paymentRequest.amount.toString()
            val orderInfo = "Thanh toán vé sự kiện: ${paymentRequest.description}"
            
            // Tạo chuỗi để tạo chữ ký
            val rawSignature = "accessKey=$accessKey" +
                    "&amount=$amount" +
                    "&extraData=" +
                    "&ipnUrl=$notifyUrl" +
                    "&orderId=$orderId" +
                    "&orderInfo=$orderInfo" +
                    "&partnerCode=$partnerCode" +
                    "&redirectUrl=$returnUrl" +
                    "&requestId=$requestId" +
                    "&requestType=captureWallet"
            
            // Tạo chữ ký HMAC SHA256
            val signature = createSignature(rawSignature, secretKey)
            
            // Tạo request body
            val requestBody = mapOf(
                "partnerCode" to partnerCode,
                "accessKey" to accessKey,
                "requestId" to requestId,
                "amount" to amount,
                "orderId" to orderId,
                "orderInfo" to orderInfo,
                "redirectUrl" to returnUrl,
                "ipnUrl" to notifyUrl,
                "extraData" to "",
                "requestType" to "captureWallet",
                "signature" to signature,
                "lang" to "vi"
            )
            
            // Gọi API Momo
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val request = HttpEntity(requestBody, headers)
            
            val response = restTemplate.postForObject(
                momoEndpoint,
                request,
                Map::class.java
            ) ?: throw PaymentException("Không nhận được phản hồi từ Momo")
            
            // Kiểm tra kết quả
            if (response["resultCode"] != 0) {
                throw PaymentException("Lỗi từ Momo: ${response["message"]}")
            }
            
            // Trả về kết quả
            return PaymentResponseDto(
                success = true,
                redirectUrl = response["payUrl"] as String,
                transactionId = orderId,
                message = "Yêu cầu thanh toán thành công"
            )
            
        } catch (e: Exception) {
            logger.error("Lỗi khi xử lý thanh toán Momo", e)
            throw PaymentException("Lỗi khi xử lý thanh toán Momo: ${e.message}")
        }
    }

    override fun verifyPayment(params: Map<String, String>): Boolean {
        try {
            val partnerCode = params["partnerCode"] ?: return false
            val orderId = params["orderId"] ?: return false
            val requestId = params["requestId"] ?: return false
            val amount = params["amount"] ?: return false
            val orderInfo = params["orderInfo"] ?: return false
            val orderType = params["orderType"] ?: return false
            val transId = params["transId"] ?: return false
            val resultCode = params["resultCode"] ?: return false
            val message = params["message"] ?: return false
            val payType = params["payType"] ?: return false
            val responseTime = params["responseTime"] ?: return false
            val extraData = params["extraData"] ?: ""
            val signature = params["signature"] ?: return false
            
            // Kiểm tra resultCode
            if (resultCode != "0") {
                logger.error("Momo payment failed with result code: $resultCode, message: $message")
                return false
            }
            
            // Tạo chuỗi để kiểm tra chữ ký
            val rawSignature = "accessKey=$accessKey" +
                    "&amount=$amount" +
                    "&extraData=$extraData" +
                    "&message=$message" +
                    "&orderId=$orderId" +
                    "&orderInfo=$orderInfo" +
                    "&orderType=$orderType" +
                    "&partnerCode=$partnerCode" +
                    "&payType=$payType" +
                    "&requestId=$requestId" +
                    "&responseTime=$responseTime" +
                    "&resultCode=$resultCode" +
                    "&transId=$transId"
            
            // Tạo chữ ký và so sánh
            val calculatedSignature = createSignature(rawSignature, secretKey)
            
            if (calculatedSignature != signature) {
                logger.error("Invalid Momo signature")
                return false
            }
            
            return true
        } catch (e: Exception) {
            logger.error("Error verifying Momo payment", e)
            return false
        }
    }
    
    /**
     * Tạo chữ ký HMAC SHA256
     */
    private fun createSignature(data: String, secretKey: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
        mac.init(secretKeySpec)
        val rawHmac = mac.doFinal(data.toByteArray())
        return rawHmac.joinToString("") { "%02x".format(it) }
    }
} 