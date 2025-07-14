package com.nicha.eventticketing.util

/**
 * Lớp tiện ích chứa các hàm validation chung cho toàn ứng dụng
 */
object ValidationUtils {
    
    /**
     * Kiểm tra tính hợp lệ của email
     * @param email Email cần kiểm tra
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email không được để trống"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email không hợp lệ"
            else -> null
        }
    }
    
    /**
     * Kiểm tra tính hợp lệ của mật khẩu
     * @param password Mật khẩu cần kiểm tra
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Mật khẩu không được để trống"
            password.length < 6 -> "Mật khẩu phải có ít nhất 6 ký tự"
            else -> null
        }
    }
    
    /**
     * Kiểm tra tính hợp lệ của số điện thoại
     * @param phoneNumber Số điện thoại cần kiểm tra
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    fun validatePhoneNumber(phoneNumber: String): String? {
        return when {
            phoneNumber.isBlank() -> "Số điện thoại không được để trống"
            !phoneNumber.matches(Regex("^[0-9]{10,11}$")) -> "Số điện thoại không hợp lệ"
            else -> null
        }
    }
    
    /**
     * Kiểm tra tính hợp lệ của họ tên
     * @param fullName Họ tên cần kiểm tra
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    fun validateFullName(fullName: String): String? {
        return when {
            fullName.isBlank() -> "Họ và tên không được để trống"
            fullName.length < 3 -> "Họ và tên phải có ít nhất 3 ký tự"
            else -> null
        }
    }
    
    /**
     * Kiểm tra xác nhận mật khẩu
     * @param confirmPassword Mật khẩu xác nhận
     * @param password Mật khẩu gốc
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    fun validateConfirmPassword(confirmPassword: String, password: String): String? {
        return when {
            confirmPassword.isBlank() -> "Xác nhận mật khẩu không được để trống"
            confirmPassword != password -> "Mật khẩu xác nhận không khớp"
            else -> null
        }
    }
} 