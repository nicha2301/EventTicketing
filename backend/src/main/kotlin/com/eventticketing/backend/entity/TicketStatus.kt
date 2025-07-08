package com.eventticketing.backend.entity

enum class TicketStatus {
    RESERVED,       // Đã đặt chỗ nhưng chưa thanh toán
    PAID,           // Đã thanh toán
    CHECKED_IN,     // Đã check-in tại sự kiện
    CANCELLED,      // Đã hủy
    EXPIRED         // Hết hạn (không thanh toán đúng hạn)
} 