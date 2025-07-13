package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.ApiResponse
import com.eventticketing.backend.dto.payment.PaymentCreateDto
import com.eventticketing.backend.dto.payment.PaymentDto
import com.eventticketing.backend.dto.payment.PaymentResponseDto
import com.eventticketing.backend.service.PaymentService
import com.eventticketing.backend.util.SecurityUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService,
    private val securityUtils: SecurityUtils
) {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun createPayment(@RequestBody paymentCreateDto: PaymentCreateDto): ResponseEntity<ApiResponse<PaymentResponseDto>> {
        val currentUser = securityUtils.getCurrentUser()
            ?: return ResponseEntity.badRequest().body(ApiResponse.error<PaymentResponseDto>("Người dùng chưa đăng nhập"))
        
        // Không cần copy userId vì PaymentCreateDto không có trường userId
        val response = paymentService.processPayment(paymentCreateDto, currentUser.id!!)
        return ResponseEntity.ok(ApiResponse.success("Yêu cầu thanh toán thành công", response))
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getPayment(@PathVariable id: UUID): ResponseEntity<ApiResponse<PaymentDto>> {
        val currentUser = securityUtils.getCurrentUser()
            ?: return ResponseEntity.badRequest().body(ApiResponse.error<PaymentDto>("Người dùng chưa đăng nhập"))
        
        val payment = paymentService.getPaymentById(id)
        
        // Kiểm tra quyền truy cập
        if (payment.userId != currentUser.id && !securityUtils.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.error<PaymentDto>("Không có quyền truy cập"))
        }
        
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thanh toán thành công", payment))
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    fun getUserPayments(@PathVariable userId: UUID, @PageableDefault(size = 20) pageable: Pageable): ResponseEntity<ApiResponse<Page<PaymentDto>>> {
        val payments = paymentService.getPaymentsByUserId(userId, pageable)
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thanh toán thành công", payments))
    }

    @GetMapping("/my-payments")
    @PreAuthorize("isAuthenticated()")
    fun getMyPayments(@PageableDefault(size = 20) pageable: Pageable): ResponseEntity<ApiResponse<Page<PaymentDto>>> {
        val currentUser = securityUtils.getCurrentUser()
            ?: return ResponseEntity.badRequest().body(ApiResponse.error<Page<PaymentDto>>("Người dùng chưa đăng nhập"))
        
        val payments = paymentService.getPaymentsByUserId(currentUser.id!!, pageable)
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thanh toán thành công", payments))
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    fun cancelPayment(@PathVariable id: UUID): ResponseEntity<ApiResponse<Boolean>> {
        val currentUser = securityUtils.getCurrentUser()
            ?: return ResponseEntity.badRequest().body(ApiResponse.error<Boolean>("Người dùng chưa đăng nhập"))
        
        val payment = paymentService.getPaymentById(id)
        
        // Kiểm tra quyền truy cập
        if (payment.userId != currentUser.id && !securityUtils.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.error<Boolean>("Không có quyền truy cập"))
        }
        
        val result = paymentService.cancelPayment(id)
        return if (result) {
            ResponseEntity.ok(ApiResponse.success("Hủy thanh toán thành công", true))
        } else {
            ResponseEntity.badRequest().body(ApiResponse.error<Boolean>("Không thể hủy thanh toán"))
        }
    }

    @PostMapping("/vnpay-return")
    fun handleVnPayReturn(
        @RequestParam params: Map<String, String>
    ): ResponseEntity<String> {
        // Xử lý callback từ VNPay
        // Trong thực tế, cần phải có trang HTML để hiển thị kết quả thanh toán
        val paymentId = params["vnp_OrderInfo"]?.substringAfter("payment_id=")?.substringBefore("&")
        val transactionId = params["vnp_TransactionNo"]
        
        return if (paymentId != null && transactionId != null) {
            val result = paymentService.completePayment(UUID.fromString(paymentId), transactionId, params)
            if (result) {
                ResponseEntity.ok("Thanh toán thành công")
            } else {
                ResponseEntity.ok("Thanh toán thất bại")
            }
        } else {
            ResponseEntity.badRequest().body("Thiếu thông tin thanh toán")
        }
    }

    @PostMapping("/momo-return")
    fun handleMomoReturn(
        @RequestParam params: Map<String, String>
    ): ResponseEntity<String> {
        // Xử lý callback từ Momo
        val paymentId = params["orderId"]?.substringAfter("ORDER_")
        val transactionId = params["transId"]
        
        return if (paymentId != null && transactionId != null) {
            val result = paymentService.completePayment(UUID.fromString(paymentId), transactionId, params)
            if (result) {
                ResponseEntity.ok("Thanh toán thành công")
            } else {
                ResponseEntity.ok("Thanh toán thất bại")
            }
        } else {
            ResponseEntity.badRequest().body("Thiếu thông tin thanh toán")
        }
    }

    @PostMapping("/zalopay-return")
    fun handleZaloPayReturn(
        @RequestParam params: Map<String, String>
    ): ResponseEntity<String> {
        // Xử lý callback từ ZaloPay
        val paymentId = params["app_trans_id"]?.substringAfter("_")
        val transactionId = params["zp_trans_id"]
        
        return if (paymentId != null && transactionId != null) {
            val result = paymentService.completePayment(UUID.fromString(paymentId), transactionId, params)
            if (result) {
                ResponseEntity.ok("Thanh toán thành công")
            } else {
                ResponseEntity.ok("Thanh toán thất bại")
            }
        } else {
            ResponseEntity.badRequest().body("Thiếu thông tin thanh toán")
        }
    }

    @PostMapping("/momo-notify")
    fun handleMomoNotify(
        @RequestBody params: Map<String, String>
    ): ResponseEntity<String> {
        // Xử lý IPN từ Momo
        // Trong thực tế, cần phải kiểm tra chữ ký và xử lý theo quy định của Momo
        return ResponseEntity.ok("OK")
    }

    @PostMapping("/zalopay-notify")
    fun handleZaloPayNotify(
        @RequestBody params: Map<String, String>
    ): ResponseEntity<String> {
        // Xử lý IPN từ ZaloPay
        // Trong thực tế, cần phải kiểm tra chữ ký và xử lý theo quy định của ZaloPay
        return ResponseEntity.ok("OK")
    }
} 