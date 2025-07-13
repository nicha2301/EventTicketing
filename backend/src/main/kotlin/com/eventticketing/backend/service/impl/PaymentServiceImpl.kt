package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.dto.payment.PaymentCreateDto
import com.eventticketing.backend.dto.payment.PaymentResponseDto
import com.eventticketing.backend.dto.payment.RefundRequestDto
import com.eventticketing.backend.entity.Payment
import com.eventticketing.backend.entity.PaymentStatus
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.exception.PaymentException
import com.eventticketing.backend.repository.PaymentRepository
import com.eventticketing.backend.repository.TicketRepository
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.service.PaymentService
import com.eventticketing.backend.util.SecurityUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val ticketRepository: TicketRepository,
    private val userRepository: UserRepository,
    private val securityUtils: SecurityUtils,
    private val objectMapper: ObjectMapper,

    @Value("\${payment.vnpay.return-url}")
    private val vnpayReturnUrl: String,

    @Value("\${payment.vnpay.tmn-code}")
    private val vnpayTmnCode: String,

    @Value("\${payment.vnpay.hash-secret}")
    private val vnpayHashSecret: String,

    @Value("\${payment.stripe.secret-key}")
    private val stripeSecretKey: String,

    @Value("\${payment.stripe.webhook-secret}")
    private val stripeWebhookSecret: String
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
                paymentMethod = paymentCreateDto.paymentMethod
            )
        }
        
        // Update payment if it exists
        if (existingPayment.isPresent) {
            payment.amount = paymentCreateDto.amount
            payment.paymentMethod = paymentCreateDto.paymentMethod
            payment.updatedAt = LocalDateTime.now()
        }
        
        // Set description and metadata if provided
        payment.description = paymentCreateDto.description
        if (paymentCreateDto.metadata != null) {
            payment.metadata = objectMapper.writeValueAsString(paymentCreateDto.metadata)
        }
        
        // Generate payment URL based on payment method
        val paymentUrl = when (paymentCreateDto.paymentMethod.lowercase()) {
            "vnpay" -> createVnPayUrl(payment, paymentCreateDto.returnUrl)
            "stripe" -> createStripeCheckoutSession(payment, paymentCreateDto.returnUrl)
            else -> throw PaymentException("Unsupported payment method: ${paymentCreateDto.paymentMethod}")
        }
        
        payment.paymentUrl = paymentUrl
        payment.status = PaymentStatus.PENDING
        
        val savedPayment = paymentRepository.save(payment)
        
        return mapToPaymentResponseDto(savedPayment)
    }

    @Transactional
    override fun processVnPayReturn(params: Map<String, String>): ApiResponse<PaymentResponseDto> {
        // Validate VNPay response
        if (!validateVnPayResponse(params)) {
            return ApiResponse.error("Invalid VNPay response")
        }
        
        val vnpTxnRef = params["vnp_TxnRef"] ?: return ApiResponse.error("Transaction reference not found")
        val vnpResponseCode = params["vnp_ResponseCode"]
        val vnpTransactionStatus = params["vnp_TransactionStatus"]
        
        // Find payment by transaction ID
        val payment = paymentRepository.findByTransactionId(vnpTxnRef)
            .orElseThrow { ResourceNotFoundException("Payment not found with transaction ID: $vnpTxnRef") }
        
        // Update payment status based on VNPay response
        payment.status = when {
            vnpResponseCode == "00" && vnpTransactionStatus == "00" -> PaymentStatus.COMPLETED
            else -> PaymentStatus.FAILED
        }
        
        payment.updatedAt = LocalDateTime.now()
        val updatedPayment = paymentRepository.save(payment)
        
        return if (updatedPayment.status == PaymentStatus.COMPLETED) {
            ApiResponse.success(mapToPaymentResponseDto(updatedPayment))
        } else {
            ApiResponse.error("Payment failed", mapToPaymentResponseDto(updatedPayment))
        }
    }

    @Transactional
    override fun processVnPayIpn(params: Map<String, String>): ApiResponse<String> {
        // Validate VNPay IPN
        if (!validateVnPayResponse(params)) {
            return ApiResponse.error("Invalid VNPay IPN")
        }
        
        val vnpTxnRef = params["vnp_TxnRef"] ?: return ApiResponse.error("Transaction reference not found")
        val vnpResponseCode = params["vnp_ResponseCode"]
        val vnpTransactionStatus = params["vnp_TransactionStatus"]
        
        // Find payment by transaction ID
        val payment = paymentRepository.findByTransactionId(vnpTxnRef)
            .orElseThrow { ResourceNotFoundException("Payment not found with transaction ID: $vnpTxnRef") }
        
        // Update payment status based on VNPay response
        payment.status = when {
            vnpResponseCode == "00" && vnpTransactionStatus == "00" -> PaymentStatus.COMPLETED
            else -> PaymentStatus.FAILED
        }
        
        payment.updatedAt = LocalDateTime.now()
        paymentRepository.save(payment)
        
        return ApiResponse.success("IPN processed successfully")
    }

    @Transactional
    override fun processStripeWebhook(payload: String, signature: String): ApiResponse<String> {
        try {
            // In a real implementation, we would use Stripe's library to verify the signature
            // and process the webhook event
            
            // For now, just log the webhook and return success
            logger.info("Received Stripe webhook: $payload")
            
            // Parse the event type from the payload (simplified)
            val eventType = extractEventTypeFromPayload(payload)
            val paymentIntent = extractPaymentIntentFromPayload(payload)
            
            when (eventType) {
                "payment_intent.succeeded" -> {
                    // Find payment by Stripe payment intent ID
                    val payment = paymentRepository.findByTransactionId(paymentIntent)
                        .orElse(null)
                    
                    if (payment != null) {
                        payment.status = PaymentStatus.COMPLETED
                        payment.updatedAt = LocalDateTime.now()
                        paymentRepository.save(payment)
                    }
                }
                "payment_intent.payment_failed" -> {
                    // Find payment by Stripe payment intent ID
                    val payment = paymentRepository.findByTransactionId(paymentIntent)
                        .orElse(null)
                    
                    if (payment != null) {
                        payment.status = PaymentStatus.FAILED
                        payment.updatedAt = LocalDateTime.now()
                        paymentRepository.save(payment)
                    }
                }
            }
            
            return ApiResponse.success("Webhook processed successfully")
        } catch (e: Exception) {
            logger.error("Error processing Stripe webhook", e)
            return ApiResponse.error("Error processing Stripe webhook: ${e.message}")
        }
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
        
        // Process refund based on payment method
        val refundSuccess = when (payment.paymentMethod.lowercase()) {
            "vnpay" -> processVnPayRefund(payment, refundRequestDto)
            "stripe" -> processStripeRefund(payment, refundRequestDto)
            else -> false
        }
        
        if (!refundSuccess) {
            return ApiResponse.error("Failed to process refund")
        }
        
        // Update payment with refund information
        payment.status = PaymentStatus.REFUNDED
        payment.refundedAmount = refundRequestDto.amount
        payment.refundedAt = LocalDateTime.now()
        payment.refundReason = refundRequestDto.reason
        payment.updatedAt = LocalDateTime.now()
        
        if (refundRequestDto.metadata != null) {
            val existingMetadata = if (payment.metadata != null) {
                objectMapper.readValue(payment.metadata, Map::class.java) as Map<String, Any>
            } else {
                emptyMap()
            }
            
            val updatedMetadata = existingMetadata + refundRequestDto.metadata
            payment.metadata = objectMapper.writeValueAsString(updatedMetadata)
        }
        
        val updatedPayment = paymentRepository.save(payment)
        
        return ApiResponse.success(mapToPaymentResponseDto(updatedPayment))
    }

    @Transactional
    override fun updatePaymentStatus(paymentId: UUID, status: PaymentStatus): Payment {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { ResourceNotFoundException("Payment not found with id: $paymentId") }
        
        payment.status = status
        payment.updatedAt = LocalDateTime.now()
        
        return paymentRepository.save(payment)
    }

    // Helper methods
    
    private fun createVnPayUrl(payment: Payment, returnUrl: String): String {
        // In a real implementation, we would use VNPay's library to create the payment URL
        // For now, return a dummy URL
        val transactionId = "VNP${System.currentTimeMillis()}"
        payment.transactionId = transactionId
        
        return "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=${payment.amount}&vnp_TxnRef=$transactionId"
    }
    
    private fun createStripeCheckoutSession(payment: Payment, returnUrl: String): String {
        // In a real implementation, we would use Stripe's library to create a checkout session
        // For now, return a dummy URL
        val transactionId = "STRIPE${System.currentTimeMillis()}"
        payment.transactionId = transactionId
        
        return "https://checkout.stripe.com/pay/$transactionId"
    }
    
    private fun validateVnPayResponse(params: Map<String, String>): Boolean {
        // In a real implementation, we would validate the VNPay response signature
        // For now, return true
        return true
    }
    
    private fun processVnPayRefund(payment: Payment, refundRequestDto: RefundRequestDto): Boolean {
        // In a real implementation, we would call VNPay's refund API
        // For now, return true
        return true
    }
    
    private fun processStripeRefund(payment: Payment, refundRequestDto: RefundRequestDto): Boolean {
        // In a real implementation, we would use Stripe's library to process the refund
        // For now, return true
        return true
    }
    
    private fun extractEventTypeFromPayload(payload: String): String {
        // In a real implementation, we would parse the JSON payload
        // For now, return a dummy event type
        return "payment_intent.succeeded"
    }
    
    private fun extractPaymentIntentFromPayload(payload: String): String {
        // In a real implementation, we would parse the JSON payload
        // For now, return a dummy payment intent ID
        return "pi_123456789"
    }
    
    private fun mapToPaymentResponseDto(payment: Payment): PaymentResponseDto {
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
            metadata = if (payment.metadata != null) {
                objectMapper.readValue(payment.metadata, Map::class.java) as Map<String, String>
            } else {
                null
            }
        )
    }
} 