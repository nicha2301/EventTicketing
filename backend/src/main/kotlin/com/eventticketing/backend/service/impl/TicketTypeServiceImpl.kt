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
        val saleStartDate = ticketTypeDto.salesStartDate ?: LocalDateTime.now()
        val saleEndDate = ticketTypeDto.salesEndDate ?: event.endDate
        
        if (saleStartDate.isAfter(saleEndDate)) {
            throw BadRequestException("Thời gian bắt đầu bán vé phải trước thời gian kết thúc")
        }
        
        // Tạo đối tượng TicketType
        val ticketType = TicketType(
            name = ticketTypeDto.name,
            description = ticketTypeDto.description,
            price = ticketTypeDto.price,
            quantity = ticketTypeDto.quantity,
            quantitySold = 0,
            saleStartDate = saleStartDate,
            saleEndDate = saleEndDate,
            maxPerOrder = ticketTypeDto.maxTicketsPerCustomer,
            minPerOrder = ticketTypeDto.minTicketsPerOrder,
            event = event
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
        if (!securityUtils.isCurrentUserOrAdmin(ticketType.event!!.organizer.id!!)) {
            throw UnauthorizedException("Bạn không có quyền cập nhật loại vé này")
        }
        
        // Kiểm tra thông tin hợp lệ
        val saleStartDate = ticketTypeDto.salesStartDate ?: ticketType.saleStartDate
        val saleEndDate = ticketTypeDto.salesEndDate ?: ticketType.saleEndDate
        
        if (saleStartDate.isAfter(saleEndDate)) {
            throw BadRequestException("Thời gian bắt đầu bán vé phải trước thời gian kết thúc")
        }
        
        // Cập nhật thông tin
        ticketType.name = ticketTypeDto.name
        ticketType.description = ticketTypeDto.description
        ticketType.price = ticketTypeDto.price
        
        // Nếu số lượng vé mới lớn hơn số lượng vé cũ, cập nhật số lượng vé còn lại
        if (ticketTypeDto.quantity > ticketType.quantity) {
            // Không cần cập nhật availableQuantity vì entity sử dụng quantitySold
            ticketType.quantity = ticketTypeDto.quantity
        }
        
        ticketType.saleStartDate = saleStartDate
        ticketType.saleEndDate = saleEndDate
        ticketType.maxPerOrder = ticketTypeDto.maxTicketsPerCustomer
        ticketType.minPerOrder = ticketTypeDto.minTicketsPerOrder
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
        if (!securityUtils.isCurrentUserOrAdmin(ticketType.event!!.organizer.id!!)) {
            throw UnauthorizedException("Bạn không có quyền xóa loại vé này")
        }
        
        // Kiểm tra xem đã có vé nào được bán chưa
        if (ticketType.quantitySold > 0) {
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
        if (!ticketType.reserveTickets(purchasedQuantity)) {
            throw BadRequestException("Không đủ vé hoặc loại vé không khả dụng")
        }
        
        val updatedTicketType = ticketTypeRepository.save(ticketType)
        logger.info("Đã cập nhật số lượng vé còn lại cho loại vé: $id, số lượng mua: $purchasedQuantity")
        
        return mapToTicketTypeDto(updatedTicketType)
    }

    override fun checkTicketAvailability(id: UUID, quantity: Int): Boolean {
        try {
            val ticketType = findTicketTypeById(id)
            return ticketType.canReserve(quantity)
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
        val availableQuantity = ticketType.quantity - ticketType.quantitySold
        
        return TicketTypeDto(
            id = ticketType.id,
            name = ticketType.name,
            description = ticketType.description,
            price = ticketType.price,
            quantity = ticketType.quantity,
            availableQuantity = availableQuantity,
            eventId = ticketType.event?.id,
            salesStartDate = ticketType.saleStartDate,
            salesEndDate = ticketType.saleEndDate,
            maxTicketsPerCustomer = ticketType.maxPerOrder,
            minTicketsPerOrder = ticketType.minPerOrder,
            isEarlyBird = false, // Không có trong entity
            isVIP = false, // Không có trong entity
            isActive = ticketType.isOnSale(),
            createdAt = ticketType.createdAt,
            updatedAt = ticketType.updatedAt
        )
    }
} 