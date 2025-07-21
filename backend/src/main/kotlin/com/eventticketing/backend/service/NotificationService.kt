package com.eventticketing.backend.service

import java.util.*

interface NotificationService {
    
    /**
     * Gửi thông báo kích hoạt tài khoản
     */
    fun sendAccountActivation(userId: UUID, email: String, name: String, token: String)
    
    /**
     * Gửi thông báo đặt lại mật khẩu
     */
    fun sendPasswordReset(userId: UUID, email: String, name: String, token: String)
    
    /**
     * Gửi thông báo xác nhận mua vé
     */
    fun sendTicketConfirmation(userId: UUID, email: String, name: String, eventId: UUID, eventName: String,
                              eventDate: String, eventLocation: String, ticketId: UUID, ticketType: String,
                              ticketPrice: String, qrCodeData: String)
    
    /**
     * Gửi thông báo nhắc nhở sự kiện
     */
    fun sendEventReminder(userId: UUID, email: String, name: String, eventId: UUID, eventName: String,
                         eventDate: String, eventLocation: String, ticketId: UUID, hoursLeft: Int)
    
    /**
     * Gửi thông báo cho ban tổ chức khi có người mua vé
     */
    fun sendOrganizerTicketPurchaseNotification(organizerId: UUID, email: String, organizerName: String,
                                              eventId: UUID, eventName: String, ticketType: String,
                                              buyerName: String, purchaseDate: String)
    
    /**
     * Gửi thông báo bình luận mới
     */
    fun sendNewCommentNotification(userId: UUID, email: String, name: String, eventId: UUID,
                                 eventName: String, commenterName: String, commentContent: String)
    
    /**
     * Gửi thông báo đánh giá mới
     */
    fun sendNewRatingNotification(userId: UUID, email: String, name: String, eventId: UUID,
                                eventName: String, raterName: String, rating: Int, review: String?)
                                
    /**
     * Gửi thông báo hệ thống tổng quát
     */
    fun sendSystemNotification(userId: UUID, email: String, name: String, 
                             subject: String, message: String,
                             referenceId: UUID? = null, referenceType: String? = null,
                             buttonUrl: String? = null, buttonText: String? = null)
} 