package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.dto.payment.PaymentCreateDto
import com.eventticketing.backend.dto.payment.PaymentResponseDto
import com.eventticketing.backend.dto.payment.RefundRequestDto
import com.eventticketing.backend.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api")
@Tag(name = "Payment", description = "Payment API")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping("/payments/create")
    @Operation(summary = "Create payment", description = "Create a new payment transaction")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('USER')")
    fun createPayment(@Valid @RequestBody paymentCreateDto: PaymentCreateDto): ResponseEntity<PaymentResponseDto> {
        val paymentResponse = paymentService.createPayment(paymentCreateDto)
        return ResponseEntity.ok(paymentResponse)
    }

    @PostMapping("/payments/vnpay-return")
    @Operation(summary = "Process VNPay return", description = "Process VNPay payment return callback")
    fun processVnPayReturn(request: HttpServletRequest): ResponseEntity<ApiResponse<PaymentResponseDto>> {
        val params = request.parameterMap.mapValues { it.value[0] }
        val response = paymentService.processVnPayReturn(params)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/payments/vnpay-ipn")
    @Operation(summary = "Process VNPay IPN", description = "Process VNPay Instant Payment Notification")
    fun processVnPayIpn(request: HttpServletRequest): ResponseEntity<ApiResponse<String>> {
        val params = request.parameterMap.mapValues { it.value[0] }
        val response = paymentService.processVnPayIpn(params)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/payments/stripe-webhook")
    @Operation(summary = "Process Stripe webhook", description = "Process Stripe webhook events")
    fun processStripeWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") signature: String
    ): ResponseEntity<ApiResponse<String>> {
        val response = paymentService.processStripeWebhook(payload, signature)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/users/me/payments")
    @Operation(summary = "Get current user payments", description = "Get payments for current user")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('USER')")
    fun getCurrentUserPayments(): ResponseEntity<List<PaymentResponseDto>> {
        val payments = paymentService.getCurrentUserPayments()
        return ResponseEntity.ok(payments)
    }

    @PostMapping("/payments/{id}/refund")
    @Operation(summary = "Process refund", description = "Process refund request")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('USER')")
    fun processRefund(
        @PathVariable id: UUID,
        @Valid @RequestBody refundRequestDto: RefundRequestDto
    ): ResponseEntity<ApiResponse<PaymentResponseDto>> {
        val response = paymentService.processRefund(id, refundRequestDto)
        return ResponseEntity.ok(response)
    }
} 