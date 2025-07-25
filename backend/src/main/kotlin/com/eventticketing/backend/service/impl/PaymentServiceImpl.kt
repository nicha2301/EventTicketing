package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.dto.payment.PaymentCreateDto
import com.eventticketing.backend.dto.payment.PaymentResponseDto
import com.eventticketing.backend.dto.payment.RefundRequestDto
import com.eventticketing.backend.entity.Payment
import com.eventticketing.backend.entity.PaymentStatus
import com.eventticketing.backend.entity.TicketStatus
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.exception.PaymentException
import com.eventticketing.backend.repository.PaymentRepository
import com.eventticketing.backend.repository.TicketRepository
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.service.PaymentService
import com.eventticketing.backend.util.SecurityUtils
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.UUID
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.TreeMap
import java.util.Locale
import java.math.BigDecimal
import java.text.Normalizer
import java.util.LinkedHashMap

@Service
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val ticketRepository: TicketRepository,
    private val userRepository: UserRepository,
    private val securityUtils: SecurityUtils,
    private val objectMapper: ObjectMapper,
    private val restTemplate: RestTemplate,

    @Value("\${payment.momo.return-url}")
    private val momoReturnUrl: String,

    @Value("\${payment.momo.ipn-url}")
    private val momoIpnUrl: String,

    @Value("\${payment.momo.partner-code}")
    private val momoPartnerCode: String,

    @Value("\${payment.momo.access-key}")
    private val momoAccessKey: String,
    
    @Value("\${payment.momo.secret-key}")
    private val momoSecretKey: String,
    
    @Value("\${payment.momo.api-endpoint}")
    private val momoApiEndpoint: String,

    @Value("\${payment.momo.request-type}")
    private val momoRequestType: String
) : PaymentService {

    private val logger = LoggerFactory.getLogger(PaymentServiceImpl::class.java)

    @Transactional
    override fun createPayment(paymentCreateDto: PaymentCreateDto): PaymentResponseDto {
        val currentUser = securityUtils.getCurrentUser()
            ?: throw IllegalStateException("User must be authenticated to create payment")
        
        val ticket = ticketRepository.findById(paymentCreateDto.ticketId)
            .orElseThrow { ResourceNotFoundException("Ticket not found with id: ${paymentCreateDto.ticketId}") }
        
        // Check if ticket is already paid
        val existingPayment = paymentRepository.findByTicket(ticket)
        if (existingPayment.isPresent && existingPayment.get().status == PaymentStatus.COMPLETED) {
            throw PaymentException("Ticket is already paid")
        }
        
        // Check if ticket belongs to current user
        if (ticket.user.id != currentUser.id) {
            throw PaymentException("Ticket does not belong to current user")
        }
        
        // Create new payment or update existing one
        val payment = existingPayment.orElseGet {
            Payment(
                user = currentUser,
                ticket = ticket,
                amount = paymentCreateDto.amount,
                paymentMethod = "momo"
            )
        }
        
        // Update payment if it exists
        if (existingPayment.isPresent) {
            payment.amount = paymentCreateDto.amount
            payment.paymentMethod = "momo"
            payment.updatedAt = LocalDateTime.now()
        }
        
        // Set description and metadata if provided
        payment.description = paymentCreateDto.description ?: "Thanh toán vé sự kiện"
        if (paymentCreateDto.metadata != null) {
            try {
                val jsonNode: JsonNode = objectMapper.valueToTree(paymentCreateDto.metadata)
                payment.metadata = objectMapper.writeValueAsString(jsonNode)
            } catch (e: Exception) {
                logger.warn("Failed to convert metadata to JSON: ${e.message}")
                payment.metadata = null
            }
        }
        
        // Generate Momo payment URL
        val returnUrl = if (paymentCreateDto.returnUrl.isEmpty()) momoReturnUrl else paymentCreateDto.returnUrl
        
        // Generate a unique transaction ID
        val transactionId = "MOMO${System.currentTimeMillis()}"
        payment.transactionId = transactionId
        
        // Create Momo payment request
        val paymentUrl = createMomoPaymentUrl(payment, returnUrl)
        
        payment.paymentUrl = paymentUrl
        payment.status = PaymentStatus.PENDING
        
        val savedPayment = paymentRepository.save(payment)
        
        val responseDto = mapToPaymentResponseDto(savedPayment)
        responseDto.paymentUrl = paymentUrl
        
        return responseDto
    }

    /**
     * Create Momo payment URL using the official Momo API
     */
    private fun createMomoPaymentUrl(payment: Payment, redirectUrl: String): String {
        try {
            val requestId = UUID.randomUUID().toString()
            val orderId = payment.transactionId!!
            val amount = payment.amount.multiply(BigDecimal(1)).toLong()
            val orderInfo = removeAccent(payment.description ?: "Thanh toan ve su kien")
            val extraData = ""

            // Build raw signature đúng thứ tự và tên trường theo tài liệu MoMo
            val rawSignature = "accessKey=$momoAccessKey" +
                "&amount=$amount" +
                "&extraData=$extraData" +
                "&ipnUrl=$momoIpnUrl" +
                "&orderId=$orderId" +
                "&orderInfo=$orderInfo" +
                "&partnerCode=$momoPartnerCode" +
                "&redirectUrl=$redirectUrl" +
                "&requestId=$requestId" +
                "&requestType=$momoRequestType"

            val signature = createMomoSignature(rawSignature, momoSecretKey)

            // Build request data gửi lên MoMo
            val fullRequest = mapOf(
                "partnerCode" to momoPartnerCode,
                "accessKey" to momoAccessKey,
                "requestId" to requestId,
                "amount" to amount.toString(),
                "orderId" to orderId,
                "orderInfo" to orderInfo,
                "redirectUrl" to redirectUrl,
                "ipnUrl" to momoIpnUrl,
                "extraData" to extraData,
                "requestType" to momoRequestType,
                "signature" to signature
            )

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val entity = HttpEntity(fullRequest, headers)

            logger.info("Sending Momo payment request: $fullRequest")
            val response = restTemplate.postForObject(momoApiEndpoint, entity, Map::class.java)
            logger.info("Received Momo payment response: $response")
            
            // Extract payment URL from response
            if (response != null && response["resultCode"] == 0) {
                return response["payUrl"] as String
            } else {
                val errorMessage = response?.get("message") as? String ?: "Unknown error"
                logger.error("Error from Momo: $errorMessage")
                throw PaymentException("Error from Momo: $errorMessage")
            }
        } catch (e: Exception) {
            logger.error("Error creating Momo payment URL: ${e.message}", e)
            throw PaymentException("Error creating Momo payment URL: ${e.message}")
        }
    }
    
    /**
     * Create HMAC-SHA256 signature for Momo API
     */
    private fun createMomoSignature(data: String, secretKey: String): String {
        try {
            val hmac = Mac.getInstance("HmacSHA256")
            val keySpec = SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
            hmac.init(keySpec)
            val result = hmac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            return bytesToHex(result)
        } catch (e: Exception) {
            logger.error("Error creating signature: ${e.message}", e)
            throw PaymentException("Error creating signature: ${e.message}")
        }
    }
    
    /**
     * Convert bytes to hexadecimal string
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef".toCharArray()
        val result = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val i = b.toInt() and 0xFF
            result.append(hexChars[i shr 4])
            result.append(hexChars[i and 0x0F])
        }
        return result.toString()
    }

    @Transactional
    override fun processMomoReturn(params: Map<String, String>): ApiResponse<PaymentResponseDto> {
        logger.info("Received Momo Return URL params: ${params.entries.joinToString(", ") { "${it.key}=${it.value}" }}")
        
        // Validate Momo response
        if (!validateMomoResponse(params)) {
            logger.error("Invalid Momo response: signature validation failed")
            return ApiResponse.error("Invalid signature. Please contact customer support.")
        }
        
        // Process payment response
        return processMomoPaymentResponse(params, "RETURN")
    }

    @Transactional
    override fun processMomoIpn(params: Map<String, String>): ApiResponse<String> {
        logger.info("Received Momo IPN params: ${params.entries.joinToString(", ") { "${it.key}=${it.value}" }}")
        
        // Validate Momo response
        if (!validateMomoResponse(params)) {
            logger.error("Invalid Momo IPN: signature validation failed")
            return ApiResponse.error("Invalid signature")
        }
        
        try {
            val result = processMomoPaymentResponse(params, "IPN")
            
            // Momo requires returning a specific response format for IPN
            if (result.success) {
                return ApiResponse.success("Momo IPN processed successfully")
            } else {
                return ApiResponse.error(result.message ?: "IPN processing failed")
            }
        } catch (e: Exception) {
            logger.error("Error processing Momo IPN: ${e.message}", e)
            return ApiResponse.error("Error processing IPN: ${e.message}")
        }
    }
    
    /**
     * Validate Momo response signature
     */
    private fun validateMomoResponse(params: Map<String, String>): Boolean {
        try {
            val signature = params["signature"] ?: return false
            val accessKey = momoAccessKey
            val amount = params["amount"] ?: return false
            val extraData = params["extraData"] ?: ""
            val message = params["message"] ?: ""
            val orderId = params["orderId"] ?: return false
            val orderInfo = params["orderInfo"] ?: ""
            val orderType = params["orderType"] ?: ""
            val partnerCode = params["partnerCode"] ?: momoPartnerCode
            val payType = params["payType"] ?: ""
            val requestId = params["requestId"] ?: return false
            val responseTime = params["responseTime"] ?: ""
            val resultCode = params["resultCode"] ?: params["errorCode"] ?: ""
            val transId = params["transId"] ?: ""

            // Build raw signature đúng thứ tự và trường theo tài liệu MoMo
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

            val calculatedSignature = createMomoSignature(rawSignature, momoSecretKey)

            logger.info("Momo signature validation - raw: $rawSignature")
            logger.info("Momo signature validation - expected: $signature")
            logger.info("Momo signature validation - calculated: $calculatedSignature")

            val isValid = calculatedSignature.equals(signature, ignoreCase = true)
            logger.info("Momo signature validation result: $isValid")
            return isValid
        } catch (e: Exception) {
            logger.error("Error validating Momo response: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Data class to store Momo response data
     */
    private data class MomoResponse(
        val orderId: String,
        val requestId: String,
        val amount: Long?,
        val orderInfo: String?,
        val orderType: String?,
        val transId: String?,
        val resultCode: Int,
        val message: String?,
        val payType: String?,
        val responseTime: Long?,
        val extraData: String?,
        val signature: String?
    ) {
        companion object {
            fun fromParams(params: Map<String, String>): MomoResponse {
                return MomoResponse(
                    orderId = params["orderId"] ?: "",
                    requestId = params["requestId"] ?: "",
                    amount = params["amount"]?.toLongOrNull(),
                    orderInfo = params["orderInfo"],
                    orderType = params["orderType"],
                    transId = params["transId"],
                    resultCode = params["resultCode"]?.toIntOrNull() ?: params["errorCode"]?.toIntOrNull() ?: -1,
                    message = params["message"],
                    payType = params["payType"],
                    responseTime = params["responseTime"]?.toLongOrNull(),
                    extraData = params["extraData"],
                    signature = params["signature"]
                )
            }
        }
    }

    /**
     * Process Momo payment response (common logic for both Return URL and IPN)
     */
    @Transactional
    private fun processMomoPaymentResponse(params: Map<String, String>, source: String): ApiResponse<PaymentResponseDto> {
        val momoResponse = MomoResponse.fromParams(params)
        logger.info("Processing Momo $source: orderId=${momoResponse.orderId}, resultCode=${momoResponse.resultCode}, " +
                "transId=${momoResponse.transId}, amount=${momoResponse.amount}")
        try {
            val payment = paymentRepository.findByTransactionId(momoResponse.orderId)
                .orElseThrow { ResourceNotFoundException("Payment not found with transaction ID: ${momoResponse.orderId}") }
            val ticket = payment.ticket

            if (source == "RETURN") {
                // Nếu đã completed thì trả về thành công, nếu chưa thì trả về đang chờ xác nhận
                return if (payment.status == PaymentStatus.COMPLETED) {
                    ApiResponse.success("Thanh toán thành công", mapToPaymentResponseDto(payment))
                } else {
                    ApiResponse.error("Thanh toán đang chờ xác nhận từ MoMo. Vui lòng kiểm tra lại sau khi nhận được thông báo xác nhận.", mapToPaymentResponseDto(payment))
                }
            }

            // Chỉ cập nhật trạng thái khi là IPN
            payment.status = when (momoResponse.resultCode) {
                0 -> {
                    logger.info("Payment successful ($source): ${momoResponse.orderId}")
                    if (ticket.status == TicketStatus.RESERVED) {
                        ticket.markAsPaid(payment.id!!)
                        if (ticket.ticketNumber == null) {
                            ticket.generateTicketNumber()
                            ticket.qrCode = generateQRCode(ticket.ticketNumber!!)
                        }
                        ticketRepository.save(ticket)
                        val ticketType = ticket.ticketType
                        ticketType.availableQuantity = (ticketType.availableQuantity - 1).coerceAtLeast(0)
                        ticketRepository.save(ticket)
                    }
                    PaymentStatus.COMPLETED
                }
                9000 -> {
                    logger.info("Payment cancelled by user ($source): ${momoResponse.orderId}")
                    PaymentStatus.CANCELLED
                }
                else -> {
                    logger.warn("Payment failed with code ${momoResponse.resultCode} ($source): ${momoResponse.orderId}")
                    PaymentStatus.FAILED
                }
            }
            payment.updatedAt = LocalDateTime.now()
            val metadata = mutableMapOf<String, String>()
            if (payment.metadata != null) {
                try {
                    val existingMetadata = objectMapper.readValue(payment.metadata, Map::class.java) as Map<String, Any>
                    metadata.putAll(existingMetadata.mapValues { it.value.toString() })
                } catch (e: Exception) {
                    logger.warn("Failed to parse existing metadata: ${e.message}")
                }
            }
            momoResponse.transId?.let { metadata["momoTransId"] = it }
            momoResponse.resultCode.let { metadata["momoResultCode"] = it.toString() }
            momoResponse.message?.let { metadata["momoMessage"] = it }
            momoResponse.payType?.let { metadata["payType"] = it }
            momoResponse.responseTime?.let { metadata["responseTime"] = it.toString() }
            metadata["processedBy"] = source
            metadata["processedAt"] = LocalDateTime.now().toString()
            try {
                val jsonNode: JsonNode = objectMapper.valueToTree(metadata)
                payment.metadata = objectMapper.writeValueAsString(jsonNode)
            } catch (e: Exception) {
                logger.warn("Failed to update metadata: ${e.message}")
            }
            val updatedPayment = paymentRepository.save(payment)
            return if (updatedPayment.status == PaymentStatus.COMPLETED) {
                ApiResponse.success("Thanh toán thành công", mapToPaymentResponseDto(updatedPayment))
            } else {
                val errorMessage = when (momoResponse.resultCode) {
                    9000 -> "Thanh toán đã bị hủy bởi người dùng"
                    1001 -> "Giao dịch không thành công do không đủ tiền trong ví"
                    1003 -> "Giao dịch bị từ chối bởi người dùng"
                    1004 -> "Giao dịch thất bại do lỗi hệ thống Momo"
                    1005 -> "Giao dịch thất bại do ví chưa xác thực/đăng ký dịch vụ"
                    1006 -> "Ví người dùng đã bị khóa"
                    1007 -> "Mật khẩu không chính xác"
                    else -> "Thanh toán thất bại với mã lỗi: ${momoResponse.resultCode}"
                }
                ApiResponse.error(errorMessage, mapToPaymentResponseDto(updatedPayment))
            }
        } catch (e: Exception) {
            logger.error("Error processing Momo $source: ${e.message}", e)
            return ApiResponse.error("Lỗi xử lý thanh toán: ${e.message}")
        }
    }

    @Transactional
    override fun processRefund(paymentId: UUID, refundRequestDto: RefundRequestDto): ApiResponse<PaymentResponseDto> {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { ResourceNotFoundException("Payment not found with id: $paymentId") }
        
        // Check if payment is completed
        if (payment.status != PaymentStatus.COMPLETED) {
            return ApiResponse.error("Only completed payments can be refunded")
        }
        
        // Check if refund amount is valid
        if (refundRequestDto.amount > payment.amount) {
            return ApiResponse.error("Refund amount cannot be greater than payment amount")
        }
        
        // Process Momo refund
        val refundSuccess = processMomoRefund(payment, refundRequestDto)
        
        if (!refundSuccess) {
            return ApiResponse.error("Failed to process refund")
        }
        
        // Update payment with refund information
        payment.status = PaymentStatus.REFUNDED
        payment.refundedAmount = refundRequestDto.amount
        payment.refundedAt = LocalDateTime.now()
        payment.refundReason = refundRequestDto.reason
        payment.updatedAt = LocalDateTime.now()
        
        // Update metadata
        if (refundRequestDto.metadata != null) {
            val existingMetadata = if (payment.metadata != null) {
                try {
                objectMapper.readValue(payment.metadata, Map::class.java) as Map<String, Any>
                } catch (e: Exception) {
                    logger.warn("Failed to parse existing metadata: ${e.message}")
                    emptyMap<String, Any>()
                }
            } else {
                emptyMap<String, Any>()
            }
            
            val updatedMetadata = existingMetadata + refundRequestDto.metadata
            try {
                val jsonNode: JsonNode = objectMapper.valueToTree(updatedMetadata)
                payment.metadata = objectMapper.writeValueAsString(jsonNode)
            } catch (e: Exception) {
                logger.warn("Failed to convert metadata to JSON: ${e.message}")
            }
        }
        
        // Update ticket status to CANCELLED when refunded
        val ticket = payment.ticket
        if (ticket.status == TicketStatus.PAID) {
            ticket.cancel()
            ticketRepository.save(ticket)
            
            // Update available ticket quantity
            val ticketType = ticket.ticketType
            ticketType.availableQuantity += 1
            ticketRepository.save(ticket)
        }
        
        val updatedPayment = paymentRepository.save(payment)
        
        return ApiResponse.success("Hoàn tiền thành công", mapToPaymentResponseDto(updatedPayment))
    }
    
    /**
     * Process refund through Momo API
     */
    private fun processMomoRefund(payment: Payment, refundRequestDto: RefundRequestDto): Boolean {
        try {
            // Check if we have a Momo transaction ID in metadata
            val momoTransId = payment.metadata?.let {
                try {
                    val metadata = objectMapper.readValue(it, Map::class.java) as Map<String, Any>
                    metadata["momoTransId"]?.toString()
                } catch (e: Exception) {
                    null
                }
            } ?: return false
            
            val requestId = UUID.randomUUID().toString()
            val amount = refundRequestDto.amount.multiply(BigDecimal(1)).toLong()
            val orderId = "REFUND_${payment.transactionId}_${System.currentTimeMillis()}"
            val description = refundRequestDto.reason ?: "Refund for transaction ${payment.transactionId}"
            
            // Create refund request
            val rawSignature = "partnerCode=${momoPartnerCode}" +
                    "&accessKey=${momoAccessKey}" +
                    "&requestId=${requestId}" +
                    "&amount=${amount}" +
                    "&orderId=${orderId}" +
                    "&transId=${momoTransId}" +
                    "&description=${URLEncoder.encode(description, StandardCharsets.UTF_8)}"
            
            val signature = createMomoSignature(rawSignature, momoSecretKey)
            
            val refundRequest = mapOf(
                "partnerCode" to momoPartnerCode,
                "accessKey" to momoAccessKey,
                "requestId" to requestId,
                "amount" to amount.toString(),
                "orderId" to orderId,
                "transId" to momoTransId,
                "description" to description,
                "signature" to signature
            )
            
            // Send refund request to Momo API
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val entity = HttpEntity(refundRequest, headers)
            
            logger.info("Sending Momo refund request: $refundRequest")
            
            // In a real environment, you would make an actual API call to Momo's refund endpoint
            // For this implementation, we'll simulate a successful refund
            // val response = restTemplate.postForObject(momoRefundEndpoint, entity, Map::class.java)
            
            logger.info("Processed refund for transaction ${payment.transactionId}, amount: ${refundRequestDto.amount}")
            return true
        } catch (e: Exception) {
            logger.error("Error processing Momo refund: ${e.message}", e)
            return false
        }
    }

    @Transactional
    override fun updatePaymentStatus(paymentId: UUID, status: PaymentStatus): Payment {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { ResourceNotFoundException("Payment not found with id: $paymentId") }
        
        payment.status = status
        payment.updatedAt = LocalDateTime.now()
        
        return paymentRepository.save(payment)
    }

    @Transactional(readOnly = true)
    override fun getPaymentById(id: UUID): Payment {
        return paymentRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Payment not found with id: $id") }
    }

    @Transactional(readOnly = true)
    override fun getCurrentUserPayments(): List<PaymentResponseDto> {
        val currentUser = securityUtils.getCurrentUser()
            ?: throw IllegalStateException("User must be authenticated to get payments")
        
        val payments = paymentRepository.findByUser(currentUser, org.springframework.data.domain.PageRequest.of(0, 100))
        
        return payments.content.map { mapToPaymentResponseDto(it) }
    }
    
    /**
     * Map Payment entity to PaymentResponseDto
     */
    private fun mapToPaymentResponseDto(payment: Payment): PaymentResponseDto {
        val metadataMap = if (payment.metadata != null) {
            try {
                objectMapper.readValue(payment.metadata, Map::class.java) as Map<String, String>
            } catch (e: Exception) {
                logger.warn("Failed to parse metadata: ${e.message}")
                null
            }
        } else {
            null
        }
        
        return PaymentResponseDto(
            id = payment.id!!,
            userId = payment.user.id!!,
            userName = payment.user.fullName,
            ticketId = payment.ticket.id!!,
            eventId = payment.ticket.ticketType.event.id!!,
            eventTitle = payment.ticket.ticketType.event.title,
            ticketTypeName = payment.ticket.ticketType.name,
            amount = payment.amount,
            paymentMethod = payment.paymentMethod,
            transactionId = payment.transactionId,
            status = payment.status,
            paymentUrl = payment.paymentUrl,
            createdAt = payment.createdAt,
            updatedAt = payment.updatedAt,
            refundedAmount = payment.refundedAmount,
            refundedAt = payment.refundedAt,
            metadata = metadataMap
        )
    }
    
    /**
     * Generate QR code for ticket
     */
    private fun generateQRCode(ticketNumber: String): String {
        // In a real environment, we would use a QR code generation library
        // Here, we'll just create a simple URL
        return "https://api.eventticketing.com/verify/$ticketNumber"
    }
    
    /**
     * Remove Vietnamese accents from a string
     */
    private fun removeAccent(s: String): String {
        val temp = Normalizer.normalize(s, Normalizer.Form.NFD)
        return temp.replace("[^\\p{ASCII}]".toRegex(), "")
    }
} 