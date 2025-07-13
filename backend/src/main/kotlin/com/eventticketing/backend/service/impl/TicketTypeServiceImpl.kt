package com.eventticketing.backend.service.impl

import com.eventticketing.backend.dto.TicketTypeDto
import com.eventticketing.backend.entity.TicketType
import com.eventticketing.backend.exception.BadRequestException
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.exception.UnauthorizedException
import com.eventticketing.backend.repository.EventRepository
import com.eventticketing.backend.repository.TicketTypeRepository
import com.eventticketing.backend.service.TicketTypeService
import com.eventticketing.backend.util.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class TicketTypeServiceImpl(
    private val ticketTypeRepository: TicketTypeRepository,
    private val eventRepository: EventRepository,
    private val securityUtils: SecurityUtils
) : TicketTypeService {

    private val logger = LoggerFactory.getLogger(TicketTypeServiceImpl::class.java)

    @Transactional
    override fun createTicketType(eventId: UUID, ticketTypeDto: TicketTypeDto): TicketTypeDto {
        // Kiểm tra sự kiện tồn tại
        val event = eventRepository.findById(eventId)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy sự kiện với ID $eventId") }
        
        // Kiểm tra quyền truy cập
        if (!securityUtils.isCurrentUserOrAdmin(event.organizer.id!!)) {
            throw UnauthorizedException("Bạn không có quyền thêm loại vé cho sự kiện này")
        }
        
        // Kiểm tra thông tin hợp lệ
        if (ticketTypeDto.salesStartDate != null && ticketTypeDto.salesEndDate != null && 
            ticketTypeDto.salesStartDate.isAfter(ticketTypeDto.salesEndDate)) {
            throw BadRequestException("Thời gian bắt đầu bán vé phải trước thời gian kết thúc")
        }
        
        // Tạo đối tượng TicketType
        val ticketType = TicketType(
            name = ticketTypeDto.name,
            description = ticketTypeDto.description,
            price = ticketTypeDto.price,
            quantity = ticketTypeDto.quantity,
            availableQuantity = ticketTypeDto.quantity,
            event = event,
            salesStartDate = ticketTypeDto.salesStartDate,
            salesEndDate = ticketTypeDto.salesEndDate,
            maxTicketsPerCustomer = ticketTypeDto.maxTicketsPerCustomer,
            minTicketsPerOrder = ticketTypeDto.minTicketsPerOrder ?: 1,
            isEarlyBird = ticketTypeDto.isEarlyBird,
            isVIP = ticketTypeDto.isVIP,
            isActive = ticketTypeDto.isActive
        )
        
        val savedTicketType = ticketTypeRepository.save(ticketType)
        logger.info("Đã tạo loại vé mới: ${savedTicketType.id} cho sự kiện: $eventId")
        
        return mapToTicketTypeDto(savedTicketType)
    }

    @Transactional
    override fun updateTicketType(id: UUID, ticketTypeDto: TicketTypeDto): TicketTypeDto {
        // Kiểm tra loại vé tồn tại
        val ticketType = findTicketTypeById(id)
        
        // Kiểm tra quyền truy cập
        if (!securityUtils.isCurrentUserOrAdmin(ticketType.event.organizer.id!!)) {
            throw UnauthorizedException("Bạn không có quyền cập nhật loại vé này")
        }
        
        // Kiểm tra thông tin hợp lệ
        if (ticketTypeDto.salesStartDate != null && ticketTypeDto.salesEndDate != null && 
            ticketTypeDto.salesStartDate.isAfter(ticketTypeDto.salesEndDate)) {
            throw BadRequestException("Thời gian bắt đầu bán vé phải trước thời gian kết thúc")
        }
        
        // Cập nhật thông tin
        ticketType.name = ticketTypeDto.name
        ticketType.description = ticketTypeDto.description
        ticketType.price = ticketTypeDto.price
        
        // Nếu số lượng vé mới lớn hơn số lượng vé cũ, cập nhật số lượng vé còn lại
        if (ticketTypeDto.quantity > ticketType.quantity) {
            val difference = ticketTypeDto.quantity - ticketType.quantity
            ticketType.availableQuantity += difference
        }
        
        ticketType.quantity = ticketTypeDto.quantity
        ticketType.salesStartDate = ticketTypeDto.salesStartDate
        ticketType.salesEndDate = ticketTypeDto.salesEndDate
        ticketType.maxTicketsPerCustomer = ticketTypeDto.maxTicketsPerCustomer
        ticketType.minTicketsPerOrder = ticketTypeDto.minTicketsPerOrder ?: 1
        ticketType.isEarlyBird = ticketTypeDto.isEarlyBird
        ticketType.isVIP = ticketTypeDto.isVIP
        ticketType.isActive = ticketTypeDto.isActive
        ticketType.updatedAt = LocalDateTime.now()
        
        val updatedTicketType = ticketTypeRepository.save(ticketType)
        logger.info("Đã cập nhật loại vé: $id")
        
        return mapToTicketTypeDto(updatedTicketType)
    }

    override fun getTicketTypeById(id: UUID): TicketTypeDto {
        val ticketType = findTicketTypeById(id)
        return mapToTicketTypeDto(ticketType)
    }

    override fun getTicketTypesByEventId(eventId: UUID, pageable: Pageable): Page<TicketTypeDto> {
        // Kiểm tra sự kiện tồn tại
        if (!eventRepository.existsById(eventId)) {
            throw ResourceNotFoundException("Không tìm thấy sự kiện với ID $eventId")
        }
        
        return ticketTypeRepository.findByEventId(eventId, pageable)
            .map { mapToTicketTypeDto(it) }
    }

    @Transactional
    override fun deleteTicketType(id: UUID): Boolean {
        // Kiểm tra loại vé tồn tại
        val ticketType = findTicketTypeById(id)
        
        // Kiểm tra quyền truy cập
        if (!securityUtils.isCurrentUserOrAdmin(ticketType.event.organizer.id!!)) {
            throw UnauthorizedException("Bạn không có quyền xóa loại vé này")
        }
        
        // Kiểm tra xem đã có vé nào được bán chưa
        if (ticketType.quantity > ticketType.availableQuantity) {
            throw BadRequestException("Không thể xóa loại vé đã có người mua")
        }
        
        ticketTypeRepository.delete(ticketType)
        logger.info("Đã xóa loại vé: $id")
        
        return true
    }

    @Transactional
    override fun updateAvailableQuantity(id: UUID, purchasedQuantity: Int): TicketTypeDto {
        // Kiểm tra loại vé tồn tại
        val ticketType = findTicketTypeById(id)
        
        // Kiểm tra số lượng hợp lệ
        if (purchasedQuantity <= 0) {
            throw BadRequestException("Số lượng vé phải lớn hơn 0")
        }
        
        // Kiểm tra và cập nhật số lượng vé còn lại
        if (!ticketType.updateAvailableQuantity(purchasedQuantity)) {
            throw BadRequestException("Không đủ vé hoặc loại vé không khả dụng")
        }
        
        val updatedTicketType = ticketTypeRepository.save(ticketType)
        logger.info("Đã cập nhật số lượng vé còn lại cho loại vé: $id, số lượng mua: $purchasedQuantity")
        
        return mapToTicketTypeDto(updatedTicketType)
    }

    override fun checkTicketAvailability(id: UUID, quantity: Int): Boolean {
        try {
            val ticketType = findTicketTypeById(id)
            return ticketType.hasAvailableTickets(quantity)
        } catch (e: ResourceNotFoundException) {
            return false
        }
    }

    /**
     * Tìm TicketType theo ID
     */
    private fun findTicketTypeById(id: UUID): TicketType {
        return ticketTypeRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Không tìm thấy loại vé với ID $id") }
    }

    /**
     * Chuyển đổi TicketType thành TicketTypeDto
     */
    private fun mapToTicketTypeDto(ticketType: TicketType): TicketTypeDto {
        return TicketTypeDto(
            id = ticketType.id,
            name = ticketType.name,
            description = ticketType.description,
            price = ticketType.price,
            quantity = ticketType.quantity,
            availableQuantity = ticketType.availableQuantity,
            eventId = ticketType.event.id,
            salesStartDate = ticketType.salesStartDate,
            salesEndDate = ticketType.salesEndDate,
            maxTicketsPerCustomer = ticketType.maxTicketsPerCustomer,
            minTicketsPerOrder = ticketType.minTicketsPerOrder,
            isEarlyBird = ticketType.isEarlyBird,
            isVIP = ticketType.isVIP,
            isActive = ticketType.isActive,
            createdAt = ticketType.createdAt,
            updatedAt = ticketType.updatedAt
        )
    }
} 