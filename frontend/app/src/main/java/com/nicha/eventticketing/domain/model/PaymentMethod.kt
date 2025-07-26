package com.nicha.eventticketing.domain.model

import androidx.annotation.DrawableRes
import com.nicha.eventticketing.R

/**
 * Enum class cho các phương thức thanh toán
 */
enum class PaymentMethod(
    val code: String,
    val displayName: String,
    @DrawableRes val iconRes: Int? = null
) {
    MOMO("momo", "Ví MoMo", R.drawable.ic_momo),
    VNPAY("vnpay", "VNPay", R.drawable.ic_vnpay),
    BANK_TRANSFER("bank_transfer", "Chuyển khoản ngân hàng", null),
    CASH("cash", "Tiền mặt", null);
    
    companion object {
        fun fromCode(code: String): PaymentMethod? {
            return values().find { it.code == code }
        }
        
        fun fromString(method: String): PaymentMethod? {
            return try {
                valueOf(method.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}
