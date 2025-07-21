package com.eventticketing.backend.scheduler

import com.eventticketing.backend.entity.EventStatus
import com.eventticketing.backend.entity.NotificationType
import com.eventticketing.backend.entity.TicketStatus
import com.eventticketing.backend.repository.EventRepository
import com.eventticketing.backend.repository.TicketRepository
import com.eventticketing.backend.service.NotificationService
import com.eventticketing.backend.service.UserNotificationService
import com.eventticketing.backend.dto.notification.NotificationDto
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class EventCompletionScheduler(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository,
    private val notificationService: NotificationService,
    private val userNotificationService: UserNotificationService
) {
    private val logger = LoggerFactory.getLogger(EventCompletionScheduler::class.java)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    
    // Chạy mỗi ngày lúc 1:00 AM
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    fun processCompletedEvents() {
        logger.info("Starting event completion scheduler")
        
        val now = LocalDateTime.now()
        
        try {
            // Tìm các sự kiện đã kết thúc nhưng chưa được đánh dấu là COMPLETED
            val completedEvents = eventRepository.findByStatusAndEndDateBefore(
                EventStatus.PUBLISHED, now
            )
            
            logger.info("Found ${completedEvents.size} events that have ended")
            
            for (event in completedEvents) {
                // Cập nhật trạng thái sự kiện
                event.status = EventStatus.COMPLETED
                eventRepository.save(event)
                
                logger.info("Event ${event.title} marked as COMPLETED")
                
                // Tìm tất cả vé đã được thanh toán cho sự kiện này
                val tickets = ticketRepository.findByEventIdAndStatus(event.id!!, TicketStatus.PAID)
                
                // Gửi thông báo cảm ơn cho người tham dự
                for (ticket in tickets) {
                    val user = ticket.user
                    
                    // Tạo thông báo trong ứng dụng
                    val notificationDto = NotificationDto(
                        userId = user.id,
                        title = "Cảm ơn bạn đã tham gia sự kiện",
                        content = "Cảm ơn bạn đã tham gia sự kiện ${event.title}. Hãy để lại đánh giá để giúp chúng tôi cải thiện trong tương lai.",
                        notificationType = NotificationType.SYSTEM,
                        referenceId = event.id,
                        referenceType = "EVENT"
                    )
                    
                    userNotificationService.createNotification(notificationDto)
                    
                    logger.info("Thank you notification sent to user: ${user.id}")
                }
                
                // Thông báo cho ban tổ chức
                val organizer = event.organizer
                val notificationDto = NotificationDto(
                    userId = organizer.id,
                    title = "Sự kiện đã kết thúc",
                    content = "Sự kiện ${event.title} của bạn đã kết thúc. Bạn có thể xem các thống kê và đánh giá về sự kiện trong dashboard.",
                    notificationType = NotificationType.SYSTEM,
                    referenceId = event.id,
                    referenceType = "EVENT"
                )
                
                userNotificationService.createNotification(notificationDto)
                
                logger.info("Event completion notification sent to organizer: ${organizer.id}")
            }
            
            logger.info("Event completion scheduler completed successfully")
        } catch (e: Exception) {
            logger.error("Error in event completion scheduler: ${e.message}", e)
        }
    }
} 