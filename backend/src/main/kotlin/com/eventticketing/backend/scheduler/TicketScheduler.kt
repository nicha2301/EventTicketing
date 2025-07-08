package com.eventticketing.backend.scheduler

import com.eventticketing.backend.service.TicketService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TicketScheduler(
    private val ticketService: TicketService
) {
    private val logger = LoggerFactory.getLogger(TicketScheduler::class.java)

    /**
     * Xử lý các vé đặt chỗ đã hết hạn mỗi 5 phút
     * Vé đặt chỗ sẽ hết hạn sau 15 phút nếu không thanh toán
     */
    @Scheduled(fixedRate = 300000) // 5 phút = 300,000 ms
    fun processExpiredReservations() {
        logger.info("Bắt đầu xử lý các vé đặt chỗ đã hết hạn")
        try {
            val expiredCount = ticketService.processExpiredReservations()
            logger.info("Đã xử lý $expiredCount vé đặt chỗ đã hết hạn")
        } catch (e: Exception) {
            logger.error("Lỗi khi xử lý các vé đặt chỗ đã hết hạn", e)
        }
    }
} 