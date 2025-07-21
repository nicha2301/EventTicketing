package com.eventticketing.backend.scheduler

import com.eventticketing.backend.entity.EventStatus
import com.eventticketing.backend.entity.TicketStatus
import com.eventticketing.backend.repository.EventRepository
import com.eventticketing.backend.repository.TicketRepository
import com.eventticketing.backend.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class EventReminderScheduler(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(EventReminderScheduler::class.java)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    
    // Chạy mỗi ngày lúc 9:00 AM
    @Scheduled(cron = "0 0 9 * * *")
    fun sendEventReminders() {
        logger.info("Starting event reminder scheduler")
        
        val now = LocalDateTime.now()
        val nextDay = now.plusHours(24)
        
        try {
            // Tìm các sự kiện sắp diễn ra trong vòng 24 giờ tới
            val upcomingEvents = eventRepository.findByStatusAndStartDateBetween(
                EventStatus.PUBLISHED, now, nextDay
            )
            
            logger.info("Found ${upcomingEvents.size} upcoming events in the next 24 hours")
            
            for (event in upcomingEvents) {
                val hoursLeft = Duration.between(now, event.startDate).toHours().toInt()
                
                // Tìm tất cả vé đã được mua cho sự kiện này
                val tickets = ticketRepository.findByEventIdAndStatus(
                    event.id!!, TicketStatus.PAID
                )
                
                logger.info("Found ${tickets.size} tickets for event: ${event.title}")
                
                for (ticket in tickets) {
                    val user = ticket.user
                    val eventDateStr = event.startDate.format(dateFormatter)
                    
                    // Gửi thông báo nhắc nhở cho người dùng
                    notificationService.sendEventReminder(
                        userId = user.id!!,
                        email = user.email,
                        name = user.fullName,
                        eventId = event.id!!,
                        eventName = event.title,
                        eventDate = eventDateStr,
                        eventLocation = event.address + ", " + event.city,
                        ticketId = ticket.id!!,
                        hoursLeft = hoursLeft
                    )
                }
            }
            
            logger.info("Event reminder scheduler completed successfully")
        } catch (e: Exception) {
            logger.error("Error in event reminder scheduler: ${e.message}", e)
        }
    }
} 