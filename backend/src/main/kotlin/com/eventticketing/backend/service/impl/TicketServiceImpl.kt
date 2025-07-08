package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.TicketDto
import com.eventticketing.backend.dto.TicketPurchaseDto
import com.eventticketing.backend.dto.TicketPurchaseResponseDto
import com.eventticketing.backend.entity.Event
import com.eventticketing.backend.entity.Ticket
import com.eventticketing.backend.entity.TicketStatus
import com.eventticketing.backend.entity.User
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.exception.TicketException
import com.eventticketing.backend.repository.EventRepository
import com.eventticketing.backend.repository.TicketRepository
import com.eventticketing.backend.repository.TicketTypeRepository
import com.eventticketing.backend.repository.UserRepository
import com.eventticketing.backend.service.TicketService
import com.eventticketing.backend.util.SecurityUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import com.eventticketing.backend.entity.EventStatus

@Service
class TicketServiceImpl(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository,
    private val ticketTypeRepository: TicketTypeRepository,
    private val userRepository: UserRepository,
    private val securityUtils: SecurityUtils
) : TicketService {

    @Transactional
    override fun purchaseTickets(ticketPurchaseDto: TicketPurchaseDto): TicketPurchaseResponseDto {
        val currentUser = securityUtils.getCurrentUser()
            ?: throw TicketException("Người dùng chưa đăng nhập")

        val event = eventRepository.findById(ticketPurchaseDto.eventId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy sự kiện với ID: ${ticketPurchaseDto.eventId}") }

        if (event.startDate.isBefore(LocalDateTime.now())) {
            throw TicketException("Sự kiện đã diễn ra, không thể mua vé")
        }

        if (event.status == EventStatus.CANCELLED) {
            throw TicketException("Sự kiện đã bị hủy, không thể mua vé")
        }

        val orderId = UUID.randomUUID()
        var totalAmount = BigDecimal.ZERO
        val tickets = mutableListOf<Ticket>()
        val ticketDtos = mutableListOf<TicketDto>()

        // Xử lý từng loại vé trong đơn hàng
        for (item in ticketPurchaseDto.tickets) {
            val ticketType = ticketTypeRepository.findById(item.ticketTypeId)
                .orElseThrow { ResourceNotFoundException("Không tìm thấy loại vé với ID: ${item.ticketTypeId}") }

            // Kiểm tra số lượng vé còn lại
            val soldTickets = ticketRepository.countSoldTicketsByTicketTypeId(ticketType.id!!)
            if (ticketType.quantity - soldTickets < item.quantity) {
                throw TicketException("Loại vé ${ticketType.name} không đủ số lượng")
            }

            // Tạo vé cho mỗi số lượng
            for (i in 1..item.quantity) {
                val ticket = Ticket(
                    user = currentUser,
                    event = event,
                    ticketType = ticketType,
                    price = ticketType.price
                )
                ticket.generateTicketNumber()
                
                val savedTicket = ticketRepository.save(ticket)
                tickets.add(savedTicket)
                
                // Chuyển đổi thành DTO để trả về
                ticketDtos.add(convertToDto(savedTicket))
                
                // Cộng dồn tổng tiền
                totalAmount = totalAmount.add(ticketType.price)
            }
        }

        // Tạo đối tượng phản hồi
        return TicketPurchaseResponseDto(
            orderId = orderId,
            eventId = event.id!!,
            eventTitle = event.title,
            tickets = ticketDtos,
            totalAmount = totalAmount,
            paymentStatus = "PENDING",
            purchaseDate = LocalDateTime.now(),
            buyerName = ticketPurchaseDto.buyerName,
            buyerEmail = ticketPurchaseDto.buyerEmail,
            buyerPhone = ticketPurchaseDto.buyerPhone,
            promoCode = ticketPurchaseDto.promoCode
        )
    }

    override fun getTicketById(ticketId: UUID): TicketDto {
        val ticket = ticketRepository.findById(ticketId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy vé với ID: $ticketId") }
        
        // Kiểm tra quyền truy cập
        checkTicketAccess(ticket)
        
        return convertToDto(ticket)
    }

    override fun getTicketByNumber(ticketNumber: String): TicketDto {
        val ticket = ticketRepository.findByTicketNumber(ticketNumber)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy vé với mã: $ticketNumber") }
        
        // Kiểm tra quyền truy cập
        checkTicketAccess(ticket)
        
        return convertToDto(ticket)
    }

    override fun getTicketsByUserId(userId: UUID, pageable: Pageable): Page<TicketDto> {
        // Kiểm tra quyền truy cập
        val currentUser = securityUtils.getCurrentUser()
        if (currentUser == null || (currentUser.id != userId && !securityUtils.isAdmin())) {
            throw TicketException("Không có quyền xem vé của người dùng khác")
        }

        return ticketRepository.findByUserId(userId, pageable).map { convertToDto(it) }
    }

    override fun getTicketsByEventId(eventId: UUID, pageable: Pageable): Page<TicketDto> {
        // Chỉ admin mới có quyền xem tất cả vé của sự kiện
        if (!securityUtils.isAdmin()) {
            throw TicketException("Không có quyền xem tất cả vé của sự kiện")
        }

        return ticketRepository.findByEventId(eventId, pageable).map { convertToDto(it) }
    }

    override fun getTicketsByUserIdAndStatus(userId: UUID, status: TicketStatus, pageable: Pageable): Page<TicketDto> {
        // Kiểm tra quyền truy cập
        val currentUser = securityUtils.getCurrentUser()
        if (currentUser == null || (currentUser.id != userId && !securityUtils.isAdmin())) {
            throw TicketException("Không có quyền xem vé của người dùng khác")
        }

        return ticketRepository.findByUserIdAndStatus(userId, status, pageable).map { convertToDto(it) }
    }

    @Transactional
    override fun checkInTicket(ticketId: UUID): TicketDto {
        // Chỉ admin mới có quyền check-in vé
        if (!securityUtils.isAdmin()) {
            throw TicketException("Không có quyền check-in vé")
        }

        val ticket = ticketRepository.findById(ticketId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy vé với ID: $ticketId") }

        if (!ticket.checkIn()) {
            throw TicketException("Không thể check-in vé. Vé phải ở trạng thái đã thanh toán.")
        }

        return convertToDto(ticketRepository.save(ticket))
    }

    @Transactional
    override fun checkInTicketByNumber(ticketNumber: String): TicketDto {
        // Chỉ admin mới có quyền check-in vé
        if (!securityUtils.isAdmin()) {
            throw TicketException("Không có quyền check-in vé")
        }

        val ticket = ticketRepository.findByTicketNumber(ticketNumber)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy vé với mã: $ticketNumber") }

        if (!ticket.checkIn()) {
            throw TicketException("Không thể check-in vé. Vé phải ở trạng thái đã thanh toán.")
        }

        return convertToDto(ticketRepository.save(ticket))
    }

    @Transactional
    override fun cancelTicket(ticketId: UUID): TicketDto {
        val ticket = ticketRepository.findById(ticketId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy vé với ID: $ticketId") }

        // Kiểm tra quyền truy cập
        val currentUser = securityUtils.getCurrentUser()
        if (currentUser == null || (currentUser.id != ticket.user.id && !securityUtils.isAdmin())) {
            throw TicketException("Không có quyền hủy vé này")
        }

        // Kiểm tra xem có thể hủy vé không
        if (!ticket.cancel()) {
            throw TicketException("Không thể hủy vé này. Vé đã check-in hoặc đã bị hủy trước đó.")
        }

        return convertToDto(ticketRepository.save(ticket))
    }

    @Transactional
    override fun updatePaymentStatus(orderId: UUID, paymentId: UUID, status: String): TicketPurchaseResponseDto {
        // Tìm tất cả vé thuộc đơn hàng này (dựa vào paymentId)
        val tickets = ticketRepository.findAll().filter { it.paymentId == orderId }
        if (tickets.isEmpty()) {
            throw ResourceNotFoundException("Không tìm thấy đơn hàng với ID: $orderId")
        }

        val event = tickets.first().event
        val ticketDtos = mutableListOf<TicketDto>()
        var totalAmount = BigDecimal.ZERO

        // Cập nhật trạng thái thanh toán cho tất cả vé
        for (ticket in tickets) {
            if (status == "SUCCESS" || status == "COMPLETED") {
                ticket.markAsPaid(paymentId)
            } else if (status == "FAILED" || status == "CANCELLED") {
                ticket.cancel()
            }
            
            ticketRepository.save(ticket)
            ticketDtos.add(convertToDto(ticket))
            totalAmount = totalAmount.add(ticket.price)
        }

        // Tạo đối tượng phản hồi
        return TicketPurchaseResponseDto(
            orderId = orderId,
            eventId = event.id!!,
            eventTitle = event.title,
            tickets = ticketDtos,
            totalAmount = totalAmount,
            paymentId = paymentId,
            paymentStatus = status,
            purchaseDate = tickets.first().purchaseDate ?: LocalDateTime.now(),
            buyerName = tickets.first().user.fullName,
            buyerEmail = tickets.first().user.email,
            buyerPhone = tickets.first().user.phoneNumber
        )
    }

    @Transactional
    override fun processExpiredReservations(): Int {
        // Tìm các vé đặt chỗ quá 15 phút chưa thanh toán
        val expirationTime = LocalDateTime.now().minusMinutes(15)
        val expiredTickets = ticketRepository.findExpiredReservations(expirationTime)
        
        var count = 0
        for (ticket in expiredTickets) {
            if (ticket.expire()) {
                ticketRepository.save(ticket)
                count++
            }
        }
        
        return count
    }

    /**
     * Kiểm tra quyền truy cập vé
     */
    private fun checkTicketAccess(ticket: Ticket) {
        val currentUser = securityUtils.getCurrentUser()
        if (currentUser == null || (currentUser.id != ticket.user.id && !securityUtils.isAdmin())) {
            throw TicketException("Không có quyền xem vé này")
        }
    }

    /**
     * Chuyển đổi từ entity sang DTO
     */
    private fun convertToDto(ticket: Ticket): TicketDto {
        return TicketDto(
            id = ticket.id,
            ticketNumber = ticket.ticketNumber,
            userId = ticket.user.id!!,
            userName = ticket.user.fullName,
            eventId = ticket.event.id!!,
            eventTitle = ticket.event.title,
            ticketTypeId = ticket.ticketType.id!!,
            ticketTypeName = ticket.ticketType.name,
            price = ticket.price,
            status = ticket.status,
            qrCodeUrl = ticket.qrCode,
            purchaseDate = ticket.purchaseDate,
            checkedInAt = ticket.checkedInAt,
            cancelledAt = ticket.cancelledAt,
            paymentId = ticket.paymentId,
            paymentStatus = if (ticket.status == TicketStatus.PAID) "PAID" else if (ticket.status == TicketStatus.RESERVED) "PENDING" else ticket.status.name,
            eventStartDate = ticket.event.startDate,
            eventEndDate = ticket.event.endDate,
            eventLocation = ticket.event.location.name,
            eventAddress = ticket.event.location.address,
            eventImageUrl = ticket.event.featuredImageUrl
        )
    }
} 