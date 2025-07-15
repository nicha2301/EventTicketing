package com.nicha.eventticketing.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import timber.log.Timber
import java.util.EnumMap

/**
 * Tiện ích để tạo mã QR từ chuỗi dữ liệu
 */
object QRCodeGenerator {
    
    /**
     * Tạo mã QR từ chuỗi dữ liệu
     * @param content Nội dung cần mã hóa
     * @param size Kích thước mã QR (chiều rộng và chiều cao)
     * @param margin Lề xung quanh mã QR
     * @return Bitmap chứa mã QR hoặc null nếu có lỗi
     */
    fun generateQRCode(content: String, size: Int = 512, margin: Int = 2): Bitmap? {
        return try {
            // Tạo hints để cấu hình mã QR
            val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H // Mức độ sửa lỗi cao nhất
            hints[EncodeHintType.MARGIN] = margin
            
            // Tạo mã QR
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            
            // Chuyển đổi ma trận bit thành bitmap
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            
            // Đặt màu cho từng điểm ảnh
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            bitmap
        } catch (e: WriterException) {
            Timber.e(e, "Lỗi khi tạo mã QR: ${e.message}")
            null
        } catch (e: Exception) {
            Timber.e(e, "Lỗi không xác định khi tạo mã QR: ${e.message}")
            null
        }
    }
    
    /**
     * Tạo mã QR từ thông tin vé
     * @param ticketId ID của vé
     * @param ticketNumber Mã vé
     * @param eventId ID của sự kiện
     * @param userId ID của người dùng
     * @return Bitmap chứa mã QR hoặc null nếu có lỗi
     */
    fun generateTicketQRCode(
        ticketId: String,
        ticketNumber: String,
        eventId: String,
        userId: String
    ): Bitmap? {
        val content = "TICKET:$ticketId:$ticketNumber:$eventId:$userId"
        return generateQRCode(content)
    }
} 