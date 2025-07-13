package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.payment.PaymentCreateDto
import com.eventticketing.backend.dto.payment.PaymentDto
import com.eventticketing.backend.dto.payment.PaymentRequestDto
import com.eventticketing.backend.dto.payment.PaymentResponseDto
import com.eventticketing.backend.entity.Payment
import com.eventticketing.backend.entity.TicketStatus
import com.eventticketing.backend.entity.PaymentStatus
import com.eventticketing.backend.exception.PaymentException
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.messaging.dto.AnalyticsMessageDto
import com.eventticketing.backend.repository.PaymentRepository
import com.eventticketing.backend.repository.TicketRepository
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.service.MessageQueueService
import com.eventticketing.backend.service.PaymentService
import com.eventticketing.backend.service.payment.PaymentGatewayService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val ticketRepository: TicketRepository,
    private val userRepository: UserRepository,
    private val messageQueueService: MessageQueueService,
    private val paymentGateways: List<PaymentGatewayService>
) : PaymentService {

    private val logger = LoggerFactory.getLogger(PaymentServiceImpl::class.java)

    @Transactional
    override fun processPayment(paymentCreateDto: PaymentCreateDto, userId: UUID): PaymentResponseDto {
        // Tìm user
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy người dùng với ID $userId") }
        
        // Tìm vé
        val ticket = ticketRepository.findById(paymentCreateDto.ticketId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy vé với ID ${paymentCreateDto.ticketId}") }
        
        // Kiểm tra xem vé có thuộc về user không
        if (ticket.user?.id != userId) {
            throw PaymentException("Vé ${ticket.id} không thuộc về người dùng $userId")
        }
        
        if (ticket.status != TicketStatus.RESERVED) {
            throw PaymentException("Vé ${ticket.id} không ở trạng thái RESERVED")
        }
        
        // Tạo payment
        val payment = Payment(
            user = user,
            ticket = ticket,
            amount = paymentCreateDto.amount,
            paymentMethod = paymentCreateDto.paymentMethod,
            status = PaymentStatus.PENDING,
            createdAt = LocalDateTime.now()
        )
        
        val savedPayment = paymentRepository.save(payment)
        
        // Liên kết vé với payment
        ticket.paymentId = savedPayment.id
        ticketRepository.save(ticket)
        
        // Chọn payment gateway
        val paymentGateway = paymentGateways.find { it.getName().equals(paymentCreateDto.paymentMethod, ignoreCase = true) }
            ?: throw PaymentException("Không hỗ trợ phương thức thanh toán ${paymentCreateDto.paymentMethod}")
        
        // Tạo payment request
        val paymentRequest = PaymentRequestDto(
            amount = paymentCreateDto.amount,
            description = paymentCreateDto.description ?: "Thanh toán vé sự kiện",
            paymentMethod = paymentCreateDto.paymentMethod,
            returnUrl = paymentCreateDto.returnUrl,
            metadata = paymentCreateDto.metadata ?: emptyMap()
        )
        
        // Xử lý thanh toán
        return try {
            val response = paymentGateway.initiatePayment(paymentRequest)
            
            // Cập nhật transaction ID
            if (response.transactionId != null) {
                savedPayment.transactionId = response.transactionId
                paymentRepository.save(savedPayment)
            }
            
            response
        } catch (e: Exception) {
            // Cập nhật trạng thái payment thành FAILED
            savedPayment.status = PaymentStatus.FAILED
            paymentRepository.save(savedPayment)
            
            logger.error("Lỗi khi xử lý thanh toán", e)
            throw PaymentException("Lỗi khi xử lý thanh toán: ${e.message}")
        }
    }

    @Transactional
    override fun completePayment(paymentId: UUID, transactionId: String, params: Map<String, String>): Boolean {
        // Tìm payment
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy thanh toán với ID $paymentId") }
        
        // Kiểm tra trạng thái
        if (payment.status != PaymentStatus.PENDING) {
            logger.warn("Thanh toán ${payment.id} không ở trạng thái PENDING")
            return false
        }
        
        // Chọn payment gateway
        val paymentGateway = paymentGateways.find { it.getName().equals(payment.paymentMethod, ignoreCase = true) }
            ?: throw PaymentException("Không hỗ trợ phương thức thanh toán ${payment.paymentMethod}")
        
        // Xác thực thanh toán
        val isValid = paymentGateway.verifyPayment(params)
        if (!isValid) {
            logger.error("Xác thực thanh toán thất bại cho payment ${payment.id}")
            payment.status = PaymentStatus.FAILED
            paymentRepository.save(payment)
            return false
        }
        
        // Cập nhật payment
        payment.status = PaymentStatus.COMPLETED
        payment.transactionId = transactionId
        payment.updatedAt = LocalDateTime.now()
        paymentRepository.save(payment)
        
        // Cập nhật trạng thái vé
        val tickets = ticketRepository.findAllByPaymentId(payment.id!!)
        tickets.forEach { ticket ->
            ticket.status = TicketStatus.PAID
            ticketRepository.save(ticket)
        }
        
        // Gửi thông báo
        val analyticsData = mapOf(
            "paymentId" to payment.id.toString(),
            "userId" to payment.user.id.toString(),
            "amount" to payment.amount.toString(),
            "paymentMethod" to payment.paymentMethod,
            "transactionId" to payment.transactionId.toString()
        )
        messageQueueService.trackEvent(AnalyticsMessageDto.fromUserEvent(
            eventType = "payment.completed",
            userId = payment.user.id!!,
            data = analyticsData
        ))
        
        return true
    }

    @Transactional
    override fun cancelPayment(paymentId: UUID): Boolean {
        // Tìm payment
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy thanh toán với ID $paymentId") }
        
        // Kiểm tra trạng thái
        if (payment.status != PaymentStatus.PENDING) {
            logger.warn("Thanh toán ${payment.id} không ở trạng thái PENDING")
            return false
        }
        
        // Cập nhật payment
        payment.status = PaymentStatus.CANCELLED
        paymentRepository.save(payment)
        
        return true
    }

    override fun getPaymentById(paymentId: UUID): PaymentDto {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy thanh toán với ID $paymentId") }
        
        // Tìm các vé liên quan
        val tickets = ticketRepository.findAllByPaymentId(paymentId)
        
        return PaymentDto(
            id = payment.id,
            userId = payment.user.id!!,
            amount = payment.amount,
            paymentMethod = payment.paymentMethod,
            transactionId = payment.transactionId,
            status = payment.status.toString(),
            createdAt = payment.createdAt,
            updatedAt = payment.updatedAt,
            ticketIds = tickets.mapNotNull { it.id }
        )
    }

    override fun getPaymentsByUserId(userId: UUID, pageable: Pageable): Page<PaymentDto> {
        val payments = paymentRepository.findByUser_Id(userId, pageable)
        
        return payments.map { payment ->
            val tickets = ticketRepository.findAllByPaymentId(payment.id!!)
            
            PaymentDto(
                id = payment.id,
                userId = payment.user.id!!,
                amount = payment.amount,
                paymentMethod = payment.paymentMethod,
                transactionId = payment.transactionId,
                status = payment.status.toString(),
                createdAt = payment.createdAt,
                updatedAt = payment.updatedAt,
                ticketIds = tickets.mapNotNull { it.id }
            )
        }
    }
} 