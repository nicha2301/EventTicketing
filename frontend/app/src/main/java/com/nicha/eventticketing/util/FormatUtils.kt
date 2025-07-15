package com.nicha.eventticketing.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lớp tiện ích chứa các hàm định dạng chung cho toàn ứng dụng
 */
object FormatUtils {
    
    /**
     * Định dạng giá tiền theo chuẩn Việt Nam
     * @param price Giá tiền cần định dạng
     * @param includeCurrency Có thêm đơn vị tiền tệ (VNĐ) hay không
     * @return Chuỗi giá tiền đã định dạng
     */
    fun formatPrice(price: Double?, includeCurrency: Boolean = true): String {
        if (price == null) return "Chưa có giá"
        
        val formatter = DecimalFormat("#,###")
        formatter.decimalFormatSymbols = DecimalFormatSymbols(Locale("vi", "VN"))
        return formatter.format(price) + (if (includeCurrency) " VNĐ" else "")
    }
    
    /**
     * Định dạng giá tiền cho sự kiện, xử lý cả trường hợp miễn phí
     * @param price Giá tiền cần định dạng
     * @param isFree Sự kiện có miễn phí không
     * @param prefix Tiền tố (ví dụ: "Từ ")
     * @return Chuỗi giá tiền đã định dạng
     */
    fun formatEventPrice(price: Double?, isFree: Boolean, prefix: String = ""): String {
        return when {
            isFree -> "Miễn phí"
            price != null -> prefix + formatPrice(price)
            else -> "Chưa có giá"
        }
    }
    
    /**
     * Định dạng ngày tháng từ chuỗi ISO
     * @param dateString Chuỗi ngày tháng cần định dạng (yyyy-MM-dd'T'HH:mm:ss hoặc yyyy-MM-dd HH:mm:ss)
     * @param outputPattern Mẫu đầu ra (mặc định: dd/MM/yyyy)
     * @return Chuỗi ngày tháng đã định dạng
     */
    fun formatDate(dateString: String?, outputPattern: String = "dd/MM/yyyy"): String {
        if (dateString.isNullOrEmpty()) return ""
        
        return try {
            val inputFormat = if (dateString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            }
            val outputFormat = SimpleDateFormat(outputPattern, Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString.split("T")[0].replace("-", "/")
        } catch (e: Exception) {
            dateString.split("T")[0].replace("-", "/")
        }
    }
    
    /**
     * Định dạng ngày tháng từ đối tượng Date
     * @param date Đối tượng Date cần định dạng
     * @param pattern Mẫu đầu ra (mặc định: dd/MM/yyyy)
     * @return Chuỗi ngày tháng đã định dạng
     */
    fun formatDate(date: Date?, pattern: String = "dd/MM/yyyy"): String {
        if (date == null) return ""
        
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Phân tích chuỗi ngày tháng thành đối tượng Date
     * @param dateString Chuỗi ngày tháng cần phân tích
     * @param pattern Mẫu đầu vào (mặc định: yyyy-MM-dd)
     * @return Đối tượng Date hoặc null nếu không thể phân tích
     */
    fun parseDate(dateString: String?, pattern: String = "yyyy-MM-dd"): Date? {
        if (dateString.isNullOrEmpty()) return null
        
        return try {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            formatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
} 